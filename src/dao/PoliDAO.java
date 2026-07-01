package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DatabaseConnection;
import model.Poli;

public class PoliDAO {

    public List<Poli> findAll() {
        String sql = "SELECT poli_id, nama_poli FROM tb_poli ORDER BY nama_poli";
        List<Poli> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Poli poli = new Poli();
                poli.setPoliId(rs.getInt("poli_id"));
                poli.setNamaPoli(rs.getString("nama_poli"));
                items.add(poli);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load poli list: " + e.getMessage(), e);
        }

        return items;
    }
}
