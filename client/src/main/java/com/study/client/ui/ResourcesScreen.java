package com.study.client.ui;

import com.study.client.api.ApiClient;
import com.study.client.api.ResourceDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ResourcesScreen {

    private final Stage stage;
    private final ApiClient api;
    private final Long groupId;

    private final ObjectMapper mapper = new ObjectMapper();
    private List<ResourceDto> lastResources = new ArrayList<>();

    private ListView<String> resourceList;
    private Label infoLabel;

    public ResourcesScreen(Stage stage, ApiClient api, Long groupId) {
        this.stage = stage;
        this.api = api;
        this.groupId = groupId;
    }

    public void show() {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        resourceList = new ListView<>(FXCollections.observableArrayList());

        TextField title = new TextField();
        TextField url = new TextField();

        Button addLink = new Button("Add Link");
        Button addFile = new Button("Upload File");
        Button downloadFile = new Button("Download File");
        Button copyLink = new Button("Copy Link");
        Button delete = new Button("Delete");
        Button refresh = new Button("Refresh");

        infoLabel = new Label("Group ID: " + groupId);

        FileChooser chooser = new FileChooser();

        HBox top = new HBox(8,
                new Label("Title:"), title,
                new Label("URL:"), url,
                addLink,
                addFile,
                downloadFile,
                copyLink,
                delete,
                refresh
        );
        top.setPadding(new Insets(8));

        addLink.setOnAction(e -> {
            try {
                ObjectNode reqObj = mapper.createObjectNode();
                reqObj.put("groupId", groupId);
                reqObj.put("title", title.getText());
                reqObj.put("type", "LINK");
                reqObj.put("pathOrUrl", url.getText());

                var res = api.postJson("/api/resources", mapper.writeValueAsString(reqObj));
                infoLabel.setText("Added link: " + res.statusCode());

                title.clear();
                url.clear();
                refreshResources();

            } catch (Exception ex) {
                infoLabel.setText("Error: " + ex.getMessage());
            }
        });

        addFile.setOnAction(e -> {
            File file = chooser.showOpenDialog(stage);
            if (file == null) {
                infoLabel.setText("No file selected.");
                return;
            }

            try {
                String fileTitle = title.getText().isBlank()
                        ? file.getName()
                        : title.getText();

                var res = api.uploadFile("/api/resources/upload", file, fileTitle, groupId);

                infoLabel.setText("Uploaded: " + res.statusCode());
                title.clear();
                refreshResources();

            } catch (Exception ex) {
                infoLabel.setText("Upload error: " + ex.getMessage());
            }
        });

        downloadFile.setOnAction(e -> downloadSelectedFile());
        copyLink.setOnAction(e -> copySelectedLink());
        delete.setOnAction(e -> deleteSelectedResource());
        resourceList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleDoubleClick();
            }
        });

        resourceList.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                copySelectedLink();
            }
        });

        refresh.setOnAction(e -> refreshResources());
        refreshResources();

        root.setTop(top);
        root.setCenter(resourceList);
        root.setBottom(infoLabel);

        stage.setScene(new Scene(root, 900, 500));
        stage.setTitle("Resources - Group " + groupId);
        stage.show();
    }

    private void handleDoubleClick() {
        int index = resourceList.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        ResourceDto r = lastResources.get(index);

        try {
            if ("LINK".equals(r.type)) {
                Desktop.getDesktop().browse(new URI(r.pathOrUrl));
                infoLabel.setText("Opened link");
            } else if ("FILE".equals(r.type)) {
                api.downloadFile(r.resourceId);
                infoLabel.setText("Downloading file...");
            }
        } catch (Exception e) {
            infoLabel.setText("Error: " + e.getMessage());
        }
    }

    private void downloadSelectedFile() {
        int index = resourceList.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        ResourceDto r = lastResources.get(index);

        if (!"FILE".equals(r.type)) {
            infoLabel.setText("Selected item is not a file.");
            return;
        }

        try {
            api.downloadFile(r.resourceId);
            infoLabel.setText("Downloading file...");
        } catch (Exception ex) {
            infoLabel.setText("Download error: " + ex.getMessage());
        }
    }

    private void copySelectedLink() {
        int index = resourceList.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        ResourceDto r = lastResources.get(index);

        if (!"LINK".equals(r.type)) {
            infoLabel.setText("Selected item is not a link.");
            return;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(r.pathOrUrl);
        Clipboard.getSystemClipboard().setContent(content);

        infoLabel.setText("Copied link ✔");
    }

    private void deleteSelectedResource() {
        int index = resourceList.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            infoLabel.setText("No item selected.");
            return;
        }

        ResourceDto r = lastResources.get(index);

        try {
            var res = api.delete("/api/resources/" + r.resourceId);

            if (res.statusCode() == 200) {
                infoLabel.setText("Deleted: " + r.title);
                refreshResources();
            } else {
                infoLabel.setText("Delete failed: " + res.statusCode());
            }
        } catch (Exception ex) {
            infoLabel.setText("Error deleting: " + ex.getMessage());
        }
    }


    private void refreshResources() {
        refreshResources(resourceList, infoLabel);
    }

    private void refreshResources(ListView<String> list, Label info) {
        try {
            var res = api.get("/api/resources/by-group/" + groupId);
            info.setText("Resources loaded: " + res.statusCode());

            list.getItems().clear();

            var type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, ResourceDto.class);

            lastResources = mapper.readValue(res.body(), type);

            for (var r : lastResources) {
                String icon = "FILE".equals(r.type) ? "📄" : "🔗";
                list.getItems().add(icon + " " + r.title + " | " + r.type);
            }

        } catch (Exception ex) {
            info.setText("Error: " + ex.getMessage());
        }
    }
}
