package com.tsymq.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 进程守护类
 * 监控主进程，在其被强制退出时自动重启
 */
public class ProcessWatchdog {
    
    private static final String WATCHDOG_PID_FILE = System.getProperty("user.home") + "/.config/myfocusme/watchdog.pid";
    private static final String MAIN_PID_FILE = System.getProperty("user.home") + "/.config/myfocusme/main.pid";
    private static final String RESTART_LOG_FILE = System.getProperty("user.home") + "/.config/myfocusme/restart.log";
    
    private final ScheduledExecutorService scheduler;
    private final String projectDir;
    private volatile boolean isRunning = true;
    
    public ProcessWatchdog(String projectDir) {
        this.projectDir = projectDir;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * 启动守护进程
     */
    public void start() {
        try {
            // 检查是否已经有其他守护进程在运行
            if (isAnotherWatchdogRunning()) {
                System.out.println("检测到其他守护进程已在运行，当前实例退出");
                return;
            }
            
            // 记录守护进程PID
            recordWatchdogPid();
            
            System.out.println("ProcessWatchdog started, monitoring main process...");
            
            // 每5秒检查一次主进程
            scheduler.scheduleWithFixedDelay(this::checkMainProcess, 5, 5, TimeUnit.SECONDS);
            
            // 添加shutdown hook来清理守护进程
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            
        } catch (Exception e) {
            System.err.println("Failed to start ProcessWatchdog: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否有其他守护进程在运行
     */
    private boolean isAnotherWatchdogRunning() {
        try {
            Path pidFile = Paths.get(WATCHDOG_PID_FILE);
            if (!Files.exists(pidFile)) {
                return false;
            }
            
            String existingPid = new String(Files.readAllBytes(pidFile)).trim();
            String currentPid = String.valueOf(ProcessHandle.current().pid());
            
            // 如果PID文件中的PID就是当前进程，说明是正常情况
            if (existingPid.equals(currentPid)) {
                return false;
            }
            
            // 检查该PID的进程是否还在运行
            Process checkProcess = new ProcessBuilder("ps", "-p", existingPid, "-o", "command=").start();
            int exitCode = checkProcess.waitFor();
            
            if (exitCode == 0) {
                // 进程存在，检查是否是守护进程
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(checkProcess.getInputStream()))) {
                    String command = reader.readLine();
                    if (command != null && command.contains("ProcessWatchdog")) {
                        System.out.println("发现其他守护进程正在运行 PID: " + existingPid);
                        return true;
                    }
                }
            }
            
            // PID文件存在但进程不存在，清理旧文件
            Files.deleteIfExists(pidFile);
            System.out.println("清理了过期的守护进程PID文件");
            return false;
            
        } catch (Exception e) {
            System.err.println("检查其他守护进程失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查主进程是否还在运行
     */
    private void checkMainProcess() {
        try {
            String mainPid = readMainPid();
            if (mainPid == null) {
                return; // 没有记录的主进程PID
            }
            
            if (!isProcessRunning(mainPid)) {
                System.out.println("Main process (PID: " + mainPid + ") is not running, attempting restart...");
                
                // 检查是否在学习模式
                if (isInFocusMode()) {
                    logRestart("Main process died in FOCUS mode, restarting...");
                    restartMainProcess();
                } else {
                    logRestart("Main process died in NORMAL mode, restarting...");
                    restartMainProcess();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error checking main process: " + e.getMessage());
        }
    }
    
    /**
     * 检查进程是否在运行
     */
    private boolean isProcessRunning(String pid) {
        try {
            // 在macOS上使用ps命令检查进程
            Process process = new ProcessBuilder("ps", "-p", pid).start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 重启主进程
     */
    private void restartMainProcess() {
        try {
            ProcessBuilder builder;
            
            // 检查是否在Maven项目目录
            Path pomFile = Paths.get(projectDir, "pom.xml");
            if (Files.exists(pomFile)) {
                builder = new ProcessBuilder("mvn", "javafx:run");
                builder.directory(pomFile.getParent().toFile());
            } else {
                // 使用jar文件启动（如果有的话）
                builder = new ProcessBuilder("java", "-jar", "MyFocusme.jar");
            }
            
            // 设置环境变量标记这是重启
            builder.environment().put("MYFOCUSME_RESTARTED_BY_WATCHDOG", "true");
            
            Process newProcess = builder.start();
            
            // 等待一下确保进程启动
            Thread.sleep(3000);
            
            if (newProcess.isAlive()) {
                System.out.println("Main process restarted successfully");
                logRestart("Main process restarted successfully by watchdog");
            } else {
                System.out.println("Failed to restart main process, exit code: " + newProcess.exitValue());
                logRestart("Failed to restart main process, exit code: " + newProcess.exitValue());
            }
            
        } catch (Exception e) {
            System.err.println("Failed to restart main process: " + e.getMessage());
            logRestart("Failed to restart main process: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否在学习模式
     */
    private boolean isInFocusMode() {
        try {
            Path stateFile = Paths.get(System.getProperty("user.home"), ".config", "myfocusme", "mode_state.json");
            if (Files.exists(stateFile)) {
                String content = new String(Files.readAllBytes(stateFile));
                return content.contains("\"currentMode\" : \"FOCUS\"");
            }
        } catch (Exception e) {
            System.err.println("Error checking focus mode: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 记录守护进程PID
     */
    private void recordWatchdogPid() throws IOException {
        String pid = String.valueOf(ProcessHandle.current().pid());
        Path pidFile = Paths.get(WATCHDOG_PID_FILE);
        Files.createDirectories(pidFile.getParent());
        Files.write(pidFile, pid.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    /**
     * 读取主进程PID
     */
    private String readMainPid() {
        try {
            Path pidFile = Paths.get(MAIN_PID_FILE);
            if (Files.exists(pidFile)) {
                return new String(Files.readAllBytes(pidFile)).trim();
            }
        } catch (Exception e) {
            System.err.println("Error reading main PID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 记录重启日志
     */
    private void logRestart(String message) {
        try {
            String logEntry = LocalDateTime.now() + " - " + message + "\n";
            Path logFile = Paths.get(RESTART_LOG_FILE);
            Files.createDirectories(logFile.getParent());
            Files.write(logFile, logEntry.getBytes(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.err.println("Error writing restart log: " + e.getMessage());
        }
    }
    
    /**
     * 停止守护进程
     */
    public void shutdown() {
        isRunning = false;
        if (scheduler != null) {
            scheduler.shutdown();
        }
        
        // 清理PID文件
        try {
            Files.deleteIfExists(Paths.get(WATCHDOG_PID_FILE));
        } catch (Exception e) {
            System.err.println("Error cleaning up watchdog PID file: " + e.getMessage());
        }
        
        System.out.println("ProcessWatchdog shutdown");
    }
    
    /**
     * 主方法 - 可以独立运行守护进程
     */
    public static void main(String[] args) {
        String projectDir = args.length > 0 ? args[0] : System.getProperty("user.dir");
        
        ProcessWatchdog watchdog = new ProcessWatchdog(projectDir);
        watchdog.start();
        
        // 保持守护进程运行
        try {
            while (watchdog.isRunning) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("Watchdog interrupted");
        }
    }
} 