package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DatabaseConnection;
import model.Pendaftaran;

public class PendaftaranDAO {

    public int insert(Pendaftaran pendaftaran) {
        String sql = "INSERT INTO tb_pendaftaran (pasien_id, dokter_id, no_antrean, tgl_kunjungan, keluhan, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pendaftaran.getPasienId());
            ps.setInt(2, pendaftaran.getDokterId());
            ps.setInt(3, pendaftaran.getNoAntrean());
            ps.setDate(4, pendaftaran.getTglKunjungan());
            ps.setString(5, pendaftaran.getKeluhan());
            ps.setString(6, statusForDatabase(pendaftaran.getStatus()));

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating appointment failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new SQLException("Creating appointment failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert appointment: " + e.getMessage(), e);
        }
    }

    public boolean update(Pendaftaran pendaftaran) {
        String sql = "UPDATE tb_pendaftaran SET pasien_id = ?, dokter_id = ?, no_antrean = ?, tgl_kunjungan = ?, "
                + "keluhan = ?, status = ? WHERE pendaftaran_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pendaftaran.getPasienId());
            ps.setInt(2, pendaftaran.getDokterId());
            ps.setInt(3, pendaftaran.getNoAntrean());
            ps.setDate(4, pendaftaran.getTglKunjungan());
            ps.setString(5, pendaftaran.getKeluhan());
            ps.setString(6, statusForDatabase(pendaftaran.getStatus()));
            ps.setInt(7, pendaftaran.getPendaftaranId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update appointment: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(int pendaftaranId, String status) {
        String sql = "UPDATE tb_pendaftaran SET status = ? WHERE pendaftaran_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusForDatabase(status));
            ps.setInt(2, pendaftaranId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update appointment status: " + e.getMessage(), e);
        }
    }

    public boolean cancelByPasienId(int pendaftaranId, int pasienId) {
        String sql = "UPDATE tb_pendaftaran SET status = ? "
                + "WHERE pendaftaran_id = ? AND pasien_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusForDatabase("Dibatalkan"));
            ps.setInt(2, pendaftaranId);
            ps.setInt(3, pasienId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to cancel appointment: " + e.getMessage(), e);
        }
    }

    public boolean delete(int pendaftaranId) {
        String sql = "DELETE FROM tb_pendaftaran WHERE pendaftaran_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pendaftaranId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete appointment: " + e.getMessage(), e);
        }
    }

    public int getNextQueueNumber(int dokterId, Date tglKunjungan) {
        String sql = "SELECT COALESCE(MAX(no_antrean), 0) + 1 AS next_queue "
                + "FROM tb_pendaftaran WHERE dokter_id = ? AND tgl_kunjungan = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dokterId);
            ps.setDate(2, tglKunjungan);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("next_queue");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate queue number: " + e.getMessage(), e);
        }

        return 1;
    }

    public List<Object[]> findByPasienId(int pasienId) {
        return searchByPasienId(pasienId, null, null);
    }

    public List<Object[]> searchByPasienId(int pasienId, String keyword, String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.pendaftaran_id, d.nama_dokter, po.nama_poli, p.tgl_kunjungan, "
                        + "COALESCE(jd.jam_mulai, '') AS jam_mulai, COALESCE(jd.jam_selesai, '') AS jam_selesai, "
                        + "p.no_antrean, p.status, p.keluhan "
                        + "FROM tb_pendaftaran p "
                        + "JOIN tb_dokter d ON p.dokter_id = d.dokter_id "
                        + "JOIN tb_poli po ON d.poli_id = po.poli_id "
                        + "LEFT JOIN tb_jadwal_dokter jd ON jd.dokter_id = d.dokter_id AND ("
                        + "jd.hari = CASE DAYOFWEEK(p.tgl_kunjungan) "
                        + "WHEN 2 THEN 'Monday' WHEN 3 THEN 'Tuesday' WHEN 4 THEN 'Wednesday' WHEN 5 THEN 'Thursday' "
                        + "WHEN 6 THEN 'Friday' WHEN 7 THEN 'Saturday' WHEN 1 THEN 'Sunday' END "
                        + "OR jd.hari = CASE DAYOFWEEK(p.tgl_kunjungan) "
                        + "WHEN 2 THEN 'Senin' WHEN 3 THEN 'Selasa' WHEN 4 THEN 'Rabu' WHEN 5 THEN 'Kamis' "
                        + "WHEN 6 THEN 'Jumat' WHEN 7 THEN 'Sabtu' WHEN 1 THEN 'Minggu' END) "
                        + "WHERE p.pasien_id = ? ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status.trim());
        String dbStatusFilter = hasStatus ? statusForDatabase(status.trim()) : null;

        if (hasKeyword) {
            sql.append("AND (d.nama_dokter LIKE ? OR po.nama_poli LIKE ? OR p.keluhan LIKE ? OR p.status LIKE ?) ");
        }
        if (hasStatus) {
            sql.append("AND p.status = ? ");
        }
        sql.append("ORDER BY p.tgl_kunjungan DESC, p.no_antrean DESC");

        List<Object[]> rows = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setInt(index++, pasienId);
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
            }
            if (hasStatus) {
                ps.setString(index++, dbStatusFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] {
                            rs.getInt("pendaftaran_id"),
                            rs.getString("nama_dokter"),
                            rs.getString("nama_poli"),
                            rs.getDate("tgl_kunjungan"),
                            formatScheduleTime(rs.getString("jam_mulai"), rs.getString("jam_selesai")),
                            rs.getInt("no_antrean"),
                            statusForDisplay(rs.getString("status")),
                            rs.getString("keluhan")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load patient appointments: " + e.getMessage(), e);
        }

        return rows;
    }

    public List<Object[]> findAll() {
        return searchAll(null, null);
    }

    public List<Object[]> searchAll(String keyword, String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.pendaftaran_id, pa.nama_lengkap AS nama_pasien, d.nama_dokter, po.nama_poli, "
                        + "p.tgl_kunjungan, p.no_antrean, p.status, p.keluhan "
                        + "FROM tb_pendaftaran p "
                        + "JOIN tb_pasien pa ON p.pasien_id = pa.pasien_id "
                        + "JOIN tb_dokter d ON p.dokter_id = d.dokter_id "
                        + "JOIN tb_poli po ON d.poli_id = po.poli_id ");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status.trim());
        String dbStatusFilter = hasStatus ? statusForDatabase(status.trim()) : null;

        if (hasKeyword || hasStatus) {
            sql.append("WHERE ");
            boolean needAnd = false;
            if (hasKeyword) {
                sql.append("(pa.nama_lengkap LIKE ? OR d.nama_dokter LIKE ? OR po.nama_poli LIKE ? OR p.keluhan LIKE ? OR p.status LIKE ?)");
                needAnd = true;
            }
            if (hasStatus) {
                if (needAnd) {
                    sql.append(" AND ");
                }
                sql.append("p.status = ?");
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
            if (hasStatus) {
                ps.setString(index++, dbStatusFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] {
                            rs.getInt("pendaftaran_id"),
                            rs.getString("nama_pasien"),
                            rs.getString("nama_dokter"),
                            rs.getString("nama_poli"),
                            rs.getDate("tgl_kunjungan"),
                            rs.getInt("no_antrean"),
                            statusForDisplay(rs.getString("status")),
                            rs.getString("keluhan")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load appointments: " + e.getMessage(), e);
        }

        return rows;
    }

    public String statusForDisplay(String rawStatus) {
        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            return "Menunggu";
        }

        String normalized = rawStatus.trim();
        if ("1".equals(normalized) || "Menunggu".equalsIgnoreCase(normalized)) {
            return "Menunggu";
        }
        if ("2".equals(normalized) || "Dikonfirmasi".equalsIgnoreCase(normalized) || "Disetujui".equalsIgnoreCase(normalized)) {
            return "Disetujui";
        }
        if ("3".equals(normalized) || "Selesai".equalsIgnoreCase(normalized)) {
            return "Selesai";
        }
        if ("4".equals(normalized) || "Dibatalkan".equalsIgnoreCase(normalized)) {
            return "Dibatalkan";
        }
        return normalized;
    }

    public String statusForDatabase(String uiStatus) {
        if (uiStatus == null || uiStatus.trim().isEmpty()) {
            return "Menunggu";
        }

        String normalized = uiStatus.trim();
        if ("Menunggu".equalsIgnoreCase(normalized) || "1".equals(normalized)) {
            return "Menunggu";
        }
        if ("Disetujui".equalsIgnoreCase(normalized) || "Dikonfirmasi".equalsIgnoreCase(normalized) || "2".equals(normalized)) {
            return "Dikonfirmasi";
        }
        if ("Selesai".equalsIgnoreCase(normalized) || "3".equals(normalized)) {
            return "Selesai";
        }
        if ("Dibatalkan".equalsIgnoreCase(normalized) || "4".equals(normalized)) {
            return "Dibatalkan";
        }
        return normalized;
    }

    private String formatScheduleTime(String jamMulai, String jamSelesai) {
        if ((jamMulai == null || jamMulai.isEmpty()) && (jamSelesai == null || jamSelesai.isEmpty())) {
            return "-";
        }
        return (jamMulai == null ? "" : jamMulai) + " - " + (jamSelesai == null ? "" : jamSelesai);
    }
}
