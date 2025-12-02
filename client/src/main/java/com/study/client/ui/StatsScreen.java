package com.study.client.ui;

import com.study.client.api.ApiClient;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONObject;

public class StatsScreen {

    private final Stage stage;
    private final ApiClient api;
    private final Long groupId;

    public StatsScreen(Stage stage, ApiClient api, Long groupId) {
        this.stage = stage;
        this.api = api;
        this.groupId = groupId;
    }

    public void show() {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        // --- Controls ---
        Button load = new Button("Load Stats");
        Label statusLabel = new Label("Stats for Group " + groupId);

        HBox top = new HBox(10, load, statusLabel);
        top.setPadding(new Insets(8));

        // --- Charts ---
        VBox chartsBox = new VBox(20);
        chartsBox.setPadding(new Insets(10));

        PieChart tasksPie = new PieChart();
        tasksPie.setTitle("Tasks by Status");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> tasksByUserBar = new BarChart<>(xAxis, yAxis);
        tasksByUserBar.setTitle("Tasks by Author");
        xAxis.setLabel("User");
        yAxis.setLabel("Tasks");

        // --- Summary Panel ---
        VBox summaryBox = new VBox(8);
        summaryBox.setPadding(new Insets(10));
        summaryBox.setStyle("-fx-background-color: #f2f2f2; -fx-padding: 10; -fx-border-color: #ccc");

        Label totalTasksLabel = new Label("Total Tasks: -");
        Label doneTasksLabel = new Label("Completed Tasks: -");
        Label inProgressLabel = new Label("In Progress: -");
        Label openLabel = new Label("Open: -");
        Label resourcesLabel = new Label("Resources Count: -");
        Label mostActiveLabel = new Label("Most Active Member: -");

        summaryBox.getChildren().addAll(
                totalTasksLabel,
                doneTasksLabel,
                inProgressLabel,
                openLabel,
                resourcesLabel,
                mostActiveLabel
        );

        chartsBox.getChildren().addAll(tasksPie, tasksByUserBar, summaryBox);

        // --- Load Button Action ---
        load.setOnAction(e -> {
            try {
                var res = api.get("/api/stats/group/" + groupId);
                statusLabel.setText("Status: " + res.statusCode());

                if (res.statusCode() != 200) return;

                JSONObject json = new JSONObject(res.body());

                // tasksByStatus
                JSONObject byStatus = json.getJSONObject("tasksByStatus");
                tasksPie.getData().clear();

                long done = byStatus.getLong("DONE");
                long inProg = byStatus.getLong("IN_PROGRESS");
                long open = byStatus.getLong("OPEN");

                tasksPie.getData().add(new PieChart.Data("DONE", done));
                tasksPie.getData().add(new PieChart.Data("IN_PROGRESS", inProg));
                tasksPie.getData().add(new PieChart.Data("OPEN", open));

                // Bar chart
                JSONObject byAuthor = json.getJSONObject("tasksByAuthor");
                tasksByUserBar.getData().clear();
                XYChart.Series<String, Number> series = new XYChart.Series<>();

                String mostActive = "-";
                long maxCount = 0;

                for (String user : byAuthor.keySet()) {
                    long count = byAuthor.getLong(user);
                    series.getData().add(new XYChart.Data<>(user, count));

                    if (count > maxCount) {
                        maxCount = count;
                        mostActive = user;
                    }
                }
                tasksByUserBar.getData().add(series);

                // Summary
                long totalTasks = done + inProg + open;
                totalTasksLabel.setText("Total Tasks: " + totalTasks);
                doneTasksLabel.setText("Completed Tasks: " + done);
                inProgressLabel.setText("In Progress: " + inProg);
                openLabel.setText("Open: " + open);

                long resources = json.getLong("resourcesCount");
                resourcesLabel.setText("Resources Count: " + resources);

                mostActiveLabel.setText("Most Active Member: " + mostActive);

            } catch (Exception ex) {
                statusLabel.setText("Error loading stats");
            }
        });

        root.setTop(top);
        root.setCenter(new ScrollPane(chartsBox));

        stage.setScene(new Scene(root, 800, 700));
        stage.setTitle("Statistics - Group " + groupId);
        stage.show();
    }
}
