package com.tsymq;

import com.tsymq.mode.ModeManager;
import com.tsymq.config.BlockedSitesConfig;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AppBlocker {

    private final String BLOCKED_WEBSITES_FILENAME = "/Users/tsymq/.config/myfocusme/blocked_websites.txt";

    private final String WHITE_WEBSITES_FILENAME = "/Users/tsymq/.config/myfocusme/white_websites.txt";

    private final Set<String> blockedWebsites = new HashSet<>();

    private final Set<String> whiteWebsites = new HashSet<>();

    private final Set<String> closedWebsites = new HashSet<>();

    private ScheduledExecutorService scheduler;
    
    // 添加模式管理器依赖
    private ModeManager modeManager;


    public AppBlocker() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        // 硬编码的屏蔽列表现在由BlockedSitesConfig管理
    }
    
    /**
     * 设置模式管理器
     * @param modeManager 模式管理器实例
     */
    public void setModeManager(ModeManager modeManager) {
        this.modeManager = modeManager;
    }

    public void monitorActiveEdgeUrl(TextArea outputArea) {
        Runnable monitor = new Runnable() {
            public void run() {
                String activeAppName = getActiveAppName();
                if (activeAppName.equals("Google Chrome") || activeAppName.equals("Safari")) {
                    closeApp(activeAppName);
                    outputArea.appendText("close " + activeAppName + "\n");
                    return;
                }

                if (activeAppName.equals("Microsoft Edge")) {
                    String activeEdgeURL = getActiveEdgeURL();
                    String activeEdgeTitle = getActiveEdgeTitle();

                    if (isWhiteWeb(activeEdgeTitle)) {
                        return;
                    }

                    // 用户自定义屏蔽网站功能只在学习模式下生效
                    if (modeManager != null && modeManager.isInFocusMode() && isBlocked(activeEdgeURL)) {
                        openNewEdgeTab();
                        return;
                    }

                    // 硬编码的色情网站屏蔽在所有模式下都生效
                    if (isClosed(activeEdgeURL)) {
                        closeActiveEdgeTab();
                        outputArea.appendText("close web" + activeEdgeURL + "\n");
                    }
                }
            }
        };

        scheduler.scheduleWithFixedDelay(monitor, 0, 1500, TimeUnit.MILLISECONDS);

    }
    
    /**
     * 检查当前是否应该执行用户自定义网站屏蔽功能
     * @return 是否应该屏蔽用户自定义网站
     */
    public boolean shouldBlock() {
        return modeManager != null && modeManager.isInFocusMode();
    }
    
    /**
     * 检查当前是否应该执行硬编码网站屏蔽功能
     * @return 硬编码网站屏蔽始终生效
     */
    public boolean shouldBlockHardcoded() {
        return true; // 硬编码网站屏蔽在所有模式下都生效
    }
    
    /**
     * 获取当前模式状态信息
     * @return 模式状态信息
     */
    public String getModeStatusInfo() {
        if (modeManager == null) {
            return "模式管理器未初始化";
        }
        
        if (modeManager.isInFocusMode()) {
            return "学习模式 - 全部屏蔽功能已启用 - 剩余时间: " + modeManager.getRemainingTimeFormatted();
        } else {
            return "普通模式 - 仅基础屏蔽功能启用（用户自定义屏蔽已禁用）";
        }
    }

    public void stop() {
        if (this.scheduler != null) {
            this.scheduler.shutdown();
            try {
                // 等待任务结束，最长等待1分钟
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow(); // 取消当前执行的任务
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }


    public boolean block(String itemToBlock) {
        if (itemToBlock.contains("http://") || itemToBlock.contains("https://")) {
            return blockWebsite(itemToBlock);
        } else {
            // 如果需要屏蔽应用程序，这里可以实现相应的逻辑。
            return false;
        }
    }

    private boolean blockWebsite(String website) {
        blockedWebsites.add(website);
        saveBlockedWebsites();
        return true;
    }

    public void openNewEdgeTab() {
        String command = "tell application \"Microsoft Edge\" to make new tab at end of tabs of front window";
        CommandUtil.executeAppleScript(command);
    }


    private String getActiveAppName() {
        String getActiveAppNameScript = "tell application \"System Events\" to name of first application process whose frontmost is true";
        return CommandUtil.executeAppleScript(getActiveAppNameScript);
    }


    private String getActiveEdgeURL() {
        String command = "tell application \"Microsoft Edge\" to get URL of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    private String getActiveEdgeTitle() {
        String command = "tell application \"Microsoft Edge\" to set currentTitle to title of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }


    public void closeActiveEdgeTab() {
        String closeTabScript;
        closeTabScript = "tell application \"Microsoft Edge\" to close active tab of front window";
        CommandUtil.executeAppleScript(closeTabScript);
    }

    private void closeApp(String activeAppName) {
        String closeAppScript = "tell application \"%s\" to quit";
        closeAppScript = String.format(closeAppScript, activeAppName);
        CommandUtil.executeAppleScript(closeAppScript);
    }

    public boolean isBlocked(String url) {
        for (String blockedWebsite : blockedWebsites) {
            if (url.contains(blockedWebsite)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWhiteWeb(String title) {
        for (String whiteWebsite : whiteWebsites) {
            if (title.contains(whiteWebsite)) {
                return true;
            }
        }
        return false;
    }

    public boolean isClosed(String url) {
        // 使用配置类检查硬编码的屏蔽网站
        return BlockedSitesConfig.isHardcodedBlocked(url);
    }


    public void saveBlockedWebsites() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(BLOCKED_WEBSITES_FILENAME))) {
            for (String blockedWebsite : blockedWebsites) {
                writer.write(blockedWebsite);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBlockedWebsites() {
        Path blockedWebsitesPath = Paths.get(BLOCKED_WEBSITES_FILENAME);
        if (Files.exists(blockedWebsitesPath)) {
            try (BufferedReader reader = Files.newBufferedReader(blockedWebsitesPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    blockedWebsites.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadwhiteWebsites() {
        Path whiteWebsitesPath = Paths.get(WHITE_WEBSITES_FILENAME);
        if (Files.exists(whiteWebsitesPath)) {
            try (BufferedReader reader = Files.newBufferedReader(whiteWebsitesPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    whiteWebsites.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
