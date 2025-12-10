package com.tsymq.browser;

/**
 * SunBrowser 浏览器适配器
 */
public class SunBrowser extends ChromiumBrowser {

    public static final String APP_NAME = "SunBrowser";

    @Override
    public String getName() {
        return APP_NAME;
    }
}
