package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import config.DatabaseConnection;
import model.Pasien;
import model.User;

public class PasienDAO {

    private final UserDAO userDAO = new UserDAO();

    public boolean registerPasien(User user, Pasien pasien) {
        if (user == null || pasien == null) {
            throw new IllegalArgumentException("User and Pasien must not be null.");
        }

        if (!userDAO.isUsernameUnique(user.getUsername())) {
            return false;
        }

        String sqlPasien = "INSERT INTO tb_pasien "
                + "(user_id, nik, nama_lengkap, jenis_kelamin, tgl_lahir, alamat, no_telepon) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int userId = userDAO.insertUser(user, conn);
            pasien.setUserId(userId);

            try (PreparedStatement ps = conn.prepareStatement(sqlPasien)) {
                ps.setInt(1, pasien.getUserId());
                ps.setString(2, pasien.getNik());
                ps.setString(3, pasien.getNamaLengkap());
                ps.setString(4, pasien.getJenisKelamin());

                Date birthDate = pasien.getTglLahir();
                if (birthDate != null) {
                    ps.setDate(5, birthDate);
                } else {
                    ps.setDate(5, null);
                }

                ps.setString(6, pasien.getAlamat());
                ps.setString(7, pasien.getNoTelepon());
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    throw new RuntimeException("Registration failed and rollback also failed: "
                            + rollbackError.getMessage(), rollbackError);
                }
            }
            throw new RuntimeException("Failed to register patient: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public Pasien findByUserId(int userId) {
        String sql = "SELECT pasien_id, user_id, nik, nama_lengkap, jenis_kelamin, tgl_lahir, alamat, no_telepon "
                + "FROM tb_pasien WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Pasien pasien = new Pasien();
                    pasien.setPasienId(rs.getInt("pasien_id"));
                    pasien.setUserId(rs.getInt("user_id"));
                    pasien.setNik(rs.getString("nik"));
                    pasien.setNamaLengkap(rs.getString("nama_lengkap"));
                    pasien.setJenisKelamin(rs.getString("jenis_kelamin"));
                    pasien.setTglLahir(rs.getDate("tgl_lahir"));
                    pasien.setAlamat(rs.getString("alamat"));
                    pasien.setNoTelepon(rs.getString("no_telepon"));
                    return pasien;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find patient by user id: " + e.getMessage(), e);
        }

        return null;
    }
}
