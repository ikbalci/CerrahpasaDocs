package edu.iuc.client;

import edu.iuc.shared.Message;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SocketIOClientAdapter {
    private Socket socket;
    private boolean isConnected = false;
    private Consumer<String> messageHandler;
    private Consumer<String> disconnectHandler;
    private Consumer<String> connectHandler;

    public SocketIOClientAdapter() {
        // Boş constructor
    }

    public CompletableFuture<Boolean> connect(String serverUrl) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnection = true;
            options.reconnectionAttempts = 5;
            options.reconnectionDelay = 1000;
            options.timeout = 5000;
            
            socket = IO.socket(serverUrl, options);
            
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    isConnected = true;
                    if (connectHandler != null) {
                        connectHandler.accept("Bağlandı");
                    }
                    future.complete(true);
                }
            });
            
            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    isConnected = false;
                    if (disconnectHandler != null) {
                        disconnectHandler.accept("Bağlantı kesildi");
                    }
                }
            });
            
            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    isConnected = false;
                    String error = args.length > 0 ? args[0].toString() : "Bilinmeyen hata";
                    if (disconnectHandler != null) {
                        disconnectHandler.accept("Bağlantı hatası: " + error);
                    }
                    future.complete(false);
                }
            });
            
            socket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args.length > 0 && messageHandler != null) {
                        String message = args[0].toString();
                        messageHandler.accept(message);
                    }
                }
            });
            
            socket.connect();
            
        } catch (URISyntaxException e) {
            future.complete(false);
        }
        
        return future;
    }

    public void sendMessage(String message) {
        if (socket != null && isConnected) {
            socket.emit("message", message);
        }
    }

    public void sendMessage(Message message) {
        sendMessage(message.toProtocolString());
    }

    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    public void setDisconnectHandler(Consumer<String> handler) {
        this.disconnectHandler = handler;
    }

    public void setConnectHandler(Consumer<String> handler) {
        this.connectHandler = handler;
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.close();
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
} 