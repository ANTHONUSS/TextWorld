package fr.anthonus;

import fr.anthonus.server.TextWorldEndPoint;
import jakarta.websocket.DeploymentException;
import org.glassfish.tyrus.server.Server;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws DeploymentException {
        System.out.println("Démarrage du serveur...");

        Server server = new Server("127.0.0.1", 30000, "/", null, TextWorldEndPoint.class);
        server.start();

        System.out.println("Serveur WebSocket démarré");

        Scanner scanner = new Scanner(System.in);
        String input = "help";
        while (true) {
            switch (input) {
                case "help" -> {
                    System.out.println("Commandes disponibles:");
                    System.out.println("\tstop");
                    System.out.println("\trestart");
                }
                case "stop" -> {
                    System.out.println("Arrêt du serveur...");
                    server.stop();

                    System.exit(0);
                }
                case "restart" -> {
                    System.out.println("Arrêt du serveur...");
                    server.stop();

                    System.out.println("Démarrage du serveur...");
                    server.start();
                    System.out.println("Serveur WebSocket démarré");
                }
                default -> System.out.println("Commande inconnue");
            }


            input = scanner.nextLine();
        }
    }
}