package com.tsymq;

import org.junit.Test;

public class CommandUtilTest {

    @Test
    public void closeApp() {
        String activeAppName = "Safari";
        String closeAppScript = "tell application \"%s\" to quit";
        closeAppScript = String.format(closeAppScript, activeAppName);
        CommandUtil.executeAppleScript(closeAppScript);
    }

}
