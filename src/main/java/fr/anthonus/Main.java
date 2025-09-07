package fr.anthonus;

import fr.anthonus.server.TextWorldEndPoint;
import org.glassfish.tyrus.server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8080, "/", null, TextWorldEndPoint.class);
        try {
            server.start();
            System.out.println("Serveur WebSocket démarré sur ws://localhost:8080");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}