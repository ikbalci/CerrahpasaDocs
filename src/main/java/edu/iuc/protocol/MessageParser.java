package edu.iuc.protocol;

import edu.iuc.shared.Message;
import edu.iuc.shared.MessageType;

public class MessageParser {
    
    public static String[] parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new String[]{"", "", ""};
        }
        
        String[] parts = raw.split("#", 3);
        String[] result = new String[3];
        result[0] = parts.length > 0 ? parts[0].trim() : "";
        result[1] = parts.length > 1 ? parts[1].trim() : "";
        result[2] = parts.length > 2 ? parts[2] : "";
        
        return result;
    }
    
    public static Message parseMessage(String raw) {
        return Message.fromRaw(raw);
    }
    
    @Deprecated
    public static String formatLogin(String username) {
        return Message.login(username).toProtocolString();
    }
    
    @Deprecated
    public static String formatListFilesRequest() {
        return Message.listFilesRequest().toProtocolString();
    }
    
    @Deprecated
    public static String formatListFilesResponse(String fileList) {
        return Message.listFilesResponse(fileList).toProtocolString();
    }
    
    @Deprecated
    public static String formatOpenFileRequest(String filename) {
        return Message.openFileRequest(filename).toProtocolString();
    }
    
    @Deprecated
    public static String formatOpenFileResponse(String filename, String content) {
        return Message.openFileResponse(filename, content).toProtocolString();
    }
    
    @Deprecated
    public static String formatEdit(String filename, String content) {
        return Message.edit(filename, content).toProtocolString();
    }
    
    @Deprecated
    public static String formatCreateFile(String filename) {
        return Message.createFile(filename).toProtocolString();
    }
    
    @Deprecated
    public static String formatSaveFile(String filename, String content) {
        return Message.saveFile(filename, content).toProtocolString();
    }
    
    @Deprecated
    public static String formatUserJoined(String username) {
        return Message.userJoined(username).toProtocolString();
    }
    
    @Deprecated
    public static String formatUserLeft(String username) {
        return Message.userLeft(username).toProtocolString();
    }
    
    @Deprecated
    public static String formatError(String errorType, String errorMessage) {
        return Message.error(errorType, errorMessage).toProtocolString();
    }
    
    @Deprecated
    public static String formatSuccess(String message) {
        return Message.success(message).toProtocolString();
    }
    
    public static boolean isValidMessage(String raw) {
        try {
            Message message = parseMessage(raw);
            return message.isValid();
        } catch (Exception e) {
            return false;
        }
    }
    
    public static MessageType getMessageType(String raw) {
        try {
            return parseMessage(raw).getType();
        } catch (Exception e) {
            return MessageType.UNKNOWN;
        }
    }
}

