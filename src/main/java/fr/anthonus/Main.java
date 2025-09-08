package fr.anthonus;

import fr.anthonus.dataBase.DataBaseManager;
import fr.anthonus.server.TextWorldEndPoint;
import org.glassfish.tyrus.server.Server;

public class Main {
    public static void main(String[] args) {
        DataBaseManager.initDatabase();
        DataBaseManager.loadCells();

        Server server = new Server("localhost", 30000, "/", null, TextWorldEndPoint.class);
        try {
            server.start();
            System.out.println("Serveur WebSocket démarré");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}