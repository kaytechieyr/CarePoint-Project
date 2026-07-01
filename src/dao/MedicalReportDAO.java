package dao;

import config.DatabaseConnection;
import model.MedicalReportEntry;
import model.PrescriptionItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalReportDAO {

    public List<MedicalReportEntry> findByPasienId(int pasienId) {
        String sql = baseSql() + "WHERE p.pasien_id = ? " + orderBySql();
        return query(sql, pasienId);
    }

    public List<MedicalReportEntry> findAll() {
        String sql = baseSql() + orderBySql();
        return query(sql);
    }

    public List<MedicalReportEntry> findAll(String statusFilter) {
        List<String> statuses = resolveStatusFilters(statusFilter);
        StringBuilder sql = new StringBuilder(baseSql());
        if (!statuses.isEmpty()) {
            sql.append("WHERE p.status IN (");
            for (int i = 0; i < statuses.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append("?");
            }
            sql.append(") ");
        }
        sql.append(orderBySql());
        return query(sql.toString(), statuses.toArray());
    }

    public MedicalReportEntry findByRekamMedisId(int rekamMedisId) {
        String sql = baseSql() + "WHERE rm.rekam_medis_id = ? " + orderBySql();
        List<MedicalReportEntry> rows = query(sql, rekamMedisId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public MedicalReportEntry findByPendaftaranId(int pendaftaranId) {
        String sql = baseSql() + "WHERE p.pendaftaran_id = ? " + orderBySql();
        List<MedicalReportEntry> rows = query(sql, pendaftaranId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public MedicalReportEntry findByPendaftaranId(Connection conn, int pendaftaranId) {
        String sql = baseSql() + "WHERE p.pendaftaran_id = ? " + orderBySql();
        List<MedicalReportEntry> rows = query(conn, sql, pendaftaranId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String baseSql() {
        return "SELECT rm.rekam_medis_id, p.pendaftaran_id, p.tgl_kunjungan, pa.nama_lengkap AS nama_pasien, "
                + "d.nama_dokter, po.nama_poli, rm.diagnosa, "
                + "dr.detail_resep_id, dr.nama_obat, dr.dosis, dr.aturan_pakai "
                + "FROM tb_rekam_medis rm "
                + "JOIN tb_pendaftaran p ON rm.pendaftaran_id = p.pendaftaran_id "
                + "JOIN tb_pasien pa ON p.pasien_id = pa.pasien_id "
                + "JOIN tb_dokter d ON p.dokter_id = d.dokter_id "
                + "JOIN tb_poli po ON d.poli_id = po.poli_id "
                + "LEFT JOIN tb_resep r ON r.rekam_medis_id = rm.rekam_medis_id "
                + "LEFT JOIN tb_detail_resep dr ON dr.resep_id = r.resep_id ";
    }

    private String orderBySql() {
        return "ORDER BY p.tgl_kunjungan DESC, rm.rekam_medis_id DESC, dr.detail_resep_id ASC";
    }

    private List<MedicalReportEntry> query(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return query(conn, sql, params);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load medical report data: " + e.getMessage(), e);
        }
    }

    private List<MedicalReportEntry> query(Connection conn, String sql, Object... params) {
        Map<Integer, MedicalReportEntry> map = new LinkedHashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                } else {
                    ps.setObject(i + 1, param);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int reportId = rs.getInt("rekam_medis_id");
                    MedicalReportEntry entry = map.get(reportId);
                    if (entry == null) {
                        entry = new MedicalReportEntry();
                        entry.setRekamMedisId(reportId);
                        entry.setPendaftaranId(rs.getInt("pendaftaran_id"));
                        entry.setVisitDate(rs.getDate("tgl_kunjungan"));
                        entry.setPatientName(rs.getString("nama_pasien"));
                        entry.setDoctorName(rs.getString("nama_dokter"));
                        entry.setPoliName(rs.getString("nama_poli"));
                        entry.setDiagnosis(rs.getString("diagnosa"));
                        map.put(reportId, entry);
                    }

                    Integer detailId = getNullableInt(rs, "detail_resep_id");
                    String medicineName = rs.getString("nama_obat");
                    if (detailId != null || (medicineName != null && !medicineName.isBlank())) {
                        PrescriptionItem item = new PrescriptionItem();
                        if (detailId != null) {
                            item.setDetailResepId(detailId);
                        }
                        item.setMedicineName(medicineName);
                        item.setDosage(rs.getString("dosis"));
                        item.setUsageInstruction(rs.getString("aturan_pakai"));
                        entry.addPrescription(item);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load medical report data: " + e.getMessage(), e);
        }

        return new ArrayList<>(map.values());
    }

    private Integer getNullableInt(ResultSet rs, String columnLabel) throws SQLException {
        int value = rs.getInt(columnLabel);
        return rs.wasNull() ? null : value;
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
}
