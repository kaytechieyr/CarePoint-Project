package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import config.DatabaseConnection;
import model.DoctorDirectoryItem;

public class DoctorDirectoryDAO {

    public List<DoctorDirectoryItem> findDirectory(String keyword, Integer poliId) {
        StringBuilder sql = new StringBuilder(
                "SELECT d.dokter_id, d.nama_dokter, p.nama_poli, jd.hari, jd.jam_mulai, jd.jam_selesai "
                        + "FROM tb_dokter d "
                        + "JOIN tb_poli p ON d.poli_id = p.poli_id "
                        + "LEFT JOIN tb_jadwal_dokter jd ON jd.dokter_id = d.dokter_id ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasPoli = poliId != null && poliId > 0;
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
        sql.append(" ORDER BY p.nama_poli, d.nama_dokter, FIELD(jd.hari,'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'), jd.jam_mulai");

        Map<Integer, DoctorDirectoryItem> itemsByDoctor = new LinkedHashMap<>();

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

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int doctorId = rs.getInt("dokter_id");
                    DoctorDirectoryItem item = itemsByDoctor.get(doctorId);
                    if (item == null) {
                        item = new DoctorDirectoryItem();
                        item.setDokterId(doctorId);
                        item.setNamaDokter(rs.getString("nama_dokter"));
                        item.setNamaPoli(rs.getString("nama_poli"));
                        itemsByDoctor.put(doctorId, item);
                    }

                    String hari = rs.getString("hari");
                    String jamMulai = rs.getString("jam_mulai");
                    String jamSelesai = rs.getString("jam_selesai");
                    if (hari != null) {
                        item.addSchedule(hari + " | "
                                + (jamMulai != null ? jamMulai : "-")
                                + " - "
                                + (jamSelesai != null ? jamSelesai : "-"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load doctor directory: " + e.getMessage(), e);
        }

        return new ArrayList<>(itemsByDoctor.values());
    }
}
