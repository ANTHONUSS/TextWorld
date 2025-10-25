package fr.anthonus;

import fr.anthonus.dataBase.Cell;
import fr.anthonus.dataBase.DataBaseManager;
import fr.anthonus.server.TextWorldEndPoint;
import jakarta.websocket.DeploymentException;
import org.glassfish.tyrus.server.Server;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws DeploymentException {
        System.out.println("Chargement des cellules...");
        DataBaseManager.loadCells();

        Server server = new Server("127.0.0.1", 30000, "/", null, TextWorldEndPoint.class);

        server.start();
        System.out.println("Serveur WebSocket démarré");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();

            switch (input) {
                case "help" -> {
                    System.out.println("Commandes disponibles:");
                    System.out.println("- stop");
                    System.out.println("- restart");
                }
                case "stop" -> {
                    System.out.println("Arrêt du serveur...");
                    server.stop();

                    System.exit(0);
                }
                case "restart" -> {
                    System.out.println("Arrêt du serveur...");
                    server.stop();

                    System.out.println("Rechargement des cellules...");
                    Cell.cells.clear();
                    DataBaseManager.loadCells();

                    System.out.println("Démarrage du serveur...");
                    server.start();
                    System.out.println("Serveur WebSocket démarré");
                }
                default -> System.out.println("Commande inconnue");
            }
        }
    }
}