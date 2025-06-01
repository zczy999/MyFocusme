package com.tsymq.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

/**
 * 通知管理类
 * 负责系统通知和用户提醒
 */
public class NotificationManager {
    
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private boolean systemTraySupported;
    
    // 应用内通知相关
    private VBox notificationContainer;
    private Stage primaryStage;
    
    public NotificationManager() {
        initializeSystemTray();
    }
    
    /**
     * 初始化系统托盘
     */
    private void initializeSystemTray() {
        systemTraySupported = SystemTray.isSupported();
        
        if (systemTraySupported) {
            systemTray = SystemTray.getSystemTray();
            
            // 创建托盘图标（这里使用默认图标，实际应用中应该使用自定义图标）
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            trayIcon = new TrayIcon(image, "MyFocusme");
            trayIcon.setImageAutoSize(true);
            
            try {
                systemTray.add(trayIcon);
                System.out.println("System tray notification initialized");
            } catch (AWTException e) {
                System.err.println("Failed to add tray icon: " + e.getMessage());
                systemTraySupported = false;
            }
        } else {
            System.out.println("System tray is not supported on this platform");
        }
    }
    
    /**
     * 设置主舞台引用（用于应用内通知）
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * 设置通知容器（用于应用内通知）
     */
    public void setNotificationContainer(VBox container) {
        this.notificationContainer = container;
    }
    
    /**
     * 显示系统托盘通知
     */
    public void showSystemNotification(String title, String message, NotificationType type) {
        if (systemTraySupported && trayIcon != null) {
            MessageType messageType;
            switch (type) {
                case INFO:
                    messageType = MessageType.INFO;
                    break;
                case WARNING:
                    messageType = MessageType.WARNING;
                    break;
                case ERROR:
                    messageType = MessageType.ERROR;
                    break;
                default:
                    messageType = MessageType.NONE;
                    break;
            }
            
            trayIcon.displayMessage(title, message, messageType);
            System.out.println("System notification sent: " + title + " - " + message);
        } else {
            // 如果系统托盘不支持，使用应用内通知
            showInAppNotification(title, message, type);
        }
    }
    
    /**
     * 显示应用内通知
     */
    public void showInAppNotification(String title, String message, NotificationType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(getAlertType(type));
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            // 设置图标和样式
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            if (primaryStage != null) {
                alertStage.getIcons().addAll(primaryStage.getIcons());
            }
            
            alert.showAndWait();
        });
    }
    
    /**
     * 显示浮动通知（非阻塞）
     */
    public void showFloatingNotification(String message, NotificationType type) {
        Platform.runLater(() -> {
            if (notificationContainer != null) {
                Label notificationLabel = createNotificationLabel(message, type);
                notificationContainer.getChildren().add(notificationLabel);
                
                // 添加淡入动画
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                
                // 3秒后自动移除
                Timeline removeTimer = new Timeline(
                    new KeyFrame(Duration.seconds(3), e -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notificationLabel);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> notificationContainer.getChildren().remove(notificationLabel));
                        fadeOut.play();
                    })
                );
                removeTimer.play();
            }
        });
    }
    
    /**
     * 创建通知标签
     */
    private Label createNotificationLabel(String message, NotificationType type) {
        Label label = new Label(message);
        label.setMaxWidth(300);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        
        // 根据通知类型设置样式
        String styleClass;
        switch (type) {
            case SUCCESS:
                styleClass = "notification-success";
                break;
            case WARNING:
                styleClass = "notification-warning";
                break;
            case ERROR:
                styleClass = "notification-error";
                break;
            default:
                styleClass = "notification-info";
                break;
        }
        
        label.getStyleClass().addAll("notification", styleClass);
        
        // 设置基础样式
        label.setStyle(
            "-fx-background-color: rgba(0,0,0,0.8); " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 5; " +
            "-fx-font-size: 12pt;"
        );
        
        return label;
    }
    
    /**
     * 转换通知类型为Alert类型
     */
    private Alert.AlertType getAlertType(NotificationType type) {
        switch (type) {
            case WARNING:
                return Alert.AlertType.WARNING;
            case ERROR:
                return Alert.AlertType.ERROR;
            case SUCCESS:
            case INFO:
            default:
                return Alert.AlertType.INFORMATION;
        }
    }
    
    /**
     * 显示学习模式开始通知
     */
    public void notifyFocusModeStarted(int durationMinutes) {
        String message = String.format("学习模式已开始，时长：%d分钟。保持专注！", durationMinutes);
        showSystemNotification("MyFocusme", message, NotificationType.INFO);
        showFloatingNotification("🎯 学习模式已开始", NotificationType.SUCCESS);
    }
    
    /**
     * 显示学习模式结束通知
     */
    public void notifyFocusModeCompleted() {
        String message = "恭喜！你已成功完成这次专注学习。";
        showSystemNotification("MyFocusme", message, NotificationType.SUCCESS);
        showFloatingNotification("🎉 学习完成！", NotificationType.SUCCESS);
    }
    
    /**
     * 显示学习模式剩余时间提醒
     */
    public void notifyRemainingTime(int remainingMinutes) {
        String message;
        if (remainingMinutes == 15) {
            message = "还有15分钟，继续保持专注！";
        } else if (remainingMinutes == 5) {
            message = "最后5分钟，坚持到底！";
        } else if (remainingMinutes == 1) {
            message = "最后1分钟，马上就要完成了！";
        } else {
            message = String.format("还有%d分钟", remainingMinutes);
        }
        
        showFloatingNotification(message, NotificationType.INFO);
    }
    
    /**
     * 显示网站屏蔽通知
     */
    public void notifyWebsiteBlocked(String website) {
        String message = "网站已被屏蔽：" + website;
        showFloatingNotification(message, NotificationType.WARNING);
    }
    
    /**
     * 显示错误通知
     */
    public void notifyError(String errorMessage) {
        showSystemNotification("MyFocusme - 错误", errorMessage, NotificationType.ERROR);
        showFloatingNotification("❌ " + errorMessage, NotificationType.ERROR);
    }
    
    /**
     * 显示成功通知
     */
    public void notifySuccess(String successMessage) {
        showFloatingNotification("✅ " + successMessage, NotificationType.SUCCESS);
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (systemTraySupported && systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
        }
    }
    
    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }
} 