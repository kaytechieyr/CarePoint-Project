package dao;

import config.DatabaseConnection;
import model.MedicalReportEntry;
import model.PrescriptionItem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MedicalRecordDAO {

    private final MedicalReportDAO medicalReportDAO = new MedicalReportDAO();

    public List<Object[]> findAppointmentOptions() {
        return findAppointmentOptions(null);
    }

    public List<Object[]> findAppointmentOptions(String statusFilter) {
        List<String> statuses = resolveStatusFilters(statusFilter);
        String sql = "SELECT p.pendaftaran_id, pa.nama_lengkap AS nama_pasien, d.nama_dokter, po.nama_poli, "
                + "p.tgl_kunjungan, rm.rekam_medis_id "
                + "FROM tb_pendaftaran p "
                + "JOIN tb_pasien pa ON p.pasien_id = pa.pasien_id "
                + "JOIN tb_dokter d ON p.dokter_id = d.dokter_id "
                + "JOIN tb_poli po ON d.poli_id = po.poli_id "
                + "LEFT JOIN tb_rekam_medis rm ON rm.pendaftaran_id = p.pendaftaran_id "
                + buildStatusClause(statuses, true)
                + "ORDER BY p.tgl_kunjungan DESC, p.no_antrean DESC";

        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = bindStatusParameters(ps, statuses).executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[] {
                        rs.getInt("pendaftaran_id"),
                        rs.getString("nama_pasien"),
                        rs.getString("nama_dokter"),
                        rs.getString("nama_poli"),
                        rs.getDate("tgl_kunjungan"),
                        getNullableInt(rs, "rekam_medis_id")
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load appointment options: " + e.getMessage(), e);
        }

        return rows;
    }

    public List<MedicalReportEntry> findAllReports() {
        return findAllReports(null);
    }

    public List<MedicalReportEntry> findAllReports(String statusFilter) {
        return medicalReportDAO.findAll(statusFilter);
    }

    public MedicalReportEntry findReportByRekamMedisId(int rekamMedisId) {
        return medicalReportDAO.findByRekamMedisId(rekamMedisId);
    }

    public MedicalReportEntry findReportByPendaftaranId(int pendaftaranId) {
        return medicalReportDAO.findByPendaftaranId(pendaftaranId);
    }

    public int saveMedicalRecord(int rekamMedisId, int pendaftaranId, String diagnosis, List<PrescriptionItem> items) {
        if (pendaftaranId <= 0) {
            throw new IllegalArgumentException("Appointment is required.");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis is required.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one prescription item is required.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int targetRekamMedisId = rekamMedisId;
            boolean insertedNew = false;
            if (targetRekamMedisId <= 0) {
                MedicalReportEntry existing = medicalReportDAO.findByPendaftaranId(conn, pendaftaranId);
                if (existing != null) {
                    targetRekamMedisId = existing.getRekamMedisId();
                } else {
                    targetRekamMedisId = insertRekamMedis(conn, pendaftaranId, diagnosis);
                    insertedNew = true;
                }
            }
            if (!insertedNew) {
                updateRekamMedis(conn, targetRekamMedisId, pendaftaranId, diagnosis);
                deletePrescriptionGraph(conn, targetRekamMedisId);
            }

            int resepId = insertResep(conn, targetRekamMedisId);
            insertPrescriptionItems(conn, resepId, items);

            conn.commit();
            return targetRekamMedisId;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Failed to save medical record: " + e.getMessage(), e);
        } finally {
            resetAutoCommitQuietly(conn);
        }
    }

    public boolean deleteMedicalRecord(int rekamMedisId) {
        if (rekamMedisId <= 0) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            deletePrescriptionGraph(conn, rekamMedisId);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_rekam_medis WHERE rekam_medis_id = ?")) {
                ps.setInt(1, rekamMedisId);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Failed to delete medical record: " + e.getMessage(), e);
        } finally {
            resetAutoCommitQuietly(conn);
        }
    }

    private int insertRekamMedis(Connection conn, int pendaftaranId, String diagnosis) throws SQLException {
        String sql = "INSERT INTO tb_rekam_medis (pendaftaran_id, diagnosa) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pendaftaranId);
            ps.setString(2, diagnosis.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Unable to obtain generated medical record id.");
    }

    private void updateRekamMedis(Connection conn, int rekamMedisId, int pendaftaranId, String diagnosis)
            throws SQLException {
        String sql = "UPDATE tb_rekam_medis SET pendaftaran_id = ?, diagnosa = ? WHERE rekam_medis_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pendaftaranId);
            ps.setString(2, diagnosis.trim());
            ps.setInt(3, rekamMedisId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Medical record not found.");
            }
        }
    }

    private int insertResep(Connection conn, int rekamMedisId) throws SQLException {
        String sql = "INSERT INTO tb_resep (rekam_medis_id, tgl_resep) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rekamMedisId);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Unable to obtain generated prescription id.");
    }

    private void insertPrescriptionItems(Connection conn, int resepId, List<PrescriptionItem> items) throws SQLException {
        String sql = "INSERT INTO tb_detail_resep (resep_id, nama_obat, dosis, aturan_pakai) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PrescriptionItem item : items) {
                ps.setInt(1, resepId);
                ps.setString(2, safeText(item.getMedicineName()));
                ps.setString(3, safeText(item.getDosage()));
                ps.setString(4, safeText(item.getUsageInstruction()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deletePrescriptionGraph(Connection conn, int rekamMedisId) throws SQLException {
        Integer resepId = null;
        String findResepSql = "SELECT resep_id FROM tb_resep WHERE rekam_medis_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(findResepSql)) {
            ps.setInt(1, rekamMedisId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resepId = rs.getInt("resep_id");
                }
            }
        }

        if (resepId == null) {
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_detail_resep WHERE resep_id = ?")) {
            ps.setInt(1, resepId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_resep WHERE resep_id = ?")) {
            ps.setInt(1, resepId);
            ps.executeUpdate();
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void resetAutoCommitQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private Integer getNullableInt(ResultSet rs, String columnLabel) throws SQLException {
        int value = rs.getInt(columnLabel);
        return rs.wasNull() ? null : value;
    }

    private PreparedStatement bindStatusParameters(PreparedStatement ps, List<String> statuses) throws SQLException {
        for (int i = 0; i < statuses.size(); i++) {
            ps.setString(i + 1, statuses.get(i));
        }
        return ps;
    }

    private String buildStatusClause(List<String> statuses, boolean leadingWhere) {
        if (statuses.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(leadingWhere ? " WHERE " : " AND ");
        builder.append("p.status IN (");
        for (int i = 0; i < statuses.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("?");
        }
        builder.append(") ");
        return builder.toString();
    }

    private List<String> resolveStatusFilters(String statusFilter) {
        if (statusFilter == null || statusFilter.trim().isEmpty() || "All".equalsIgnoreCase(statusFilter.trim())) {
            return Arrays.asList("Disetujui", "Dikonfirmasi", "Selesai");
        }
        if ("Disetujui".equalsIgnoreCase(statusFilter.trim())) {
            return Arrays.asList("Disetujui", "Dikonfirmasi");
        }
        if ("Selesai".equalsIgnoreCase(statusFilter.trim())) {
            return Arrays.asList("Selesai");
        }
        return Arrays.asList(statusFilter.trim());
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
