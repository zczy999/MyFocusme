package com.tsymq.ui;

import com.tsymq.mode.ModeState;
import com.tsymq.utils.TimeUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * UI状态管理类
 * 负责根据模式切换UI显示状态
 */
public class UIStateManager {
    
    // UI元素引用
    private Label modeStatusLabel;
    private VBox countdownPanel;
    private Label countdownLabel;
    private ProgressBar progressBar;
    private Label progressLabel;
    
    private VBox normalModePanel;
    private VBox focusModePanel;
    private Button focusModeButton;
    private Button emergencyExitButton;
    
    private Label motivationLabel;
    private Label focusDurationLabel;
    private Label focusStartTimeLabel;
    private Label statsLabel;
    
    // 动画和定时器
    private Timeline breathingAnimation;
    private Timeline motivationUpdateTimer;
    
    // 激励文案数组
    private static final String[] MOTIVATION_QUOTES = {
        "保持专注，你正在变得更好！",
        "每一分钟的专注都是对未来的投资",
        "专注是成功的关键，坚持下去！",
        "你的努力正在塑造更好的自己",
        "专注当下，成就未来",
        "深度工作，深度成长",
        "专注力是你最宝贵的资源",
        "每一次专注都是一次自我超越"
    };
    
    private int currentMotivationIndex = 0;
    
    /**
     * 初始化UI状态管理器
     */
    public void initialize(
            Label modeStatusLabel,
            VBox countdownPanel,
            Label countdownLabel,
            ProgressBar progressBar,
            Label progressLabel,
            VBox normalModePanel,
            VBox focusModePanel,
            Button focusModeButton,
            Button emergencyExitButton,
            Label motivationLabel,
            Label focusDurationLabel,
            Label focusStartTimeLabel,
            Label statsLabel) {
        
        this.modeStatusLabel = modeStatusLabel;
        this.countdownPanel = countdownPanel;
        this.countdownLabel = countdownLabel;
        this.progressBar = progressBar;
        this.progressLabel = progressLabel;
        this.normalModePanel = normalModePanel;
        this.focusModePanel = focusModePanel;
        this.focusModeButton = focusModeButton;
        this.emergencyExitButton = emergencyExitButton;
        this.motivationLabel = motivationLabel;
        this.focusDurationLabel = focusDurationLabel;
        this.focusStartTimeLabel = focusStartTimeLabel;
        this.statsLabel = statsLabel;
        
        setupAnimations();
    }
    
    /**
     * 设置动画效果
     */
    private void setupAnimations() {
        // 呼吸动画效果（学习模式下的倒计时）
        breathingAnimation = new Timeline(
            new KeyFrame(Duration.seconds(0), e -> {
                if (countdownLabel != null) {
                    countdownLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 20, 0, 0, 0);");
                }
            }),
            new KeyFrame(Duration.seconds(1), e -> {
                if (countdownLabel != null) {
                    countdownLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.3), 5, 0, 0, 0);");
                }
            })
        );
        breathingAnimation.setCycleCount(Timeline.INDEFINITE);
        breathingAnimation.setAutoReverse(true);
        
        // 激励文案更新定时器（每30秒更换一次）
        motivationUpdateTimer = new Timeline(
            new KeyFrame(Duration.seconds(30), e -> updateMotivationText())
        );
        motivationUpdateTimer.setCycleCount(Timeline.INDEFINITE);
    }
    
    /**
     * 更新UI状态以匹配当前模式
     */
    public void updateUIForMode(ModeState modeState) {
        Platform.runLater(() -> {
            switch (modeState.getCurrentMode()) {
                case NORMAL:
                    showNormalMode();
                    break;
                case FOCUS:
                    showFocusMode(modeState);
                    break;
            }
        });
    }
    
    /**
     * 显示普通模式UI
     */
    private void showNormalMode() {
        // 更新模式状态标签
        modeStatusLabel.setText("普通模式");
        modeStatusLabel.setStyle("-fx-text-fill: #27ae60;");
        
        // 隐藏倒计时面板
        countdownPanel.setVisible(false);
        countdownPanel.setManaged(false);
        
        // 显示普通模式面板，隐藏学习模式面板
        normalModePanel.setVisible(true);
        normalModePanel.setManaged(true);
        focusModePanel.setVisible(false);
        focusModePanel.setManaged(false);
        
        // 显示进入学习模式按钮
        focusModeButton.setVisible(true);
        focusModeButton.setDisable(false);
        
        // 停止动画
        stopAnimations();
        
        System.out.println("UI switched to normal mode");
    }
    
    /**
     * 显示学习模式UI
     */
    private void showFocusMode(ModeState modeState) {
        // 更新模式状态标签
        modeStatusLabel.setText("学习模式");
        modeStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        // 显示倒计时面板
        countdownPanel.setVisible(true);
        countdownPanel.setManaged(true);
        
        // 隐藏普通模式面板，显示学习模式面板
        normalModePanel.setVisible(false);
        normalModePanel.setManaged(false);
        focusModePanel.setVisible(true);
        focusModePanel.setManaged(true);
        
        // 隐藏进入学习模式按钮
        focusModeButton.setVisible(false);
        
        // 更新学习模式信息
        updateFocusModeInfo(modeState);
        
        // 启动动画
        startAnimations();
        
        System.out.println("UI switched to focus mode");
    }
    
    /**
     * 更新学习模式信息
     */
    private void updateFocusModeInfo(ModeState modeState) {
        // 更新学习时长显示
        int duration = modeState.getFocusDurationMinutes();
        String durationText;
        if (duration >= 60) {
            int hours = duration / 60;
            int minutes = duration % 60;
            if (minutes == 0) {
                durationText = String.format("学习时长: %d小时", hours);
            } else {
                durationText = String.format("学习时长: %d小时%d分钟", hours, minutes);
            }
        } else {
            durationText = String.format("学习时长: %d分钟", duration);
        }
        focusDurationLabel.setText(durationText);
        
        // 更新开始时间显示
        LocalDateTime startTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(modeState.getModeStartTime()),
            java.time.ZoneId.systemDefault()
        );
        String startTimeText = "开始时间: " + startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        focusStartTimeLabel.setText(startTimeText);
        
        // 更新激励文案
        updateMotivationText();
    }
    
    /**
     * 更新倒计时显示
     */
    public void updateCountdown(long remainingTimeMs, double progressPercentage) {
        Platform.runLater(() -> {
            // 更新倒计时文本
            String timeText = TimeUtils.formatDuration(remainingTimeMs);
            countdownLabel.setText(timeText);
            
            // 更新进度条
            progressBar.setProgress(progressPercentage / 100.0);
            
            // 更新进度文本
            progressLabel.setText(String.format("%.1f%%", progressPercentage));
            
            // 根据剩余时间调整颜色
            if (remainingTimeMs < 5 * 60 * 1000) { // 最后5分钟
                countdownLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else if (remainingTimeMs < 15 * 60 * 1000) { // 最后15分钟
                countdownLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            } else {
                countdownLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            }
        });
    }
    
    /**
     * 更新激励文案
     */
    private void updateMotivationText() {
        if (motivationLabel != null) {
            String newText = MOTIVATION_QUOTES[currentMotivationIndex];
            motivationLabel.setText(newText);
            currentMotivationIndex = (currentMotivationIndex + 1) % MOTIVATION_QUOTES.length;
        }
    }
    
    /**
     * 更新使用统计
     */
    public void updateStats(String statsText) {
        Platform.runLater(() -> {
            if (statsLabel != null) {
                statsLabel.setText(statsText);
            }
        });
    }
    
    /**
     * 显示紧急退出按钮
     */
    public void showEmergencyExitButton() {
        Platform.runLater(() -> {
            if (emergencyExitButton != null) {
                emergencyExitButton.setVisible(true);
            }
        });
    }
    
    /**
     * 隐藏紧急退出按钮
     */
    public void hideEmergencyExitButton() {
        Platform.runLater(() -> {
            if (emergencyExitButton != null) {
                emergencyExitButton.setVisible(false);
            }
        });
    }
    
    /**
     * 启动动画
     */
    private void startAnimations() {
        if (breathingAnimation != null) {
            breathingAnimation.play();
        }
        if (motivationUpdateTimer != null) {
            motivationUpdateTimer.play();
        }
    }
    
    /**
     * 停止动画
     */
    private void stopAnimations() {
        if (breathingAnimation != null) {
            breathingAnimation.stop();
        }
        if (motivationUpdateTimer != null) {
            motivationUpdateTimer.stop();
        }
    }
    
    /**
     * 显示学习完成提示
     */
    public void showFocusCompleteMessage() {
        Platform.runLater(() -> {
            // 可以在这里添加学习完成的特殊UI效果
            modeStatusLabel.setText("学习完成！");
            modeStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            
            if (motivationLabel != null) {
                motivationLabel.setText("🎉 恭喜！你成功完成了这次专注学习！");
                motivationLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        stopAnimations();
        
        if (breathingAnimation != null) {
            breathingAnimation = null;
        }
        if (motivationUpdateTimer != null) {
            motivationUpdateTimer = null;
        }
    }
} 