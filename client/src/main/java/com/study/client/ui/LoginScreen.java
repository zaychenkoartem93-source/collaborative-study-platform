package com.study.client.ui;

import com.study.client.api.ApiClient;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginScreen {

    private final Stage stage;
    private final ApiClient api;
    private final Runnable onLoggedIn;

    public LoginScreen(Stage stage, ApiClient api, Runnable onLoggedIn) {
        this.stage = stage;
        this.api = api;
        this.onLoggedIn = onLoggedIn;
    }

    public void show() {
        GridPane root = new GridPane();
        root.setPadding(new Insets(16));
        root.setHgap(8);
        root.setVgap(8);

        TextField email = new TextField();
        PasswordField pass = new PasswordField();
        TextField name = new TextField();

        Button btnLogin = new Button("Login");
        Button btnRegister = new Button("Register");
        Label status = new Label();

        root.add(new Label("Email:"), 0, 0);
        root.add(email, 1, 0);
        root.add(new Label("Password:"), 0, 1);
        root.add(pass, 1, 1);
        root.add(btnLogin, 1, 2);
        root.add(new Separator(), 0, 3, 2, 1);
        root.add(new Label("Name (for register):"), 0, 4);
        root.add(name, 1, 4);
        root.add(btnRegister, 1, 5);
        root.add(status, 1, 6);

        btnLogin.setOnAction(e -> {
            try {

                String json = String.format(
                        "{\"email\":\"%s\", \"password\":\"%s\"}",
                        email.getText(),
                        pass.getText()
                );

                System.out.println("SENT JSON = " + json);

                var res = api.postJson("/api/auth/login", json);

                System.out.println("LOGIN STATUS = " + res.statusCode());
                System.out.println("LOGIN BODY   = " + res.body());

                status.setText("Login: " + res.statusCode());

                if (res.statusCode() == 200) {
                    onLoggedIn.run();
                }

            } catch (Exception ex) {
                status.setText("Error: " + ex.getMessage());
            }
        });


        btnRegister.setOnAction(e -> {
            try {
                String json = String.format(
                        "{\"name\":\"%s\", \"email\":\"%s\", \"password\":\"%s\"}",
                        name.getText(),
                        email.getText(),
                        pass.getText()
                );

                var res = api.postJson("/api/auth/register", json);

                if (res.statusCode() == 200) {
                    status.setText("Registered successfully!");
                } else {
                    status.setText("Error: " + res.body());
                }

            } catch (Exception ex) {
                status.setText("Error: " + ex.getMessage());
            }
        });



        stage.setScene(new Scene(root, 420, 260));
        stage.setTitle("Login");
        stage.show();
    }

}
