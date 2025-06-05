package edu.iuc.shared;

import edu.iuc.protocol.MessageParser;
import java.util.Objects;

public final class Message {
    private final MessageType type;
    private final String parameter1;
    private final String parameter2;
    private final long timestamp;
    
    private Message(MessageType type, String parameter1, String parameter2, long timestamp) {
        this.type = Objects.requireNonNull(type, "MessageType null olamaz");
        this.parameter1 = parameter1 != null ? parameter1 : "";
        this.parameter2 = parameter2 != null ? parameter2 : "";
        this.timestamp = timestamp;
    }
    
    public static Message fromRaw(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new Message(MessageType.UNKNOWN, "", "", System.currentTimeMillis());
        }
        
        String[] parts = MessageParser.parse(raw);
        MessageType type = MessageType.fromCommand(parts[0]);
        
        return new Message(type, parts[1], parts[2], System.currentTimeMillis());
    }
    
    public static Message login(String username) {
        return new Message(MessageType.LOGIN, username, "", System.currentTimeMillis());
    }
    
    public static Message listFilesRequest() {
        return new Message(MessageType.LIST_FILES_REQUEST, "", "", System.currentTimeMillis());
    }
    
    public static Message listFilesResponse(String fileList) {
        return new Message(MessageType.LIST_FILES_RESPONSE, fileList, "", System.currentTimeMillis());
    }
    
    public static Message openFileRequest(String fileName) {
        return new Message(MessageType.OPEN_FILE_REQUEST, fileName, "", System.currentTimeMillis());
    }
    
    public static Message openFileResponse(String fileName, String content) {
        return new Message(MessageType.OPEN_FILE_RESPONSE, fileName, content, System.currentTimeMillis());
    }
    
    public static Message edit(String fileName, String content) {
        return new Message(MessageType.EDIT, fileName, content, System.currentTimeMillis());
    }
    
    public static Message createFile(String fileName) {
        return new Message(MessageType.CREATE_FILE, fileName, "", System.currentTimeMillis());
    }
    
    public static Message saveFile(String fileName, String content) {
        return new Message(MessageType.SAVE_FILE, fileName, content, System.currentTimeMillis());
    }
    
    public static Message success(String message) {
        return new Message(MessageType.SUCCESS, message, "", System.currentTimeMillis());
    }
    
    public static Message error(String errorType, String errorMessage) {
        return new Message(MessageType.ERROR, errorType, errorMessage, System.currentTimeMillis());
    }
    
    public static Message userJoined(String username) {
        return new Message(MessageType.USER_JOINED, username, "", System.currentTimeMillis());
    }
    
    public static Message userLeft(String username) {
        return new Message(MessageType.USER_LEFT, username, "", System.currentTimeMillis());
    }
    
    // Getter metodları
    public MessageType getType() {
        return type;
    }
    
    public String getParameter1() {
        return parameter1;
    }
    
    public String getParameter2() {
        return parameter2;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getCommand() {
        return type.getCommand();
    }
    
    public String getHeader() {
        return parameter1;
    }
    
    public String getContent() {
        return parameter2;
    }
    
    public String getUsername() {
        return parameter1;
    }
    
    public String getFileName() {
        return parameter1;
    }
    
    public String getFileContent() {
        return parameter2;
    }
    
    public String getFileList() {
        return parameter1;
    }
    
    public String getErrorType() {
        return parameter1;
    }
    
    public String getErrorMessage() {
        return parameter2;
    }
    
    public String getSuccessMessage() {
        return parameter1;
    }
    
    public boolean isValid() {
        switch (type) {
            case LOGIN:
            case OPEN_FILE_REQUEST:
            case CREATE_FILE:
                return !parameter1.trim().isEmpty();
                
            case EDIT:
            case SAVE_FILE:
            case OPEN_FILE_RESPONSE:
                return !parameter1.trim().isEmpty();
                
            case ERROR:
                return !parameter1.trim().isEmpty();
                
            case LIST_FILES_REQUEST:
            case SUCCESS:
            case USER_JOINED:
            case USER_LEFT:
            case LIST_FILES_RESPONSE:
                return true;
                
            default:
                return false;
        }
    }
    
    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getCommand());
        sb.append("#").append(parameter1);
        sb.append("#");
        
        if (type == MessageType.EDIT || type == MessageType.SAVE_FILE || type == MessageType.OPEN_FILE_RESPONSE) {
            sb.append(parameter2.replace("\n", "\\n"));
        } else {
            sb.append(parameter2);
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Message message = (Message) obj;
        return timestamp == message.timestamp &&
               type == message.type &&
               Objects.equals(parameter1, message.parameter1) &&
               Objects.equals(parameter2, message.parameter2);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, parameter1, parameter2, timestamp);
    }
    
    @Override
    public String toString() {
        return String.format("Message{type=%s, param1='%s', param2='%s', timestamp=%d}", 
                           type, parameter1, parameter2, timestamp);
    }
    
    public static class Builder {
        private MessageType type;
        private String parameter1 = "";
        private String parameter2 = "";
        private long timestamp = System.currentTimeMillis();
        
        public Builder(MessageType type) {
            this.type = type;
        }
        
        public Builder parameter1(String parameter1) {
            this.parameter1 = parameter1;
            return this;
        }
        
        public Builder parameter2(String parameter2) {
            this.parameter2 = parameter2;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Message build() {
            return new Message(type, parameter1, parameter2, timestamp);
        }
    }
}
