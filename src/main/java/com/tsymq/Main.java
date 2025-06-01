package com.tsymq;

import com.tsymq.mode.ModeManager;
import com.tsymq.utils.ProcessWatchdog;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main extends Application {

    private Stage primaryStage;
    private AppBlockerController controller;
    private static boolean isShuttingDown = false;
    private static ProcessWatchdog watchdog;
    
    private static final String MAIN_PID_FILE = System.getProperty("user.home") + "/.config/myfocusme/main.pid";

    public static void main(String[] args) {
        // 记录主进程PID
        recordMainPid();
        
        // 启动守护进程（如果不是由守护进程重启的）
        if (System.getenv("MYFOCUSME_RESTARTED_BY_WATCHDOG") == null) {
            startWatchdog();
        } else {
            System.out.println("应用由守护进程重启");
        }
        
        // 添加shutdown hook来阻止命令行kill
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
        
        // 调整窗口大小以适应新的布局
        Scene scene = new Scene(root, 600, 550);
        primaryStage.setScene(scene);
        
        // 设置窗口属性
        primaryStage.setMinWidth(550);
        primaryStage.setMinHeight(500);
        primaryStage.setMaxWidth(700);
        primaryStage.setMaxHeight(700);
        
        // 窗口居中显示
        primaryStage.centerOnScreen();

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
        });
        
        // 设置应用关闭时的清理逻辑
        primaryStage.getScene().getWindow().setOnHidden(e -> {
            // 当窗口隐藏时不执行清理，只有真正退出时才清理
        });

        createTrayIcon();
        
        // 根据当前模式决定是否显示窗口
        // 如果是学习模式，默认不显示窗口以减少干扰
        if (controller.getModeManager().isInFocusMode()) {
            System.out.println("应用启动时检测到学习模式，窗口将保持隐藏");
        } else {
            // primaryStage.show(); // 可以选择是否默认显示
        }
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
    
    /**
     * 记录主进程PID
     */
    private static void recordMainPid() {
        try {
            String pid = String.valueOf(ProcessHandle.current().pid());
            Path pidFile = Paths.get(MAIN_PID_FILE);
            Files.createDirectories(pidFile.getParent());
            Files.write(pidFile, pid.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Main process PID recorded: " + pid);
        } catch (Exception e) {
            System.err.println("Failed to record main PID: " + e.getMessage());
        }
    }
    
    /**
     * 启动守护进程
     */
    private static void startWatchdog() {
        try {
            // 检查是否已经有守护进程在运行
            if (isWatchdogRunning()) {
                System.out.println("检测到守护进程已在运行，跳过启动");
                return;
            }
            
            String projectDir = System.getProperty("user.dir");
            
            // 在后台启动守护进程
            ProcessBuilder builder = new ProcessBuilder(
                "java", "-cp", System.getProperty("java.class.path"),
                "com.tsymq.utils.ProcessWatchdog", projectDir
            );
            
            // 设置守护进程在后台运行
            builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            
            Process watchdogProcess = builder.start();
            System.out.println("ProcessWatchdog started in background, PID: " + watchdogProcess.pid());
            
            // 等待一下确保守护进程启动成功
            Thread.sleep(1000);
            
            if (!watchdogProcess.isAlive()) {
                System.err.println("守护进程启动失败，退出码: " + watchdogProcess.exitValue());
            }
            
        } catch (Exception e) {
            System.err.println("Failed to start ProcessWatchdog: " + e.getMessage());
            System.out.println("继续运行主程序，但没有守护进程保护");
        }
    }
    
    /**
     * 检查守护进程是否已在运行
     */
    private static boolean isWatchdogRunning() {
        try {
            Path watchdogPidFile = Paths.get(System.getProperty("user.home"), ".config", "myfocusme", "watchdog.pid");
            
            if (!Files.exists(watchdogPidFile)) {
                return false;
            }
            
            String watchdogPid = new String(Files.readAllBytes(watchdogPidFile)).trim();
            
            // 检查该PID的进程是否还在运行，并且是守护进程
            Process checkProcess = new ProcessBuilder("ps", "-p", watchdogPid, "-o", "command=").start();
            int exitCode = checkProcess.waitFor();
            
            if (exitCode == 0) {
                // 进程存在，检查是否是我们的守护进程
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(checkProcess.getInputStream()))) {
                    String command = reader.readLine();
                    if (command != null && command.contains("ProcessWatchdog")) {
                        System.out.println("发现运行中的守护进程 PID: " + watchdogPid);
                        return true;
                    }
                }
            }
            
            // PID文件存在但进程不存在，清理旧的PID文件
            Files.deleteIfExists(watchdogPidFile);
            System.out.println("清理了过期的守护进程PID文件");
            return false;
            
        } catch (Exception e) {
            System.err.println("检查守护进程状态失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 应用退出时的清理工作
     * 注意：作为强制性专注应用，正常情况下不应该退出
     * 只有在系统强制关闭或特殊情况下才会执行此方法
     */
    @Override
    public void stop() throws Exception {
        System.out.println("检测到应用退出请求...");
        
        // 检查当前是否在学习模式
        if (controller != null && controller.getModeManager() != null && 
            controller.getModeManager().isInFocusMode()) {
            System.out.println("警告：当前处于学习模式，不建议退出应用！");
            // 在学习模式下，可以选择阻止退出或记录退出行为
            // 这里暂时允许退出，但会记录警告
        }
        
        // 执行必要的清理工作
        if (controller != null) {
            controller.shutdown();
        }
        
        // 清理PID文件
        try {
            Files.deleteIfExists(Paths.get(MAIN_PID_FILE));
        } catch (Exception e) {
            System.err.println("Error cleaning up main PID file: " + e.getMessage());
        }
        
        System.out.println("应用清理完成");
        super.stop();
    }

    /**
     * 添加shutdown hook来检测和阻止外部终止信号
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("检测到外部终止信号 (如 kill 命令)");
            
            // 设置标志位，防止重复处理
            if (isShuttingDown) {
                return;
            }
            isShuttingDown = true;
            
            try {
                // 尝试检查应用状态（这里可能无法访问JavaFX组件）
                System.out.println("警告：MyFocusme是一个专注学习应用，不建议强制退出！");
                
                // 记录强制退出行为
                logForceExit();
                
                // 在学习模式下，尝试阻止退出（延迟处理）
                if (shouldPreventExit()) {
                    System.out.println("检测到学习模式，正在尝试阻止退出...");
                    
                    // 尝试重启应用（在后台）
                    attemptRestart();
                }
                
            } catch (Exception e) {
                System.err.println("处理shutdown hook时出错: " + e.getMessage());
            }
        }));
    }
    
    /**
     * 记录强制退出行为
     */
    private static void logForceExit() {
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            System.out.println("强制退出记录: " + now);
            
            // 可以选择写入日志文件
            java.nio.file.Path logFile = java.nio.file.Paths.get(
                System.getProperty("user.home"), ".config", "myfocusme", "force_exit.log"
            );
            
            String logEntry = now + " - 检测到强制退出尝试\n";
            java.nio.file.Files.write(logFile, logEntry.getBytes(), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND);
                
        } catch (Exception e) {
            System.err.println("记录强制退出失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否应该阻止退出
     */
    private static boolean shouldPreventExit() {
        try {
            // 检查模式状态文件
            java.nio.file.Path stateFile = java.nio.file.Paths.get(
                System.getProperty("user.home"), ".config", "myfocusme", "mode_state.json"
            );
            
            if (java.nio.file.Files.exists(stateFile)) {
                String content = new String(java.nio.file.Files.readAllBytes(stateFile));
                return content.contains("\"currentMode\" : \"FOCUS\"");
            }
        } catch (Exception e) {
            System.err.println("检查模式状态失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 尝试重启应用
     */
    private static void attemptRestart() {
        try {
            System.out.println("正在尝试重启应用以维持学习模式...");
            
            // 延迟重启，给当前进程一些时间完成清理
            Thread.sleep(1000);
            
            // 尝试使用Maven重启（如果在开发环境）
            String userDir = System.getProperty("user.dir");
            java.nio.file.Path pomFile = java.nio.file.Paths.get(userDir, "pom.xml");
            
            ProcessBuilder builder;
            if (java.nio.file.Files.exists(pomFile)) {
                // 在Maven项目目录下，使用mvn javafx:run
                System.out.println("检测到Maven项目，使用mvn javafx:run重启");
                builder = new ProcessBuilder("mvn", "javafx:run");
                builder.directory(new java.io.File(userDir));
            } else {
                // 使用Java直接启动
                String javaHome = System.getProperty("java.home");
                String javaBin = javaHome + "/bin/java";
                String classpath = System.getProperty("java.class.path");
                String className = Main.class.getCanonicalName();
                
                builder = new ProcessBuilder(javaBin, "-cp", classpath, className);
            }
            
            // 设置环境变量
            builder.environment().put("MYFOCUSME_RESTART", "true");
            
            // 启动新进程
            Process process = builder.start();
            System.out.println("应用重启命令已执行");
            
            // 给新进程一些时间启动
            Thread.sleep(2000);
            
            if (process.isAlive()) {
                System.out.println("应用重启成功！");
            } else {
                System.out.println("应用重启可能失败，退出码: " + process.exitValue());
            }
            
        } catch (Exception e) {
            System.err.println("重启应用失败: " + e.getMessage());
            System.out.println("请手动重启MyFocusme以继续学习模式");
            
            // 尝试显示系统通知
            try {
                if (SystemTray.isSupported()) {
                    SystemTray tray = SystemTray.getSystemTray();
                    Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                    TrayIcon trayIcon = new TrayIcon(image, "MyFocusme");
                    tray.add(trayIcon);
                    trayIcon.displayMessage("MyFocusme", 
                        "学习模式被强制中断，请重新启动应用！", 
                        TrayIcon.MessageType.WARNING);
                }
            } catch (Exception notificationError) {
                System.err.println("显示通知失败: " + notificationError.getMessage());
            }
        }
    }
}
