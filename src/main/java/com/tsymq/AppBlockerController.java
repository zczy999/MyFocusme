package com.tsymq;

import com.tsymq.mode.ModeManager;
import com.tsymq.mode.ModeState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AppBlockerController {

    @FXML
    private TextField inputField;

    @FXML
    private TextArea outputArea;
    
    @FXML
    private Button focusModeButton;
    
    @FXML
    private Label modeStatusLabel;
    
    @FXML
    private VBox normalModePanel;
    
    @FXML
    private VBox focusModePanel;
    
    @FXML
    private Label statsLabel;

    private AppBlocker appBlocker;
    private ModeManager modeManager;

    public void initialize() {
        // 初始化模式管理器
        modeManager = new ModeManager();
        
        // 初始化AppBlocker
        appBlocker = new AppBlocker();
        appBlocker.setModeManager(modeManager);
        appBlocker.loadBlockedWebsites();
        appBlocker.loadwhiteWebsites();

        // 设置模式变更监听器
        modeManager.setModeChangeListener(this::onModeChanged);

        // 启动监控
        appBlocker.monitorActiveEdgeUrl(outputArea);
        
        // 初始化UI状态
        updateUIForCurrentMode();
        
        outputArea.appendText("应用已启动，当前模式: " + modeManager.getCurrentMode() + "\n");
    }
    
    /**
     * 模式变更回调
     */
    private void onModeChanged(ModeState modeState) {
        Platform.runLater(() -> {
            updateUIForCurrentMode();
            outputArea.appendText("模式已切换到: " + modeState.getCurrentMode() + "\n");
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
        }
    }

    @FXML
    void onBlockButtonClicked(ActionEvent event) {
        String itemToBlock = inputField.getText().trim();
        if (!itemToBlock.isEmpty() && appBlocker.block(itemToBlock)) {
            outputArea.appendText("已屏蔽: " + itemToBlock + "\n");
            inputField.clear();
        } else {
            outputArea.appendText("屏蔽失败: " + itemToBlock + "\n");
        }
    }
    
    /**
     * 进入学习模式按钮点击事件
     */
    @FXML
    void onFocusModeButtonClicked(ActionEvent event) {
        modeManager.switchToFocusMode(60);
        outputArea.appendText("已进入学习模式\n");
    }
    
    /**
     * 紧急退出按钮点击事件（保留用于特殊情况）
     */
    @FXML
    void onEmergencyExitClicked(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("紧急退出确认");
        alert.setHeaderText("确定要退出学习模式吗？");
        alert.setContentText("退出后将返回普通模式。");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                modeManager.switchToNormalMode();
                outputArea.appendText("已退出学习模式\n");
            }
        });
    }
    
    /**
     * 获取模式管理器
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
