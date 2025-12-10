package com.tsymq.browser;

import com.tsymq.CommandUtil;

/**
 * Safari 浏览器适配器
 * 注意：Safari 的 AppleScript 语法与 Chrome/Edge 不同
 */
public class SafariBrowser implements Browser {

    public static final String APP_NAME = "Safari";

    @Override
    public String getName() {
        return APP_NAME;
    }

    @Override
    public String getActiveTabUrl() {
        String command = "tell application \"Safari\" to get URL of current tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    @Override
    public String getActiveTabTitle() {
        String command = "tell application \"Safari\" to get name of current tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    @Override
    public void closeActiveTab() {
        String command = "tell application \"Safari\" to close current tab of front window";
        CommandUtil.executeAppleScript(command);
    }

    @Override
    public void openNewTab() {
        // Safari 需要先激活应用再创建新标签页
        String command = "tell application \"Safari\" to tell front window to set current tab to (make new tab)";
        CommandUtil.executeAppleScript(command);
    }
}
