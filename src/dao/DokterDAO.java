package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DatabaseConnection;
import model.DoctorDirectoryItem;
import model.Dokter;

public class DokterDAO {

    public List<Dokter> findAll() {
        String sql = "SELECT d.dokter_id, d.poli_id, d.nama_dokter, p.nama_poli "
                + "FROM tb_dokter d "
                + "JOIN tb_poli p ON d.poli_id = p.poli_id "
                + "ORDER BY p.nama_poli, d.nama_dokter";
        List<Dokter> doctors = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Dokter dokter = new Dokter();
                dokter.setDokterId(rs.getInt("dokter_id"));
                dokter.setPoliId(rs.getInt("poli_id"));
                dokter.setNamaDokter(rs.getString("nama_dokter"));
                dokter.setNamaPoli(rs.getString("nama_poli"));
                doctors.add(dokter);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load doctors: " + e.getMessage(), e);
        }

        return doctors;
    }

    public List<Dokter> findByPoliId(int poliId) {
        String sql = "SELECT d.dokter_id, d.poli_id, d.nama_dokter, p.nama_poli "
                + "FROM tb_dokter d "
                + "JOIN tb_poli p ON d.poli_id = p.poli_id "
                + "WHERE d.poli_id = ? "
                + "ORDER BY d.nama_dokter";
        List<Dokter> doctors = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poliId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Dokter dokter = new Dokter();
                    dokter.setDokterId(rs.getInt("dokter_id"));
                    dokter.setPoliId(rs.getInt("poli_id"));
                    dokter.setNamaDokter(rs.getString("nama_dokter"));
                    dokter.setNamaPoli(rs.getString("nama_poli"));
                    doctors.add(dokter);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load doctors by poli: " + e.getMessage(), e);
        }

        return doctors;
    }

    public Dokter findById(int dokterId) {
        String sql = "SELECT d.dokter_id, d.poli_id, d.nama_dokter, p.nama_poli "
                + "FROM tb_dokter d "
                + "JOIN tb_poli p ON d.poli_id = p.poli_id "
                + "WHERE d.dokter_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dokterId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dokter dokter = new Dokter();
                    dokter.setDokterId(rs.getInt("dokter_id"));
                    dokter.setPoliId(rs.getInt("poli_id"));
                    dokter.setNamaDokter(rs.getString("nama_dokter"));
                    dokter.setNamaPoli(rs.getString("nama_poli"));
                    return dokter;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find doctor: " + e.getMessage(), e);
        }

        return null;
    }

    public List<DoctorDirectoryItem> findDirectory(String keyword, Integer poliId) {
        StringBuilder sql = new StringBuilder(
                "SELECT d.dokter_id, d.nama_dokter, p.nama_poli, jd.hari, jd.jam_mulai, jd.jam_selesai "
                        + "FROM tb_dokter d "
                        + "JOIN tb_poli p ON d.poli_id = p.poli_id "
                        + "LEFT JOIN tb_jadwal_dokter jd ON jd.dokter_id = d.dokter_id ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasPoli = poliId != null;
        if (hasKeyword || hasPoli) {
            sql.append("WHERE ");
            boolean needAnd = false;
            if (hasKeyword) {
                sql.append("(d.nama_dokter LIKE ? OR p.nama_poli LIKE ?)");
                needAnd = true;
            }
            if (hasPoli) {
                if (needAnd) {
                    sql.append(" AND ");
                }
                sql.append("d.poli_id = ?");
            }
        }
        sql.append(" ORDER BY p.nama_poli, d.nama_dokter, jd.hari, jd.jam_mulai");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(index++, like);
                ps.setString(index++, like);
            }
            if (hasPoli) {
                ps.setInt(index++, poliId);
            }

            java.util.LinkedHashMap<Integer, DoctorDirectoryItem> map = new java.util.LinkedHashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int doctorId = rs.getInt("dokter_id");
                    DoctorDirectoryItem item = map.get(doctorId);
                    if (item == null) {
                        item = new DoctorDirectoryItem();
                        item.setDokterId(doctorId);
                        item.setNamaDokter(rs.getString("nama_dokter"));
                        item.setNamaPoli(rs.getString("nama_poli"));
                        map.put(doctorId, item);
                    }

                    String hari = rs.getString("hari");
                    String jamMulai = rs.getString("jam_mulai");
                    String jamSelesai = rs.getString("jam_selesai");
                    if (hari != null) {
                        item.addSchedule(hari + " | " + (jamMulai != null ? jamMulai : "-") + " - "
                                + (jamSelesai != null ? jamSelesai : "-"));
                    }
                }
            }
            return new java.util.ArrayList<>(map.values());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load doctor directory: " + e.getMessage(), e);
        }
    }
}
