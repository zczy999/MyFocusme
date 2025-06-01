package com.tsymq;

import com.tsymq.mode.ModeManager;
import com.tsymq.mode.ModeState;
import com.tsymq.ui.FocusModeDialogController;
import com.tsymq.utils.TimeUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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
    
    // 新增UI元素
    @FXML
    private Button focusModeButton;
    
    @FXML
    private Label modeStatusLabel;
    
    @FXML
    private Label countdownLabel;
    
    @FXML
    private VBox normalModePanel;
    
    @FXML
    private VBox focusModePanel;
    
    @FXML
    private VBox countdownPanel;
    
    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private Label progressLabel;
    
    @FXML
    private Label motivationLabel;
    
    @FXML
    private Label focusDurationLabel;
    
    @FXML
    private Label focusStartTimeLabel;
    
    @FXML
    private Button emergencyExitButton;
    
    @FXML
    private Label statsLabel;

    private AppBlocker appBlocker;
    private ModeManager modeManager;


    public void initialize() {
        // 初始化模式管理器
        modeManager = new ModeManager();
        
        // 初始化AppBlocker
        appBlocker = new AppBlocker();
        appBlocker.setModeManager(modeManager); // 注入模式管理器
        appBlocker.loadBlockedWebsites(); // 加载已保存的屏蔽网址
        appBlocker.loadwhiteWebsites(); // 加载已保存的白名单网址

        // 设置模式变更监听器
        modeManager.setModeChangeListener(this::onModeChanged);
        
        // 设置时间更新监听器
        modeManager.setTimeUpdateListener(this::onTimeUpdated);

        // 启动监控
        appBlocker.monitorActiveEdgeUrl(outputArea);
        
        // 初始化UI状态
        updateUIForCurrentMode();
        
        outputArea.appendText("应用已启动，当前模式: " + modeManager.getCurrentMode() + "\n");
    }
    
    /**
     * 模式变更回调
     * @param modeState 新的模式状态
     */
    private void onModeChanged(ModeState modeState) {
        Platform.runLater(() -> {
            updateUIForCurrentMode();
            outputArea.appendText("模式已切换到: " + modeState.getCurrentMode() + "\n");
        });
    }
    
    /**
     * 时间更新回调
     * @param remainingTimeMs 剩余时间（毫秒）
     */
    private void onTimeUpdated(Long remainingTimeMs) {
        Platform.runLater(() -> {
            if (modeManager.isInFocusMode()) {
                updateCountdownDisplay(remainingTimeMs);
                updateProgressDisplay();
            }
        });
    }
    
    /**
     * 根据当前模式更新UI
     */
    private void updateUIForCurrentMode() {
        ModeState.Mode currentMode = modeManager.getCurrentMode();
        
        if (currentMode == ModeState.Mode.NORMAL) {
            // 普通模式UI
            modeStatusLabel.setText("普通模式");
            modeStatusLabel.getStyleClass().removeAll("focus-mode-status");
            modeStatusLabel.getStyleClass().add("normal-mode-status");
            
            normalModePanel.setVisible(true);
            normalModePanel.setManaged(true);
            
            focusModePanel.setVisible(false);
            focusModePanel.setManaged(false);
            
            countdownPanel.setVisible(false);
            countdownPanel.setManaged(false);
            
            focusModeButton.setDisable(false);
            
        } else if (currentMode == ModeState.Mode.FOCUS) {
            // 学习模式UI
            modeStatusLabel.setText("学习模式");
            modeStatusLabel.getStyleClass().removeAll("normal-mode-status");
            modeStatusLabel.getStyleClass().add("focus-mode-status");
            
            normalModePanel.setVisible(false);
            normalModePanel.setManaged(false);
            
            focusModePanel.setVisible(true);
            focusModePanel.setManaged(true);
            
            countdownPanel.setVisible(true);
            countdownPanel.setManaged(true);
            
            // 更新学习模式信息
            updateFocusModeInfo();
            
            // 显示紧急退出按钮（延迟5秒后显示）
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        emergencyExitButton.setVisible(true);
                    });
                }
            }, 5000);
        }
    }
    
    /**
     * 更新倒计时显示
     * @param remainingTimeMs 剩余时间（毫秒）
     */
    private void updateCountdownDisplay(long remainingTimeMs) {
        String formattedTime = TimeUtils.formatDuration(remainingTimeMs);
        countdownLabel.setText(formattedTime);
    }
    
    /**
     * 更新进度显示
     */
    private void updateProgressDisplay() {
        if (modeManager.isInFocusMode()) {
            double progress = modeManager.getProgressPercentage();
            progressBar.setProgress(progress / 100.0);
            progressLabel.setText(String.format("%.1f%%", progress));
        }
    }
    
    /**
     * 更新学习模式信息
     */
    private void updateFocusModeInfo() {
        if (modeManager.isInFocusMode()) {
            int duration = modeManager.getFocusModeDurationMinutes();
            focusDurationLabel.setText("学习时长: " + duration + "分钟");
            
            // 计算开始时间
            long elapsedMs = modeManager.getElapsedTimeMs();
            long startTimeMs = System.currentTimeMillis() - elapsedMs;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            focusStartTimeLabel.setText("开始时间: " + sdf.format(startTimeMs));
            
            // 更新激励文案
            updateMotivationText(duration);
        }
    }
    
    /**
     * 更新激励文案
     * @param durationMinutes 学习时长
     */
    private void updateMotivationText(int durationMinutes) {
        String[] motivations = {
            "保持专注，你正在变得更好！",
            "每一分钟的专注都是对未来的投资！",
            "专注是成功的关键，坚持下去！",
            "你的努力会得到回报的！",
            "专注学习，成就更好的自己！"
        };
        
        int index = (int) (Math.random() * motivations.length);
        motivationLabel.setText(motivations[index]);
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
    
    /**
     * 进入学习模式按钮点击事件
     */
    @FXML
    void onFocusModeButtonClicked(ActionEvent event) {
        try {
            // 加载模式切换对话框
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FocusModeDialog.fxml"));
            Parent root = loader.load();
            
            FocusModeDialogController dialogController = loader.getController();
            dialogController.setModeManager(modeManager);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("进入学习模式");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            
            // 获取当前窗口并设置对话框位置
            Stage currentStage = (Stage) focusModeButton.getScene().getWindow();
            dialogStage.initOwner(currentStage);
            
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            outputArea.appendText("无法打开模式切换对话框: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    
    /**
     * 紧急退出按钮点击事件
     */
    @FXML
    void onEmergencyExitClicked(ActionEvent event) {
        // 创建确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("紧急退出确认");
        alert.setHeaderText("确定要退出学习模式吗？");
        alert.setContentText("退出后将返回普通模式，当前的学习进度将会丢失。");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                modeManager.switchToNormalMode();
                outputArea.appendText("已紧急退出学习模式\n");
            }
        });
    }
    
    /**
     * 获取模式管理器（供外部访问）
     * @return 模式管理器实例
     */
    public ModeManager getModeManager() {
        return modeManager;
    }
    
    /**
     * 应用关闭时的清理工作
     */
    public void shutdown() {
        if (appBlocker != null) {
            appBlocker.stop();
        }
        if (modeManager != null) {
            modeManager.shutdown();
        }
    }


}
