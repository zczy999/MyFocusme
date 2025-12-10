package com.tsymq.browser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 浏览器工厂类
 * 根据应用名称返回对应的浏览器适配器
 */
public class BrowserFactory {

    private static final Map<String, Browser> BROWSERS = new HashMap<>();

    static {
        // 注册所有支持的浏览器
        // Chromium 系浏览器
        registerBrowser(new EdgeBrowser());
        registerBrowser(new ChromeBrowser());
        registerBrowser(new SunBrowser());
        // 其他浏览器
        registerBrowser(new SafariBrowser());
    }

    private static void registerBrowser(Browser browser) {
        BROWSERS.put(browser.getName(), browser);
    }

    /**
     * 根据应用名称获取浏览器适配器
     * @param appName 应用名称
     * @return 对应的浏览器适配器，如果不支持则返回 empty
     */
    public static Optional<Browser> getBrowser(String appName) {
        return Optional.ofNullable(BROWSERS.get(appName));
    }

    /**
     * 检查是否是支持的浏览器
     * @param appName 应用名称
     * @return 是否支持
     */
    public static boolean isSupported(String appName) {
        return BROWSERS.containsKey(appName);
    }

    /**
     * 获取所有支持的浏览器名称
     * @return 浏览器名称集合
     */
    public static Set<String> getSupportedBrowsers() {
        return BROWSERS.keySet();
    }
}
