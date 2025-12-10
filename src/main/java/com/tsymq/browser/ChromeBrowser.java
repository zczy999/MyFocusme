package com.tsymq.browser;

/**
 * Google Chrome 浏览器适配器
 */
public class ChromeBrowser extends ChromiumBrowser {

    public static final String APP_NAME = "Google Chrome";

    @Override
    public String getName() {
        return APP_NAME;
    }
}
