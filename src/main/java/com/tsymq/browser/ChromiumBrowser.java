package com.tsymq.browser;

import com.tsymq.CommandUtil;

/**
 * Chromium 系浏览器抽象基类
 * 适用于 Chrome、Edge、SunBrowser、Brave、Vivaldi 等基于 Chromium 的浏览器
 * 这些浏览器共享相同的 AppleScript 语法
 */
public abstract class ChromiumBrowser implements Browser {

    @Override
    public String getActiveTabUrl() {
        String command = "tell application \"" + getName() + "\" to get URL of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    @Override
    public String getActiveTabTitle() {
        String command = "tell application \"" + getName() + "\" to get title of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    @Override
    public void closeActiveTab() {
        String command = "tell application \"" + getName() + "\" to close active tab of front window";
        CommandUtil.executeAppleScript(command);
    }

    @Override
    public void openNewTab() {
        String command = "tell application \"" + getName() + "\" to make new tab at end of tabs of front window";
        CommandUtil.executeAppleScript(command);
    }
}
