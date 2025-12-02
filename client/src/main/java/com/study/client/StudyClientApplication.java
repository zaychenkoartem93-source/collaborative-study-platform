package com.study.client;

import com.study.client.api.ApiClient;
import com.study.client.ui.LoginScreen;
import com.study.client.ui.MainScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class StudyClientApplication extends Application {

    @Override
    public void start(Stage stage) {
        ApiClient api = new ApiClient("http://localhost:8080");

        Runnable openMain = () -> {
            stage.close();
            new MainScreen(new Stage(), api).show();
        };
        new LoginScreen(stage, api, openMain).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
