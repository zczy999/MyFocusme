package com.tsymq.browser;

/**
 * 浏览器抽象接口
 * 定义所有支持的浏览器必须实现的通用方法
 */
public interface Browser {

    /**
     * 获取浏览器应用名称（用于 AppleScript）
     * @return 浏览器应用名称
     */
    String getName();

    /**
     * 获取当前活动标签页的 URL
     * @return 当前标签页 URL，获取失败返回空字符串
     */
    String getActiveTabUrl();

    /**
     * 获取当前活动标签页的标题
     * @return 当前标签页标题，获取失败返回空字符串
     */
    String getActiveTabTitle();

    /**
     * 关闭当前活动标签页
     */
    void closeActiveTab();

    /**
     * 打开新标签页
     */
    void openNewTab();
}
