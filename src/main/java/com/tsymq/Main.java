package com.tsymq;

import com.tsymq.mode.ModeManager;
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
    private AppBlockerController controller;
    private static boolean isShuttingDown = false;

    public static void main(String[] args) {
        // 添加shutdown hook来处理应用退出
        addShutdownHook();
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
        
        // 获取控制器实例
        controller = fxmlLoader.getController();

        primaryStage.setTitle("MyFocusme - 专注学习助手");
        
        // 调整窗口大小 - 保持宽度，增加高度
        Scene scene = new Scene(root, 500, 650);
        primaryStage.setScene(scene);
        
        // 设置窗口属性 - 增加高度范围
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(480);
        primaryStage.setMaxWidth(620);
        primaryStage.setMaxHeight(680);
        
        // 窗口居中显示
        primaryStage.centerOnScreen();

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
        });

        // createTrayIcon();
        
        // 默认不显示窗口（使用托盘图标显示/隐藏）
        // primaryStage.show();
    }

    private void createTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported");
            return;
        }

        SystemTray systemTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/myfocus.png"));
        TrayIcon trayIcon = new TrayIcon(image, "MyFocusme");
        
        // 设置托盘图标提示文本
        updateTrayIconTooltip(trayIcon);
        
        // 定期更新托盘图标提示（显示当前模式和剩余时间）
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                updateTrayIconTooltip(trayIcon);
            }
        }, 0, 30000); // 每30秒更新一次

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Platform.runLater(() -> {
                    if (primaryStage.isShowing()) {
                        primaryStage.hide();
                    } else {
                        primaryStage.show();
                        primaryStage.toFront();
                    }
                });
            }
        });
        
        // 添加右键菜单
        PopupMenu popup = new PopupMenu();
        
        MenuItem showItem = new MenuItem("显示/隐藏");
        showItem.addActionListener(e -> Platform.runLater(() -> {
            if (primaryStage.isShowing()) {
                primaryStage.hide();
            } else {
                primaryStage.show();
                primaryStage.toFront();
            }
        }));
        
        popup.add(showItem);
        trayIcon.setPopupMenu(popup);

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("Unable to add system tray icon");
            e.printStackTrace();
        }
    }
    
    /**
     * 更新托盘图标提示文本
     */
    private void updateTrayIconTooltip(TrayIcon trayIcon) {
        if (controller != null && controller.getModeManager() != null) {
            ModeManager modeManager = controller.getModeManager();
            String tooltip;
            
            if (modeManager.isInFocusMode()) {
                tooltip = "MyFocusme - 学习模式\n剩余时间: " + modeManager.getRemainingTimeFormatted() + "\n持续专注中...";
            } else {
                tooltip = "MyFocusme - 普通模式\n基础保护已启用\n点击显示界面";
            }
            
            trayIcon.setToolTip(tooltip);
        } else {
            trayIcon.setToolTip("MyFocusme - 专注学习助手\n持续运行中");
        }
    }

    @Override
    public void stop() throws Exception {
        isShuttingDown = true;
        
        // 清理资源
        if (controller != null) {
            controller.shutdown();
        }
        
        System.out.println("应用清理完成");
        super.stop();
    }
    
    /**
     * 添加shutdown hook
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!isShuttingDown) {
                System.out.println("检测到外部终止信号 (如 kill 命令)");
                System.out.println("应用正常退出");
            }
        }));
    }
}
