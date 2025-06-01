package com.tsymq.ui;

import com.tsymq.config.AppConfig;
import com.tsymq.mode.ModeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

/**
 * 模式切换对话框控制器
 * 处理时长选择和确认逻辑
 */
public class FocusModeDialogController {
    
    @FXML private Button duration15Button;
    @FXML private Button duration30Button;
    @FXML private Button duration60Button;
    @FXML private Button duration120Button;
    
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private Label selectedDurationLabel;
    
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;
    
    private int selectedDuration = AppConfig.DEFAULT_FOCUS_DURATION_MINUTES;
    private boolean confirmed = false;
    private Stage dialogStage;
    private ModeManager modeManager;
    
    /**
     * 初始化方法
     */
    public void initialize() {
        setupDurationSpinner();
        updateSelectedDurationDisplay();
        
        // 默认选中1小时按钮
        selectPresetButton(duration60Button);
    }
    
    /**
     * 设置模式管理器
     * @param modeManager 模式管理器实例
     */
    public void setModeManager(ModeManager modeManager) {
        this.modeManager = modeManager;
    }
    
    /**
     * 设置时长选择器
     */
    private void setupDurationSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                AppConfig.MIN_FOCUS_DURATION_MINUTES,
                AppConfig.MAX_FOCUS_DURATION_MINUTES,
                AppConfig.DEFAULT_FOCUS_DURATION_MINUTES,
                15 // 步长为15分钟
        );
        
        durationSpinner.setValueFactory(valueFactory);
        
        // 监听spinner值变化
        durationSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selectedDuration = newValue;
                updateSelectedDurationDisplay();
                clearPresetButtonSelection();
            }
        });
    }
    
    /**
     * 预设时长按钮点击事件
     */
    @FXML
    void onPresetDurationClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String durationStr = (String) clickedButton.getUserData();
        
        try {
            int duration = Integer.parseInt(durationStr);
            selectedDuration = duration;
            durationSpinner.getValueFactory().setValue(duration);
            updateSelectedDurationDisplay();
            selectPresetButton(clickedButton);
        } catch (NumberFormatException e) {
            System.err.println("Invalid duration format: " + durationStr);
        }
    }
    
    /**
     * 选中预设按钮
     */
    private void selectPresetButton(Button selectedButton) {
        // 清除所有按钮的选中状态
        clearPresetButtonSelection();
        
        // 设置选中按钮的样式
        selectedButton.getStyleClass().add("selected");
    }
    
    /**
     * 清除预设按钮选中状态
     */
    private void clearPresetButtonSelection() {
        duration15Button.getStyleClass().remove("selected");
        duration30Button.getStyleClass().remove("selected");
        duration60Button.getStyleClass().remove("selected");
        duration120Button.getStyleClass().remove("selected");
    }
    
    /**
     * 更新选中时长显示
     */
    private void updateSelectedDurationDisplay() {
        String displayText;
        if (selectedDuration >= 60) {
            int hours = selectedDuration / 60;
            int minutes = selectedDuration % 60;
            if (minutes == 0) {
                displayText = String.format("已选择: %d小时", hours);
            } else {
                displayText = String.format("已选择: %d小时%d分钟", hours, minutes);
            }
        } else {
            displayText = String.format("已选择: %d分钟", selectedDuration);
        }
        selectedDurationLabel.setText(displayText);
    }
    
    /**
     * 取消按钮点击事件
     */
    @FXML
    void onCancelClicked(ActionEvent event) {
        confirmed = false;
        closeDialog();
    }
    
    /**
     * 确认按钮点击事件
     */
    @FXML
    void onConfirmClicked(ActionEvent event) {
        // 验证选择的时长
        if (selectedDuration < AppConfig.MIN_FOCUS_DURATION_MINUTES || 
            selectedDuration > AppConfig.MAX_FOCUS_DURATION_MINUTES) {
            
            // 显示错误提示
            showErrorMessage("请选择有效的学习时长（" + 
                           AppConfig.MIN_FOCUS_DURATION_MINUTES + "-" + 
                           AppConfig.MAX_FOCUS_DURATION_MINUTES + "分钟）");
            return;
        }
        
        // 尝试切换到学习模式
        if (modeManager != null) {
            boolean success = modeManager.switchToFocusMode(selectedDuration);
            if (success) {
                confirmed = true;
                closeDialog();
            } else {
                showErrorMessage("无法切换到学习模式，请稍后重试");
            }
        } else {
            showErrorMessage("模式管理器未初始化");
        }
    }
    
    /**
     * 显示错误消息
     */
    private void showErrorMessage(String message) {
        // 这里可以显示一个简单的错误提示
        // 为了简化，暂时在控制台输出
        System.err.println("Error: " + message);
        
        // 可以考虑添加一个错误标签或者弹出提示框
        selectedDurationLabel.setText("❌ " + message);
        selectedDurationLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        // 2秒后恢复正常显示
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(2),
                e -> {
                    updateSelectedDurationDisplay();
                    selectedDurationLabel.setStyle("");
                }
            )
        );
        timeline.play();
    }
    
    /**
     * 关闭对话框
     */
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    /**
     * 设置对话框Stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * 获取选中的时长
     */
    public int getSelectedDuration() {
        return selectedDuration;
    }
    
    /**
     * 检查是否确认
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * 设置默认时长
     */
    public void setDefaultDuration(int duration) {
        if (duration >= AppConfig.MIN_FOCUS_DURATION_MINUTES && 
            duration <= AppConfig.MAX_FOCUS_DURATION_MINUTES) {
            this.selectedDuration = duration;
            durationSpinner.getValueFactory().setValue(duration);
            updateSelectedDurationDisplay();
            
            // 如果是预设值，选中对应按钮
            switch (duration) {
                case 15:
                    selectPresetButton(duration15Button);
                    break;
                case 30:
                    selectPresetButton(duration30Button);
                    break;
                case 60:
                    selectPresetButton(duration60Button);
                    break;
                case 120:
                    selectPresetButton(duration120Button);
                    break;
                default:
                    clearPresetButtonSelection();
                    break;
            }
        }
    }
} 