package com.tsymq.browser;

/**
 * Microsoft Edge 浏览器适配器
 */
public class EdgeBrowser extends ChromiumBrowser {

    public static final String APP_NAME = "Microsoft Edge";

    @Override
    public String getName() {
        return APP_NAME;
    }
}
