package fr.anthonus.dataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseManager {
    private static final String DB_URL = "jdbc:sqlite:database/cells.db";

    public static void saveCell(int x, int y, String content) {
        String insertQuery = "INSERT INTO Cells (x, y, c) VALUES (?, ?, ?)" +
                "ON CONFLICT(x, y) DO UPDATE SET c=excluded.c;";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            var pstmt = conn.prepareStatement(insertQuery);
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            pstmt.setString(3, content);
            pstmt.executeUpdate();

            Cell cell = Cell.getCell(x, y);
            if (cell != null) {
                cell.c = content;
            } else {
                new Cell(x, y, content);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de la cellule : " + e.getMessage());
        }
    }

    public static void loadCells() {
        String selectQuery = "SELECT x, y, c FROM Cells";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            var pstmt = conn.prepareStatement(selectQuery);
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                String c = rs.getString("c");

                new Cell(x, y, c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des cellules : " + e.getMessage());
        }

        System.out.println("Chargement des cellules terminé");

    }
}
