package dao;

import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionReportDAO {

    public List<Object[]> findAll(String keyword, String period) {
        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(rm.rekam_medis_id, 0) AS report_id, p.tgl_kunjungan, pa.nama_lengkap, "
                        + "d.nama_dokter, po.nama_poli, "
                        + "CASE WHEN rm.rekam_medis_id IS NULL THEN 'Appointment' ELSE 'Medical Record' END AS report_type, "
                        + "COALESCE(p.status, 'Selesai') AS status, COALESCE(rm.diagnosa, '-') AS diagnosa "
                        + "FROM tb_pendaftaran p "
                        + "JOIN tb_pasien pa ON p.pasien_id = pa.pasien_id "
                        + "JOIN tb_dokter d ON p.dokter_id = d.dokter_id "
                        + "JOIN tb_poli po ON d.poli_id = po.poli_id "
                        + "LEFT JOIN tb_rekam_medis rm ON rm.pendaftaran_id = p.pendaftaran_id ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasDateFilter = period != null && !period.trim().isEmpty() && !"Custom".equalsIgnoreCase(period.trim());
        if (hasKeyword || hasDateFilter) {
            sql.append("WHERE ");
            boolean needAnd = false;
            if (hasKeyword) {
                sql.append("(pa.nama_lengkap LIKE ? OR d.nama_dokter LIKE ? OR po.nama_poli LIKE ? OR p.status LIKE ? OR rm.diagnosa LIKE ?)");
                needAnd = true;
            }
            if (hasDateFilter) {
                if (needAnd) {
                    sql.append(" AND ");
                }
                sql.append(dateFilterSql(period));
            }
        }
        sql.append(" ORDER BY p.tgl_kunjungan DESC, p.no_antrean DESC");

        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] {
                            formatDate(rs.getDate("tgl_kunjungan")),
                            rs.getString("nama_lengkap"),
                            rs.getString("nama_dokter"),
                            rs.getString("report_type"),
                            rs.getString("status"),
                            rs.getString("diagnosa")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transaction reports: " + e.getMessage(), e);
        }

        return rows;
    }

    private String formatDate(Date date) {
        return date == null ? "-" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String dateFilterSql(String period) {
        LocalDate today = LocalDate.now();
        String normalized = period.trim().toLowerCase();
        switch (normalized) {
            case "today":
                return "p.tgl_kunjungan = CURDATE()";
            case "this week":
                return "YEARWEEK(p.tgl_kunjungan, 1) = YEARWEEK(CURDATE(), 1)";
            case "this month":
                return "YEAR(p.tgl_kunjungan) = YEAR(CURDATE()) AND MONTH(p.tgl_kunjungan) = MONTH(CURDATE())";
            default:
                return "p.tgl_kunjungan >= '" + Date.valueOf(today.minusDays(7)) + "'";
        }
    }
}
