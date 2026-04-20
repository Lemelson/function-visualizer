package com.lemelson.visualizer.app;

import com.lemelson.visualizer.ui.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private MainController controller;

    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new MainController();
        Scene scene = new Scene(controller.getRoot(), 1280, 860);
        String css = getClass().getResource("/com/lemelson/visualizer/style.css").toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.setTitle("Function Visualizer — Интерактивный визуализатор функций");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();
        controller.bootstrap();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }
}
