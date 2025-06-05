package edu.iuc.server;

import edu.iuc.shared.Message;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;

public class UserManager {
    private static final Map<String, ClientHandler> connectedUsers = new ConcurrentHashMap<>();
    private static final List<ClientHandler> allClients = new CopyOnWriteArrayList<>();
    
    public static boolean addUser(String username, ClientHandler handler) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        if (connectedUsers.containsKey(username)) {
            return false;
        }
        
        connectedUsers.put(username, handler);
        allClients.add(handler);
        
        broadcastToOthers(Message.userJoined(username), handler);
        
        return true;
    }
    
    public static void removeUser(ClientHandler handler) {
        String username = getUsernameByHandler(handler);
        if (username != null) {
            connectedUsers.remove(username);
            
            broadcastToOthers(Message.userLeft(username), handler);
        }
        
        allClients.remove(handler);
    }
    
    public static String getUsernameByHandler(ClientHandler handler) {
        for (Map.Entry<String, ClientHandler> entry : connectedUsers.entrySet()) {
            if (entry.getValue() == handler) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public static void broadcastToAll(String message) {
        for (ClientHandler client : allClients) {
            client.sendMessage(message);
        }
    }
    
    public static void broadcastToAll(Message message) {
        broadcastToAll(message.toProtocolString());
    }
    
    public static void broadcastToOthers(String message, ClientHandler sender) {
        for (ClientHandler client : allClients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    
    public static void broadcastToOthers(Message message, ClientHandler sender) {
        broadcastToOthers(message.toProtocolString(), sender);
    }
    
    public static List<String> getConnectedUsernames() {
        return new CopyOnWriteArrayList<>(connectedUsers.keySet());
    }
    
    public static int getUserCount() {
        return connectedUsers.size();
    }
} 