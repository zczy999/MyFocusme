package com.tsymq.browser;

import com.tsymq.CommandUtil;

/**
 * Microsoft Edge 浏览器适配器
 */
public class EdgeBrowser implements Browser {

    public static final String APP_NAME = "Microsoft Edge";

    @Override
    public String getName() {
        return APP_NAME;
    }

    @Override
    public String getActiveTabUrl() {
        String command = "tell application \"Microsoft Edge\" to get URL of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    @Override
    public String getActiveTabTitle() {
        String command = "tell application \"Microsoft Edge\" to get title of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    @Override
    public void closeActiveTab() {
        String command = "tell application \"Microsoft Edge\" to close active tab of front window";
        CommandUtil.executeAppleScript(command);
    }

    @Override
    public void openNewTab() {
        String command = "tell application \"Microsoft Edge\" to make new tab at end of tabs of front window";
        CommandUtil.executeAppleScript(command);
    }
}
