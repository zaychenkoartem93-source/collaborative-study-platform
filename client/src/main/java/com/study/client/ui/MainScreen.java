package com.study.client.ui;

import com.study.client.api.ApiClient;
import com.study.client.ws.StompWebSocketClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;

public class MainScreen {

    private final Stage stage;
    private final ApiClient api;
    private final ListView<String> notifications = new ListView<>(FXCollections.observableArrayList());
    private StompWebSocketClient stompWsClient = null;
    private Long lastSubscribedGroupId = null;

    public MainScreen(Stage stage, ApiClient api) {
        this.stage = stage;
        this.api = api;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        ListView<String> groups = new ListView<>(FXCollections.observableArrayList());
        TextField name = new TextField();
        name.setPromptText("Group name");
        TextField desc = new TextField();
        desc.setPromptText("Description");
        Button create = new Button("Create Group");
        Button openTasks = new Button("Open Tasks");
        Button openResources = new Button("Open Resources");
        Button openStats = new Button("View Stats");
        Button refresh = new Button("Refresh");
        Button profileBtn = new Button("Profile");
        Label status = new Label();

        TextField addMemberField = new TextField();
        addMemberField.setPromptText("User email...");
        Button addMemberBtn = new Button("Add Member");

        HBox top = new HBox(8, new Label("Name:"), name, new Label("Desc:"), desc, create);
        top.setPadding(new Insets(8));

        HBox addMemberBox = new HBox(8, new Label("Add by email:"), addMemberField, addMemberBtn);
        addMemberBox.setPadding(new Insets(8));

        HBox bottom = new HBox(8, openTasks, openResources, openStats, refresh, profileBtn);
        bottom.setPadding(new Insets(8));

        VBox notificationPanel = new VBox(new Label("Notifications:"), notifications);
        notificationPanel.setPadding(new Insets(8));
        root.setLeft(notificationPanel);

        create.setOnAction(e -> {
            try {
                String json = String.format(
                        "{\"name\":\"%s\", \"description\":\"%s\"}",
                        name.getText(),
                        desc.getText()
                );

                var res = api.postJson("/api/groups", json);
                status.setText("Create: " + res.statusCode());
                name.clear();
                desc.clear();
                refresh(groups, status);

            } catch (Exception ex) {
                status.setText("Error: " + ex.getMessage());
            }
        });

        openTasks.setOnAction(e -> {
            if (!groups.getSelectionModel().isEmpty()) {
                String selected = groups.getSelectionModel().getSelectedItem();
                Long groupId = extractGroupId(selected);
                if (groupId != null) new TasksScreen(new Stage(), api, groupId).show();
            }
        });

        openResources.setOnAction(e -> {
            if (!groups.getSelectionModel().isEmpty()) {
                String selected = groups.getSelectionModel().getSelectedItem();
                Long groupId = extractGroupId(selected);
                if (groupId != null) new ResourcesScreen(new Stage(), api, groupId).show();
            }
        });

        openStats.setOnAction(e -> {
            if (!groups.getSelectionModel().isEmpty()) {
                String selected = groups.getSelectionModel().getSelectedItem();
                Long groupId = extractGroupId(selected);
                if (groupId != null) new StatsScreen(new Stage(), api, groupId).show();
            }
        });

        profileBtn.setOnAction(e -> new ProfileScreen(new Stage(), api).show());

        refresh.setOnAction(e -> refresh(groups, status));
        refresh(groups, status);

        addMemberBtn.setOnAction(e -> {
            String selected = groups.getSelectionModel().getSelectedItem();
            if (selected != null && !addMemberField.getText().isBlank()) {
                Long groupId = extractGroupId(selected);
                String email = addMemberField.getText().trim();
                try {
                    String json = "{\"email\":\"" + email + "\"}";
                    var res = api.postJson("/api/groups/" + groupId + "/add-member", json);
                    status.setText("Add member: " + res.statusCode() + " " + res.body());
                    addMemberField.clear();
                } catch (Exception ex) {
                    status.setText("Error: " + ex.getMessage());
                }
            } else {
                status.setText("Select group and enter user email");
            }
        });

        root.setTop(top);
        root.setCenter(new VBox(groups, addMemberBox));
        root.setBottom(new VBox(8, bottom, status));

        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("Collaborative Study Platform");
        stage.show();
    }

    private void refresh(ListView<String> list, Label status) {
        try {
            var res = api.get("/api/groups/my");
            status.setText("Groups: " + res.statusCode());
            list.getItems().clear();
            if (res.statusCode() == 200) {
                JSONArray arr = new JSONArray(res.body());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject g = arr.getJSONObject(i);
                    long groupId = g.getLong("groupId");
                    String name = g.getString("name");
                    String description = g.optString("description", "");
                    String display = "№" + groupId + " | " + name + " | ";
                    if (!description.isBlank()) display += " " + description;
                    list.getItems().add(display);
                }
                if (list.getItems().size() > 0) {
                    String selected = list.getItems().get(0);
                    Long groupId = extractGroupId(selected);
                    if (groupId != null && !groupId.equals(lastSubscribedGroupId)) {
                        subscribeToGroupNotifications(groupId);
                        lastSubscribedGroupId = groupId;
                    }
                }
            }
        } catch (Exception ex) {
            status.setText("Error: " + ex.getMessage());
        }
    }

    private Long extractGroupId(String displayString) {
        try {
            int start = displayString.indexOf("№") + 1;
            int end = displayString.indexOf(" ", start);
            if (start == 0 || end == -1) return null;
            return Long.parseLong(displayString.substring(start, end));
        } catch (Exception e) {
            return null;
        }
    }

    private void subscribeToGroupNotifications(Long groupId) {
        if (stompWsClient != null) {
            stompWsClient.close();
        }
        String wsUrl = "ws://localhost:8080/ws"; // важный суффикс /websocket
        String topic = "/topic/group/" + groupId;

                stompWsClient = new StompWebSocketClient(URI.create(wsUrl), topic, msg -> {
            System.out.println("WS RECEIVED RAW: " + msg);
            Platform.runLater(() -> notifications.getItems().add(formatNotification(msg)));
        });

        String sessionCookie = api.getSessionCookie();
        if (sessionCookie != null) {
            stompWsClient.addHeader("Cookie", sessionCookie);
        }

        stompWsClient.connect();
    }
    private String formatNotification(String message) {
        try {
            JSONObject obj = new JSONObject(message);
            String type = obj.optString("type", "");
            String groupText = " in group \"" + lastSubscribedGroupId + "\"";

            return switch (type) {
                case "NEW_TASK" ->
                        "New task \"" + obj.optString("title", "") + "\"" + groupText;
                case "NEW_MEMBER" ->
                        "New member \"" + obj.optString("userName", "") + "\"" + groupText;
                case "NEW_FILE" ->
                        "New resource \"" + obj.optString("title", "") + "\"" + groupText;
                case "TASK_UPDATED" ->
                        "Task status changed to \"" + obj.optString("status", "") + "\"" + groupText;
                default -> message;
            };
        } catch (Exception ex) {
            ex.printStackTrace();
            return message;
        }
    }
}