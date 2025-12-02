package com.study.client.ws;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class StompWebSocketClient extends WebSocketClient {

    private final Consumer<String> messageConsumer;
    private final String topic;

    public StompWebSocketClient(URI serverUri, String topic, Consumer<String> messageConsumer) {
        super(serverUri);
        this.topic = topic;
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("WS OPENED");
        send("CONNECT\naccept-version:1.2\n\n\0");
        send("SUBSCRIBE\nid:sub-0\ndestination:" + topic + "\n\n\0");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("WS FRAME: " + message);
        if (message.contains("MESSAGE")) {
            int idx = message.indexOf("\n\n");
            if (idx >= 0) {
                String json = message.substring(idx + 2).replace("\u0000", "");
                messageConsumer.accept(json);
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WS CLOSED: " + code + " " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("WS ERROR:");
        ex.printStackTrace();
    }
}
