package com.tsymq;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        Platform.setImplicitExit(false);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/AppBlocker.fxml"));
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("App Blocker");
        primaryStage.setScene(new Scene(root, 600, 500));

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
        });

        createTrayIcon();
//        primaryStage.show();
    }

    private void createTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported");
            return;
        }

        SystemTray systemTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/myfocus.png")); // Replace with the path to your icon file
        TrayIcon trayIcon = new TrayIcon(image, "MyFocus");

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Platform.runLater(() -> {
                    if (primaryStage.isShowing()) {
                        primaryStage.hide();
                    } else {
                        primaryStage.show();
                    }
                });
            }
        });

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("Unable to add system tray icon");
            e.printStackTrace();
        }
    }
}
