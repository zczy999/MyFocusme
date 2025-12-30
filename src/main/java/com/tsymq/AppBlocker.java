package com.tsymq;

import com.tsymq.browser.Browser;
import com.tsymq.browser.BrowserFactory;
import com.tsymq.browser.EdgeBrowser;
import com.tsymq.mode.ModeManager;
import com.tsymq.config.BlockedSitesConfig;
import com.tsymq.config.AppConfig;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AppBlocker {

    // 屏蔽日志专用 Logger
    private static final Logger blockedLogger = LoggerFactory.getLogger("BlockedSitesLogger");

    private final Set<String> blockedWebsites = new HashSet<>();
    private final Set<String> whiteWebsites = new HashSet<>();
    private ScheduledExecutorService scheduler;
    
    // 添加模式管理器依赖
    private ModeManager modeManager;

    public AppBlocker() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * 设置模式管理器
     * @param modeManager 模式管理器实例
     */
    public void setModeManager(ModeManager modeManager) {
        this.modeManager = modeManager;
    }

    public void monitorActiveEdgeUrl(TextArea outputArea) {
        Runnable monitor = () -> {
            String activeAppName = getActiveAppName();

            // 使用浏览器工厂获取对应的浏览器适配器
            Optional<Browser> browserOpt = BrowserFactory.getBrowser(activeAppName);

            if (browserOpt.isPresent()) {
                Browser browser = browserOpt.get();
                handleBrowserBlocking(browser, outputArea);
            }
            // 非支持的浏览器不做任何处理
        };

        scheduler.scheduleWithFixedDelay(monitor, 0, AppConfig.MONITOR_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 处理浏览器屏蔽逻辑（适用于所有支持的浏览器）
     * @param browser 浏览器适配器
     * @param outputArea 输出区域
     */
    private void handleBrowserBlocking(Browser browser, TextArea outputArea) {
        String url = browser.getActiveTabUrl();
        String title = browser.getActiveTabTitle();

        // 硬编码屏蔽（最高优先级，所有模式生效，直接关闭）
        if (BlockedSitesConfig.isHardcodedBlocked(url) || BlockedSitesConfig.isHardcodedBlocked(title)) {
            browser.closeActiveTab();
            outputArea.appendText("close web " + url + " (" + browser.getName() + ")\n");
            blockedLogger.info("HARDCODED_BLOCKED | {} | {}", browser.getName(), url);
            return;
        }

        // 软屏蔽（打开新标签页，不受白名单影响，仅学习模式生效）
        if (shouldBlock() && BlockedSitesConfig.isSoftBlocked(url)) {
            browser.openNewTab();
            blockedLogger.info("SOFT_BLOCKED | {} | {}", browser.getName(), url);
            return;
        }

        // 精确匹配屏蔽（仅主页，仅学习模式生效）
        if (shouldBlock() && BlockedSitesConfig.isExactMatchBlocked(url)) {
            browser.openNewTab();
            blockedLogger.info("EXACT_MATCH_BLOCKED | {} | {}", browser.getName(), url);
            return;
        }

        // 白名单检查（标题匹配）
        if (isWhiteWeb(title)) {
            blockedLogger.info("WHITE_ALLOWED | {} | {}", browser.getName(), url);
            return;
        }

        // 用户自定义屏蔽网站功能只在学习模式下生效
        if (shouldBlock() && isBlocked(url)) {
            browser.openNewTab();
            blockedLogger.info("USER_BLOCKED | {} | {}", browser.getName(), url);
        }
    }
    
    /**
     * 检查当前是否应该执行用户自定义网站屏蔽功能
     * @return 是否应该屏蔽用户自定义网站
     */
    public boolean shouldBlock() {
        return modeManager != null && modeManager.isInFocusMode();
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
        // 支持所有格式的屏蔽项，包括不带协议的域名
        if (itemToBlock != null && !itemToBlock.trim().isEmpty()) {
            return blockWebsite(itemToBlock);
        }
        return false;
    }

    private boolean blockWebsite(String website) {
        blockedWebsites.add(website);
        saveBlockedWebsites();
        return true;
    }

    /**
     * 打开新的 Edge 标签页（保留向后兼容）
     */
    public void openNewEdgeTab() {
        BrowserFactory.getBrowser(EdgeBrowser.APP_NAME)
                .ifPresent(Browser::openNewTab);
    }

    private String getActiveAppName() {
        String getActiveAppNameScript = "tell application \"System Events\" to name of first application process whose frontmost is true";
        return CommandUtil.executeAppleScript(getActiveAppNameScript);
    }

    /**
     * 关闭当前 Edge 标签页（保留向后兼容）
     */
    public void closeActiveEdgeTab() {
        BrowserFactory.getBrowser(EdgeBrowser.APP_NAME)
                .ifPresent(Browser::closeActiveTab);
    }

    public boolean isBlocked(String url) {
        return blockedWebsites.stream().anyMatch(url::contains);
    }

    public boolean isWhiteWeb(String title) {
        return whiteWebsites.stream().anyMatch(title::contains);
    }

    public void saveBlockedWebsites() {
        try {
            Path path = Paths.get(AppConfig.BLOCKED_WEBSITES_FILE);
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (String website : blockedWebsites) {
                    writer.write(website);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving blocked websites: " + e.getMessage());
        }
    }

    public void loadBlockedWebsites() {
        try {
            Path path = Paths.get(AppConfig.BLOCKED_WEBSITES_FILE);
            if (Files.exists(path)) {
                blockedWebsites.clear();
                Files.lines(path)
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(blockedWebsites::add);
            }
        } catch (IOException e) {
            System.err.println("Error loading blocked websites: " + e.getMessage());
        }
    }

    public void loadwhiteWebsites() {
        try {
            Path path = Paths.get(AppConfig.WHITE_WEBSITES_FILE);
            if (Files.exists(path)) {
                whiteWebsites.clear();
                Files.lines(path)
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(whiteWebsites::add);
            }
        } catch (IOException e) {
            System.err.println("Error loading white websites: " + e.getMessage());
        }
    }
}
