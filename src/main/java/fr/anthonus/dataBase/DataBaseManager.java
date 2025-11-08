package fr.anthonus.dataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseManager {
    private static final String DB_URL = "jdbc:sqlite:database/cells.db";

    public static void saveCell(int x, int y, String content) {
        String insertQuery = "INSERT INTO Cells (x, y, c) VALUES (?, ?, ?)" +
                "ON CONFLICT(x, y) DO UPDATE SET c=excluded.c;";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            pstmt.setString(3, content);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de la cellule : " + e.getMessage());
        }
    }

    public static void saveCellBlock(List<Cell> cells) {
        String insertQuery = "INSERT INTO Cells (x, y, c) VALUES (?, ?, ?)" +
                "ON CONFLICT(x, y) DO UPDATE SET c=excluded.c";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);

            for (Cell cell : cells) {
                pstmt.setInt(1, cell.x);
                pstmt.setInt(2, cell.y);
                pstmt.setString(3, cell.c);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de la cellule : " + e.getMessage());
        }
    }

    public static Cell getCell(int x, int y) {
        String query = "SELECT c FROM Cells WHERE x = ? AND y = ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            ResultSet rs = pstmt.executeQuery();

            return new Cell(x, y, rs.getString("c"));

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la cellule : " + e.getMessage());
        }

        return null;
    }

    public static List<Cell> getCellBlock(int x, int y, int w, int h) {
        String query = "SELECT x, y, c FROM Cells WHERE x >= ? AND x < ? AND y >= ? AND y < ?";

        List<Cell> cells = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, x);
            pstmt.setInt(2, x + w);
            pstmt.setInt(3, y);
            pstmt.setInt(4, y + h);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                cells.add(new Cell(rs.getInt("x"), rs.getInt("y"), rs.getString("c")));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de la cellule : " + e.getMessage());
        }

        return cells;
    }
}
