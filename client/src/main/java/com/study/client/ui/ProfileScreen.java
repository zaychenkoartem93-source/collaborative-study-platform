package com.study.client.ui;

import com.study.client.api.ApiClient;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

public class ProfileScreen {

    private final Stage stage;
    private final ApiClient api;

    public ProfileScreen(Stage stage, ApiClient api) {
        this.stage = stage;
        this.api = api;
    }

    public void show() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(16));

        Label title = new Label("User Profile");
        title.getStyleClass().add("title");

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(16));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone (optional)");
        PasswordField oldPassField = new PasswordField();
        oldPassField.setPromptText("Old Password");
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("New Password (min 6 chars)");

        Button saveProfileBtn = new Button("Save Profile");
        Button changePassBtn = new Button("Change Password");
        Button backBtn = new Button("Back to Main");

        // Размещаем элементы формы
        form.add(new Label("Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Email:"), 0, 1);
        form.add(emailField, 1, 1);
        form.add(new Label("Phone:"), 0, 2);
        form.add(phoneField, 1, 2);

        form.add(new Separator(), 0, 3, 2, 1);

        HBox passSection = new HBox(8, oldPassField, newPassField, changePassBtn);
        form.add(new Label("Change Password:"), 0, 4);
        form.add(passSection, 1, 4);

        HBox buttons = new HBox(8, saveProfileBtn, backBtn);
        form.add(buttons, 0, 5, 2, 1);

        Label status = new Label();
        root.getChildren().addAll(title, form, status);

        loadProfile(nameField, emailField, phoneField, status);

        saveProfileBtn.setOnAction(e -> updateProfile(nameField, emailField, phoneField, status));
        changePassBtn.setOnAction(e -> changePassword(oldPassField, newPassField, status));
        backBtn.setOnAction(e -> new MainScreen(stage, api).show());

        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("User Profile");
        stage.show();
    }


    private void loadProfile(TextField nameField, TextField emailField, TextField phoneField, Label status) {
        try {
            var res = api.get("/api/user/profile");
            if (res.statusCode() == 200) {
                JSONObject user = new JSONObject(res.body());
                nameField.setText(user.optString("name", ""));
                emailField.setText(user.optString("email", ""));
                phoneField.setText(user.optString("phone", ""));  // === Добавлено загрузка phone ===
            } else {
                status.setText("Failed to load profile: " + res.statusCode());
            }
        } catch (Exception ex) {
            status.setText("Failed to load profile: " + ex.getMessage());
        }
    }


    private void updateProfile(TextField nameField, TextField emailField, TextField phoneField, Label status) {
        try {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty()) {
                status.setText("Name cannot be empty");
                return;
            }
            if (email.isEmpty() || !email.contains("@")) {
                status.setText("Please enter a valid email address");
                return;
            }

            String json = String.format(
                    "{\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"}",
                    name, email, phone.isEmpty() ? "" : phone
            );
            var res = api.postJson("/api/user/profile", json);

            if (res.statusCode() == 200) {
                status.setText("Profile updated successfully!");
            } else if (res.statusCode() == 400) {
                status.setText("Validation error: " + res.body());
            } else if (res.statusCode() == 409) {
                status.setText("Email already in use. Please choose another.");
            } else {
                status.setText("Error updating profile: " + res.statusCode() + " " + res.body());
            }
        } catch (Exception ex) {
            status.setText("Error: " + ex.getMessage());
        }
    }


    private void changePassword(PasswordField oldPassField, PasswordField newPassField, Label status) {
        try {
            String oldPass = oldPassField.getText();
            String newPass = newPassField.getText();

            if (newPass.length() < 6) {
                status.setText("Password must be at least 6 characters");
                return;
            }

            if (oldPass.isEmpty()) {
                status.setText("Old password is required");
                return;
            }

            if (newPass.isEmpty()) {
                status.setText("New password cannot be empty");
                return;
            }

            String json = String.format("{\"oldPassword\":\"%s\",\"newPassword\":\"%s\"}", oldPass, newPass);
            var res = api.postJson("/api/user/change-password", json);

            if (res.statusCode() == 200) {
                status.setText("Password changed successfully!");
                oldPassField.clear();
                newPassField.clear();
            } else if (res.statusCode() == 401) {
                status.setText("Old password is incorrect");
                oldPassField.selectAll();
                oldPassField.requestFocus();
            } else if (res.statusCode() == 400) {
                status.setText("Invalid passwords. Please check your input.");
            } else {
                status.setText("Error changing password: " + res.body());
            }
        } catch (Exception ex) {
            status.setText("Error: " + ex.getMessage());
        }
    }
}
