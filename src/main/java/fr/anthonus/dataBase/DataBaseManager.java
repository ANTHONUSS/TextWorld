package fr.anthonus.dataBase;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataBaseManager {
    private static final String DB_URL = "jdbc:sqlite:database/cells.db";

    public static void saveCell(int x, int y, String content, String address) {
        if (isUserBannedAndSaveNewUser(address)) { return; }

        String insertQuery = "INSERT INTO Cells (x, y, char, address) VALUES (?, ?, ?, ?)" +
                "ON CONFLICT(x, y) DO UPDATE SET char=excluded.char, address=excluded.address";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            String charContent = content.substring(0, 1);
            pstmt.setString(3, charContent);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de la cellule : " + e.getMessage());
        }
    }

    public static void saveCellBlock(List<Cell> cells, String address) {
        if (isUserBannedAndSaveNewUser(address)) { return; }

        String insertQuery = "INSERT INTO Cells (x, y, char, address) VALUES (?, ?, ?, ?)" +
                "ON CONFLICT(x, y) DO UPDATE SET char=excluded.char, address=excluded.address";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);

            for (Cell cell : cells) {
                pstmt.setInt(1, cell.x);
                pstmt.setInt(2, cell.y);
                String charContent = cell.c.substring(0, 1);
                pstmt.setString(3, charContent);
                pstmt.setString(4, address);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de la cellule : " + e.getMessage());
        }
    }

    public static Cell getCell(int x, int y) {
        String query = "SELECT char FROM Cells WHERE x = ? AND y = ?;";

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
        String query = "SELECT x, y, char FROM Cells WHERE x >= ? AND x < ? AND y >= ? AND y < ?";

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

    public static void saveIP(String address) {
        String insertQuery = "INSERT INTO IPs (address) VALUES (?);";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, address);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de l'ip : " + e.getMessage());
        }
    }

    public static IP getIP(String address) {
        String query = "SELECT * FROM IPs WHERE address = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, address);
            ResultSet rs = pstmt.executeQuery();

            return new IP(address, rs.getBoolean("banned"), rs.getString("until"), rs.getString("reason"));

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'IP : " + e.getMessage());
        }

        return null;
    }

    public static boolean ipAlreadyExists(String address) {
        String query = "SELECT * FROM IPs WHERE address = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, address);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'IP : " + e.getMessage());
        }

        return false;

    }

    public static void banIP(String address, String reason, String until) {
        String query = "update IPs " +
                "SET banned=1," +
                "until=?," +
                "reason=? " +
                "where address like ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, until);
            pstmt.setString(2, reason);
            pstmt.setString(3, address);
            pstmt.executeQuery();

        } catch (SQLException e) {
            System.err.println("Erreur lors du bannissement de l'IP : " + e.getMessage());
        }
    }

    public static void unbanIP(String address) {
        String query = "update IPs " +
                "SET banned=0," +
                "until=null," +
                "reason=null " +
                "where address like ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, address);
            pstmt.executeQuery();

        } catch (SQLException e) {
            System.err.println("Erreur lors du bannissement de l'IP : " + e.getMessage());
        }
    }

    private static boolean isUserBannedAndSaveNewUser(String address) {
        if (!ipAlreadyExists(address)) {
            saveIP(address);
            return false; // nouvel arrivant, forcément pas banni
        }

        IP ip = getIP(address);
        if (ip.banned) {
            String until = ip.until;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            LocalDateTime untilDate = LocalDateTime.parse(until, formatter);
            Instant instant = untilDate.atZone(ZoneId.systemDefault()).toInstant();
            boolean expired = instant.isAfter(Instant.now());

            if (expired) {
                unbanIP(address); // Date de bannissement terminée
                return false;
            } else {
                return true; // Banni
            }
        }

        return false;

    }


}
