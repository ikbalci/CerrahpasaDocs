package edu.iuc.shared;

public enum MessageType {
    LOGIN("LOGIN"),
    LIST_FILES_REQUEST("LIST_FILES_REQUEST"),
    OPEN_FILE_REQUEST("OPEN_FILE_REQUEST"),
    EDIT("EDIT"),
    CREATE_FILE("CREATE_FILE"),
    SAVE_FILE("SAVE_FILE"),
    
    LIST_FILES_RESPONSE("LIST_FILES_RESPONSE"),
    OPEN_FILE_RESPONSE("OPEN_FILE_RESPONSE"),
    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    
    USER_JOINED("USER_JOINED"),
    USER_LEFT("USER_LEFT"),
    
    UNKNOWN("UNKNOWN");
    
    private final String command;
    
    MessageType(String command) {
        this.command = command;
    }
    
    public String getCommand() {
        return command;
    }
    
    public static MessageType fromCommand(String command) {
        if (command == null) {
            return UNKNOWN;
        }
        
        String upperCommand = command.trim().toUpperCase();
        for (MessageType type : values()) {
            if (type.command.equals(upperCommand)) {
                return type;
            }
        }
        return UNKNOWN;
    }
    
    public boolean isClientToServer() {
        return this == LOGIN || this == LIST_FILES_REQUEST || this == OPEN_FILE_REQUEST 
            || this == EDIT || this == CREATE_FILE || this == SAVE_FILE;
    }
    
    public boolean isServerToClient() {
        return this == LIST_FILES_RESPONSE || this == OPEN_FILE_RESPONSE 
            || this == SUCCESS || this == ERROR;
    }
    
    public boolean isBroadcast() {
        return this == USER_JOINED || this == USER_LEFT || this == EDIT;
    }
    
    public boolean requiresAuth() {
        return this != LOGIN && this != UNKNOWN;
    }
} 