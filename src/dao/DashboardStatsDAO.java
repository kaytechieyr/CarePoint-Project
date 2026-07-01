package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import config.DatabaseConnection;

public class DashboardStatsDAO {

    public int countPendingApprovals() {
        return countByStatus("tb_pendaftaran", "status", "Menunggu");
    }

    public int countDoctorTotal() {
        return countQuery("SELECT COUNT(*) AS total FROM tb_dokter");
    }

    public int countTodayVisits() {
        return countQuery("SELECT COUNT(*) AS total FROM tb_pendaftaran WHERE tgl_kunjungan = CURDATE()");
    }

    public int countTodayReports() {
        String sql = "SELECT COUNT(*) AS total FROM tb_rekam_medis rm "
                + "JOIN tb_pendaftaran p ON rm.pendaftaran_id = p.pendaftaran_id "
                + "WHERE p.tgl_kunjungan = CURDATE()";
        return countQuery(sql);
    }

    public int countUpcomingAppointmentsForPatient(int pasienId) {
        String sql = "SELECT COUNT(*) AS total FROM tb_pendaftaran "
                + "WHERE pasien_id = ? AND status IN (?, ?)";
        return countQuery(sql, pasienId, "Menunggu", "Dikonfirmasi");
    }

    public int countCompletedVisitsForPatient(int pasienId) {
        String sql = "SELECT COUNT(*) AS total FROM tb_pendaftaran "
                + "WHERE pasien_id = ? AND status = ?";
        return countQuery(sql, pasienId, "Selesai");
    }

    public int countReportsForPatient(int pasienId) {
        String sql = "SELECT COUNT(*) AS total FROM tb_rekam_medis rm "
                + "JOIN tb_pendaftaran p ON rm.pendaftaran_id = p.pendaftaran_id "
                + "WHERE p.pasien_id = ?";
        return countQuery(sql, pasienId);
    }

    public int countAvailableDoctors() {
        return countDoctorTotal();
    }

    private int countByStatus(String table, String column, String status) {
        String sql = "SELECT COUNT(*) AS total FROM " + table + " WHERE " + column + " = ?";
        return countQuery(sql, status);
    }

    private int countQuery(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                Object value = params[i];
                if (value instanceof Integer) {
                    ps.setInt(i + 1, (Integer) value);
                } else if (value instanceof Long) {
                    ps.setLong(i + 1, (Long) value);
                } else {
                    ps.setString(i + 1, value != null ? value.toString() : null);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load dashboard statistic: " + e.getMessage(), e);
        }
        return 0;
    }
}
