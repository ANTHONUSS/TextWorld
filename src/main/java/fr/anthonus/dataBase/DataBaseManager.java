package fr.anthonus.dataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

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
                Cell.cells.add(new Cell(x, y, content));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de la cellule : " + e.getMessage());
        }
    }

    public static void saveCellBlock(List<Cell> cells) {
        String insertQuery = "INSERT INTO Cells (x, y, c) VALUES (?, ?, ?)" +
                "ON CONFLICT(x, y) DO UPDATE SET c=excluded.c";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            var pstmt = conn.prepareStatement(insertQuery);

            for (Cell cell : cells) {
                pstmt.setInt(1, cell.x);
                pstmt.setInt(2, cell.y);
                pstmt.setString(3, cell.c);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

            for (Cell cell : cells) {
                Cell existingCell = Cell.getCell(cell.x, cell.y);
                if (existingCell != null) {
                    existingCell.c = cell.c;
                } else {
                    Cell.cells.add(cell);
                }
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

                Cell.cells.add(new Cell(x, y, c));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des cellules : " + e.getMessage());
        }

        System.out.println("Chargement des cellules terminé");

    }
}
