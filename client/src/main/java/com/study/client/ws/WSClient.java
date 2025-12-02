package com.study.client.ws;

import javafx.application.Platform;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class WSClient implements WebSocket.Listener {

    private final MessageHandler handler;

    public interface MessageHandler {
        void onMessage(JSONObject json);
    }

    public WSClient(Long groupId, MessageHandler handler) {
        this.handler = handler;

        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(
                        URI.create("ws://localhost:8080/ws/groups/" + groupId),
                        this
                );
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        JSONObject json = new JSONObject(data.toString());
        Platform.runLater(() -> handler.onMessage(json));
        ws.request(1);
        return null;
    }
}
