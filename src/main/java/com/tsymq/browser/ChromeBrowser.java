package com.tsymq.browser;

import com.tsymq.CommandUtil;

/**
 * Google Chrome 浏览器适配器
 */
public class ChromeBrowser implements Browser {

    public static final String APP_NAME = "Google Chrome";

    @Override
    public String getName() {
        return APP_NAME;
    }

    @Override
    public String getActiveTabUrl() {
        String command = "tell application \"Google Chrome\" to get URL of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    @Override
    public String getActiveTabTitle() {
        String command = "tell application \"Google Chrome\" to get title of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    @Override
    public void closeActiveTab() {
        String command = "tell application \"Google Chrome\" to close active tab of front window";
        CommandUtil.executeAppleScript(command);
    }

    @Override
    public void openNewTab() {
        String command = "tell application \"Google Chrome\" to make new tab at end of tabs of front window";
        CommandUtil.executeAppleScript(command);
    }
}
