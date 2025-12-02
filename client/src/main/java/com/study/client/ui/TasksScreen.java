package com.study.client.ui;

import com.study.client.api.ApiClient;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.*;
import java.time.format.DateTimeParseException;

public class TasksScreen {

    private final Stage stage;
    private final ApiClient api;
    private final Long groupId;

    public TasksScreen(Stage stage, ApiClient api, Long groupId) {
        this.stage = stage;
        this.api = api;
        this.groupId = groupId;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        ListView<String> taskList = new ListView<>(FXCollections.observableArrayList());
        TextField title = new TextField();
        TextField desc = new TextField();
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("OPEN", "IN_PROGRESS", "DONE"));
        status.setValue("OPEN");

        DatePicker deadlineDate = new DatePicker();
        TextField deadlineTime = new TextField();
        deadlineTime.setPromptText("hh:mm");

        Button create = new Button("Create Task");
        Button refresh = new Button("Refresh");
        Button delete = new Button("Delete Task");
        Button changeStatus = new Button("Change Status");
        Label info = new Label("Group ID: " + groupId);

        HBox top = new HBox(8,
                new Label("Title:"), title,
                new Label("Desc:"), desc,
                new Label("Status:"), status,
                new Label("Deadline:"), deadlineDate, deadlineTime,
                create, refresh, delete, changeStatus
        );
        top.setPadding(new Insets(8));

        create.setOnAction(e -> {
            try {
                // Собираем дату и время (Instant)
                String deadlineStr = "";
                if (deadlineDate.getValue() != null) {
                    String t = deadlineTime.getText().trim();
                    if (t.isEmpty()) t = "00:00";
                    if (t.length() == 5) t += ":00";
                    deadlineStr = deadlineDate.getValue() + "T" + t + "Z";
                }

                String json = "{\"groupId\":" + groupId +
                        ",\"title\":\"" + title.getText() +
                        "\",\"description\":\"" + desc.getText() +
                        "\",\"status\":\"" + status.getValue() + "\"" +
                        (deadlineStr.isEmpty() ? "" : ",\"deadline\":\"" + deadlineStr + "\"") +
                        "}";

                var res = api.postJson("/api/tasks", json);
                info.setText("Created: " + res.statusCode());
                title.clear();
                desc.clear();
                deadlineDate.setValue(null);
                deadlineTime.clear();
                refreshTasks(taskList, info);
            } catch (Exception ex) {
                info.setText("Error: " + ex.getMessage());
            }
        });

        refresh.setOnAction(e -> refreshTasks(taskList, info));

        delete.setOnAction(e -> {
            String selected = taskList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Long taskId = extractTaskId(selected);
                if (taskId != null) {
                    try {
                        var res = api.delete("/api/tasks/" + taskId);
                        info.setText("Delete: " + res.statusCode());
                        refreshTasks(taskList, info);
                    } catch (Exception ex) {
                        info.setText("Error: " + ex.getMessage());
                    }
                }
            }
        });

        changeStatus.setOnAction(e -> {
            String selected = taskList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Long taskId = extractTaskId(selected);
                String currentStatus = extractStatus(selected);
                String newStatus = nextStatus(currentStatus);
                if (taskId != null && newStatus != null) {
                    try {
                        String json = "{\"status\":\"" + newStatus + "\"}";
                        var res = api.putJson("/api/tasks/" + taskId + "/status", json);
                        info.setText("Changed status: " + res.statusCode());
                        refreshTasks(taskList, info);
                    } catch (Exception ex) {
                        info.setText("Error: " + ex.getMessage());
                    }
                }
            }
        });

        refreshTasks(taskList, info);

        root.setTop(top);
        root.setCenter(taskList);
        root.setBottom(info);

        stage.setScene(new Scene(root, 1000, 550));
        stage.setTitle("Tasks - Group " + groupId);
        stage.show();
    }

    private void refreshTasks(ListView<String> list, Label info) {
        try {
            var res = api.get("/api/tasks/by-group/" + groupId);
            info.setText("Tasks: " + res.statusCode());
            list.getItems().clear();

            boolean hasUpcoming = false;
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            if (res.statusCode() == 200) {
                JSONArray arr = new JSONArray(res.body());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject task = arr.getJSONObject(i);
                    long taskId = task.getLong("taskId");
                    String title = task.optString("title", "");
                    String desc = task.optString("description", "");
                    String deadline = task.optString("deadline", "");
                    String createdAt = task.optString("createdAt", "");
                    String status = task.optString("status", "");
                    String creatorName = "";
                    String creatorEmail = "";
                    if (task.has("createdBy") && !task.isNull("createdBy")) {
                        JSONObject createdBy = task.getJSONObject("createdBy");
                        creatorName = createdBy.optString("name", "");
                        creatorEmail = createdBy.optString("email", "");
                    }

                    // Проверяем приближающийся дедлайн
                    if (!deadline.isEmpty()) {
                        try {
                            LocalDate date = LocalDate.parse(deadline.substring(0, 10));
                            if (!date.isBefore(today) && !date.isAfter(tomorrow)) {
                                hasUpcoming = true;
                            }
                        } catch (DateTimeParseException ignored) {}
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("№").append(taskId);
                    if (!title.isEmpty()) sb.append(" ").append(title);
                    if (!desc.isEmpty()) sb.append(" — ").append(desc);
                    if (!status.isEmpty()) sb.append(" [").append(status).append("]");
                    if (!deadline.isEmpty()) sb.append(" (deadline: ").append(deadline).append(")");
                    if (!creatorName.isEmpty() || !creatorEmail.isEmpty()) {
                        sb.append(" (by: ");
                        if (!creatorName.isEmpty()) sb.append(creatorName);
                        if (!creatorEmail.isEmpty()) sb.append(" <").append(creatorEmail).append(">");
                        sb.append(")");
                    }
                    if (!createdAt.isEmpty()) sb.append(" (created: ").append(createdAt).append(")");

                    list.getItems().add(sb.toString());
                }
            }
            if (hasUpcoming) {
                info.setText(info.getText() + " | Attention: there are tasks with deadlines today/tomorrow!");
            }
        } catch (Exception ex) {
            info.setText("Error: " + ex.getMessage());
        }
    }

    private Long extractTaskId(String displayString) {
        try {
            int start = displayString.indexOf("№") + 1;
            int end = displayString.indexOf(" ", start);
            if (start > 0 && end > start) {
                return Long.parseLong(displayString.substring(start, end));
            } else if (start > 0) {
                int bracket = displayString.indexOf("[", start);
                int dash = displayString.indexOf("—", start);
                int lim = bracket > 0 ? bracket : dash > 0 ? dash : displayString.length();
                return Long.parseLong(displayString.substring(start, lim).trim());
            }
        } catch (Exception e) { }
        return null;
    }

    private String extractStatus(String displayString) {
        int l = displayString.indexOf("[");
        int r = displayString.indexOf("]", l + 1);
        if (l != -1 && r != -1) {
            return displayString.substring(l + 1, r);
        }
        return null;
    }

    private String nextStatus(String status) {
        if ("OPEN".equals(status)) return "IN_PROGRESS";
        if ("IN_PROGRESS".equals(status)) return "DONE";
        if ("DONE".equals(status)) return "OPEN";
        return null;
    }
}
