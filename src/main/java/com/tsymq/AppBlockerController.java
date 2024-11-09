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
    private TextArea outputArea;

    private AppBlocker appBlocker;


    public void initialize() {
        appBlocker = new AppBlocker();
        appBlocker.loadBlockedWebsites(); // 加载已保存的屏蔽网址
        appBlocker.loadwhiteWebsites(); // 加载已保存的屏蔽网址

        appBlocker.monitorActiveEdgeUrl(outputArea);
        outputArea.appendText("Started studying \n");
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


}
