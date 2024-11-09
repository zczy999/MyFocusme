package com.tsymq;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Timer;
import java.util.TimerTask;


public class AppBlockerController {

    @FXML
    private TextField inputField;

    @FXML
    private Button blockButton;

    @FXML
    private TextArea outputArea;

    private AppBlocker appBlocker;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    private boolean isStartUp = false;

    private Timer timer;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");


    public void initialize() {
        appBlocker = new AppBlocker();
        appBlocker.loadBlockedWebsites(); // 加载已保存的屏蔽网址
        appBlocker.loadwhiteWebsites(); // 加载已保存的屏蔽网址

        appBlocker.monitorActiveEdgeUrl();
        isStartUp = true;
        outputArea.appendText("Started monitoring Microsoft Edge active URLs.\n");

        appBlocker.setOnActiveEdgeUrlChangedListener((url, browser) -> {
            Platform.runLater(() -> {
                //白名单功能
                if (appBlocker.isWhiteWeb(url)){
                    return;
                }

                if (appBlocker.isBlocked(url)) {
                    if (browser.equals("edge")) {
                        outputArea.appendText("The active website in Microsoft Edge is blocked: " + url + "\n");
                        appBlocker.openNewEdgeTab();
                    }
                    if (browser.equals("safari")) {
                        outputArea.appendText("The active website in Safari is blocked: " + url + "\n");
                        appBlocker.openNewSafariTab();
                    }


                } else {
//                    outputArea.appendText("The active website in Microsoft Edge is not blocked: " + url + "\n");
                }
            });
        });
//        appBlocker.monitorActiveEdgeUrl();
        appBlocker.monitorActiveEdgeUrlToClose();
    }

    @FXML
    void onBlockButtonClicked(ActionEvent event) {
        String itemToBlock = inputField.getText().trim();
        if (!itemToBlock.isEmpty() && appBlocker.block(itemToBlock)) {
            outputArea.appendText("Successfully blocked: " + itemToBlock + "\n");
            inputField.clear();
        } else {
            outputArea.appendText("Failed to block: " + itemToBlock + "\n");
        }
    }

    @FXML
    void onStartMonitoringButtonClicked(ActionEvent event) {
        if (isStartUp == true){
            appBlocker.stopMonitoring();
            outputArea.appendText("Stopped monitoring Microsoft Edge active URLs.\n");
            isStartUp = false;
            return;
        }
        appBlocker.monitorActiveEdgeUrl();
        isStartUp = true;
        outputArea.appendText("Started monitoring Microsoft Edge active URLs.\n");
    }

    @FXML
    void onStopMonitoringButtonClicked(ActionEvent event) {
        appBlocker.stopMonitoring();
        outputArea.appendText("Stopped monitoring Microsoft Edge active URLs.\n");
    }

    @FXML
    private void onSetTimerButtonClicked() {
        try {
            LocalTime startTime = LocalTime.parse(startTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = LocalTime.parse(endTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));

            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();

            Duration startDelay = Duration.between(LocalTime.now(), startTime);
            Duration endDelay = Duration.between(LocalTime.now(), endTime);

            TimerTask startTask = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        appBlocker.monitorActiveEdgeUrl();
                        outputArea.appendText("Timer started monitoring\n");
                    });
                }
            };

            TimerTask endTask = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        appBlocker.stopMonitoring();
                        outputArea.appendText("Timer stopped monitoring\n");
                    });
                }
            };

            timer.scheduleAtFixedRate(startTask, startDelay.toMillis(), 24 * 60 * 60 * 1000);
            timer.scheduleAtFixedRate(endTask, endDelay.toMillis(), 24 * 60 * 60 * 1000);
            outputArea.appendText("Timer set\n");
        } catch (DateTimeParseException e) {
            outputArea.appendText("Invalid time format\n");
        }
    }


}
