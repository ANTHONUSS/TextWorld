package fr.anthonus.server;

import com.google.gson.*;
import fr.anthonus.dataBase.Cell;
import fr.anthonus.dataBase.DataBaseManager;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.core.TyrusSession;
import org.glassfish.tyrus.spi.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws")
public class TextWorldEndPoint {
    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        getIP(session);
        sessions.add(session);
        System.out.println("Client connecté : " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message reçu de " + session.getId() + ": " + message);
        try {
            var jsonObject = JsonParser.parseString(message).getAsJsonObject();
            if (!jsonObject.has("type") || jsonObject.get("type").isJsonNull()) {
                sendError(session, "Missing 'type' field");
                return;
            }
            String type = jsonObject.get("type").getAsString();
            switch (type) {
                case "request_zone" -> handleRequestZone(jsonObject, session);
                case "cell_update" -> handleCellUpdate(jsonObject, session);
                case "cell_update_block" -> handleCellUpdateBlock(jsonObject, session);
                case "ping" -> handlePing(session);
                default -> sendError(session, "Unknown type: " + type);
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            System.err.println("JSON invalide de " + session.getId() + ": " + e.getMessage());
            sendError(session, "JSON invalide: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du message de " + session.getId() + ": " + e.getMessage());
            sendError(session, "Erreur serveur: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("Client déconnecté : " + session.getId());
    }

    private void handleRequestZone(JsonObject jsonObject, Session session) {
        if (!jsonObject.has("x") || !jsonObject.has("y") || !jsonObject.has("w") || !jsonObject.has("h")) {
            sendError(session, "Missing x/y/w/h for request_zone");
            return;
        }
        int x = jsonObject.get("x").getAsInt();
        int y = jsonObject.get("y").getAsInt();
        int width = jsonObject.get("w").getAsInt();
        int height = jsonObject.get("h").getAsInt();

        JsonObject response = new JsonObject();
        response.addProperty("type", "zone_data");
        response.addProperty("x", x);
        response.addProperty("y", y);
        response.addProperty("w", width);
        response.addProperty("h", height);

        List<Cell> cells = DataBaseManager.getCellBlock(x, y, width, height);
        JsonArray chars = new JsonArray();
        for (Cell cell : cells) {
            JsonObject cellJson = new JsonObject();
            cellJson.addProperty("x", cell.x);
            cellJson.addProperty("y", cell.y);
            cellJson.addProperty("c", cell.c);
            chars.add(cellJson);
        }
        response.add("chars", chars);

        try {
            session.getBasicRemote().sendText(response.toString());
            System.out.println("Envoyé zone à " + session.getId());
        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi de la zone : " + e.getMessage());
        }
    }

    private void handleCellUpdate(JsonObject jsonObject, Session session) {
        if (!jsonObject.has("x") || !jsonObject.has("y") || !jsonObject.has("c")) {
            sendError(session, "Missing x/y/c|char for cell_update");
            return;
        }

        int x = jsonObject.get("x").getAsInt();
        int y = jsonObject.get("y").getAsInt();
        String c = jsonObject.get("c").getAsString();
        String address = session.getUserProperties().get("org.glassfish.tyrus.remoteAddress").toString();
        DataBaseManager.saveCell(x, y, c, address);

        JsonObject response = new JsonObject();
        response.addProperty("type", "cell_update");
        response.addProperty("x", x);
        response.addProperty("y", y);
        response.addProperty("c", c);

        sendBroadcast(session, response);
    }

    private void handleCellUpdateBlock(JsonObject jsonObject, Session session) {
        if (!jsonObject.has("chars")) {
            sendError(session, "Missing 'chars' array for cell_update_block");
            return;
        }

        JsonArray charsJson = jsonObject.getAsJsonArray("chars");
        for (JsonElement elem : charsJson) {
            JsonObject cellJson = elem.getAsJsonObject();
            if (!cellJson.has("x") || !cellJson.has("y") || !cellJson.has("c")) {
                sendError(session, "Each cell in 'chars' must have x/y/c");
                return;
            }
        }

        List<Cell> cellsToUpdate = new ArrayList<>();
        for (JsonElement elem : charsJson) {
            JsonObject cellJson = elem.getAsJsonObject();
            int x = cellJson.get("x").getAsInt();
            int y = cellJson.get("y").getAsInt();
            String c = cellJson.get("c").getAsString();
            cellsToUpdate.add(new Cell(x, y, c));
        }
        String address = session.getUserProperties().get("org.glassfish.tyrus.remoteAddress").toString();
        DataBaseManager.saveCellBlock(cellsToUpdate, address);

        JsonObject response = new JsonObject();
        response.addProperty("type", "cell_update_block");
        response.add("chars", charsJson);

        sendBroadcast(session, response);
    }

    private void handlePing(Session session) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "pong");
        try {
            session.getBasicRemote().sendText(response.toString());
            System.out.println("Envoyé pong à " + session.getId());
        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi du pong : " + e.getMessage());
        }
    }

    private void sendBroadcast(Session session, JsonObject response) {
        for(Session s : sessions) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                try {
                    s.getBasicRemote().sendText(response.toString());
                    System.out.println("Envoyé mise à jour cellule à " + s.getId());
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'envoi de la mise à jour de la cellule : " + e.getMessage());
                }
            }
        }
    }

    private void sendError(Session session, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("type", "error");
        error.addProperty("message", message);
        try {
            session.getBasicRemote().sendText(error.toString());
        } catch (IOException e) {
            System.err.println("Impossible d'envoyer l'erreur au client " + session.getId() + ": " + e.getMessage());
        }
    }

    private void getIP(Session session) {

        String ip = (String) session.getUserProperties().get("remote-ip");
        System.out.println(ip);

//        // Tyrus standalone/Grizzly définit souvent cette clé :
//        Object addrObj = session.getUserProperties().get("org.glassfish.tyrus.remoteAddress");
//        String ip = "unknown";
//        if (addrObj != null) {
//            ip = addrObj.toString().replaceAll("^/|:.*$", "");
//        }
//
//        System.out.println("Client connected from " + ip);
    }
}
