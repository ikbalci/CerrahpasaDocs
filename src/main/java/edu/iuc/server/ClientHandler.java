package edu.iuc.server;

import edu.iuc.shared.Message;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;
    private boolean isLoggedIn = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                processMessage(line);
            }
        } catch (IOException e) {
            
        } finally {
            cleanup();
        }
    }
    
    private void processMessage(String rawMessage) {
        try {
            Message message = Message.fromRaw(rawMessage);
            
            if (!message.isValid()) {
                sendMessage(Message.error("INVALID_MESSAGE", "Geçersiz mesaj formatı"));
                return;
            }
            
            if (message.getType().requiresAuth() && !isLoggedIn) {
                sendMessage(Message.error("NOT_LOGGED_IN", "Önce giriş yapmanız gerekir"));
                return;
            }
            
            switch (message.getType()) {
                case LOGIN:
                    handleLogin(message.getUsername());
                    break;
                    
                case LIST_FILES_REQUEST:
                    handleListFilesRequest();
                    break;
                    
                case OPEN_FILE_REQUEST:
                    handleOpenFileRequest(message.getFileName());
                    break;
                    
                case EDIT:
                    handleEdit(message.getFileName(), message.getFileContent());
                    break;
                    
                case CREATE_FILE:
                    handleCreateFile(message.getFileName());
                    break;
                    
                case SAVE_FILE:
                    handleSaveFile(message.getFileName(), message.getFileContent());
                    break;
                    
                default:
                    sendMessage(Message.error("UNKNOWN_COMMAND", "Bilinmeyen komut: " + message.getType()));
            }
        } catch (Exception e) {
            sendMessage(Message.error("PARSE_ERROR", "Mesaj ayrıştırılamadı: " + e.getMessage()));
        }
    }
    
    private void handleLogin(String requestedUsername) {
        if (isLoggedIn) {
            sendMessage(Message.error("ALREADY_LOGGED_IN", "Zaten giriş yapılmış"));
            return;
        }
        
        if (requestedUsername == null || requestedUsername.trim().isEmpty()) {
            sendMessage(Message.error("INVALID_USERNAME", "Geçersiz kullanıcı adı"));
            return;
        }
        
        if (UserManager.addUser(requestedUsername, this)) {
            this.username = requestedUsername;
            this.isLoggedIn = true;
            sendMessage(Message.success("Giriş başarılı"));
            
            handleListFilesRequest();
        } else {
            sendMessage(Message.error("USERNAME_TAKEN", "Bu kullanıcı adı zaten kullanılıyor"));
        }
    }
    
    private void handleListFilesRequest() {
        List<String> files = FileManager.listFiles();
        String fileList = String.join(",", files);
        sendMessage(Message.listFilesResponse(fileList));
    }
    
    private void handleOpenFileRequest(String fileName) {
        try {
            String content = FileManager.loadFile(fileName);
            sendMessage(Message.openFileResponse(fileName, content));
        } catch (IOException e) {
            sendMessage(Message.error("FILE_ERROR", e.getMessage()));
        }
    }
    
    private void handleEdit(String fileName, String content) {
        try {
            FileManager.saveFile(fileName, content);
            
            Message editMessage = Message.edit(fileName, content);
            UserManager.broadcastToOthers(editMessage.toProtocolString(), this);
            
        } catch (IOException e) {
            sendMessage(Message.error("SAVE_ERROR", e.getMessage()));
        }
    }
    
    private void handleCreateFile(String fileName) {
        if (FileManager.createFile(fileName)) {
            sendMessage(Message.success("Dosya oluşturuldu: " + fileName));
            
            handleListFilesRequest();
            UserManager.broadcastToOthers(Message.listFilesRequest().toProtocolString(), this);
        } else {
            sendMessage(Message.error("CREATE_ERROR", "Dosya oluşturulamadı (zaten var olabilir)"));
        }
    }
    
    private void handleSaveFile(String fileName, String content) {
        try {
            FileManager.saveFile(fileName, content);
            sendMessage(Message.success("Dosya kaydedildi: " + fileName));
        } catch (IOException e) {
            sendMessage(Message.error("SAVE_ERROR", e.getMessage()));
        }
    }
    
    public void sendMessage(String message) {
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Mesaj gönderilirken hata: " + e.getMessage());
        }
    }
    
    public void sendMessage(Message message) {
        sendMessage(message.toProtocolString());
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    private void cleanup() {
        try {
            if (isLoggedIn) {
                UserManager.removeUser(this);
            }
            
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Cleanup sırasında hata: " + e.getMessage());
        }
    }
}
