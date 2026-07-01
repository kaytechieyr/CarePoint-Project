package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DatabaseConnection;
import model.JadwalDokter;

public class JadwalDokterDAO {

    public int insertSchedule(int dokterId, String hari, String jamMulai, String jamSelesai) {
        String sql = "INSERT INTO tb_jadwal_dokter (dokter_id, hari, jam_mulai, jam_selesai) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dokterId);
            ps.setString(2, hari);
            ps.setString(3, jamMulai);
            ps.setString(4, jamSelesai);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating schedule failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new SQLException("Creating schedule failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert schedule: " + e.getMessage(), e);
        }
    }

    public boolean updateSchedule(int jadwalId, int dokterId, String hari, String jamMulai, String jamSelesai) {
        String sql = "UPDATE tb_jadwal_dokter SET dokter_id = ?, hari = ?, jam_mulai = ?, jam_selesai = ? WHERE jadwal_dokter_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dokterId);
            ps.setString(2, hari);
            ps.setString(3, jamMulai);
            ps.setString(4, jamSelesai);
            ps.setInt(5, jadwalId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update schedule: " + e.getMessage(), e);
        }
    }

    public boolean deleteSchedule(int jadwalId) {
        String sql = "DELETE FROM tb_jadwal_dokter WHERE jadwal_dokter_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jadwalId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete schedule: " + e.getMessage(), e);
        }
    }

    public List<Object[]> findAll(String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT jd.jadwal_dokter_id, jd.dokter_id, d.nama_dokter, p.nama_poli, jd.hari, jd.jam_mulai, jd.jam_selesai "
                        + "FROM tb_jadwal_dokter jd "
                        + "JOIN tb_dokter d ON jd.dokter_id = d.dokter_id "
                        + "JOIN tb_poli p ON d.poli_id = p.poli_id ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql.append("WHERE d.nama_dokter LIKE ? OR p.nama_poli LIKE ? OR jd.hari LIKE ? ");
        }
        sql.append("ORDER BY d.nama_dokter, FIELD(jd.hari,'Senin','Monday','Selasa','Tuesday','Rabu','Wednesday','Kamis','Thursday','Jumat','Friday','Sabtu','Saturday','Minggu','Sunday'), jd.jam_mulai");

        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] {
                            rs.getInt("jadwal_dokter_id"),
                            rs.getInt("dokter_id"),
                            rs.getString("nama_dokter"),
                            rs.getString("nama_poli"),
                            rs.getString("hari"),
                            rs.getString("jam_mulai"),
                            rs.getString("jam_selesai")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load schedules: " + e.getMessage(), e);
        }

        return rows;
    }

    public List<JadwalDokter> findByDokterId(int dokterId) {
        String sql = "SELECT jd.jadwal_dokter_id, jd.dokter_id, d.nama_dokter, p.nama_poli, jd.hari, jd.jam_mulai, jd.jam_selesai "
                + "FROM tb_jadwal_dokter jd "
                + "JOIN tb_dokter d ON jd.dokter_id = d.dokter_id "
                + "JOIN tb_poli p ON d.poli_id = p.poli_id "
                + "WHERE jd.dokter_id = ? "
                + "ORDER BY FIELD(jd.hari,'Senin','Monday','Selasa','Tuesday','Rabu','Wednesday','Kamis','Thursday','Jumat','Friday','Sabtu','Saturday','Minggu','Sunday'), jd.jam_mulai";
        List<JadwalDokter> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dokterId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JadwalDokter jadwal = new JadwalDokter();
                    jadwal.setJadwalId(rs.getInt("jadwal_dokter_id"));
                    jadwal.setDokterId(rs.getInt("dokter_id"));
                    jadwal.setNamaDokter(rs.getString("nama_dokter"));
                    jadwal.setNamaPoli(rs.getString("nama_poli"));
                    jadwal.setHari(rs.getString("hari"));
                    jadwal.setJamMulai(rs.getString("jam_mulai"));
                    jadwal.setJamSelesai(rs.getString("jam_selesai"));
                    items.add(jadwal);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load doctor schedules: " + e.getMessage(), e);
        }

        return items;
    }
}
