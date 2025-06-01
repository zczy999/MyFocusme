package com.tsymq.config;

/**
 * 应用配置常量类
 * 定义应用中使用的各种常量和配置路径
 */
public class AppConfig {
    
    // 配置文件目录
    public static final String CONFIG_DIR = System.getProperty("user.home") + "/.config/myfocusme";
    
    // 配置文件路径
    public static final String MODE_STATE_FILE = CONFIG_DIR + "/mode_state.json";
    public static final String USER_CONFIG_FILE = CONFIG_DIR + "/user_config.json";
    public static final String BLOCKED_WEBSITES_FILE = CONFIG_DIR + "/blocked_websites.txt";
    public static final String WHITE_WEBSITES_FILE = CONFIG_DIR + "/white_websites.txt";
    
    // 时间相关常量
    public static final int MIN_FOCUS_DURATION_MINUTES = 15;
    public static final int MAX_FOCUS_DURATION_MINUTES = 480; // 8小时
    public static final int DEFAULT_FOCUS_DURATION_MINUTES = 60;
    
    // UI更新间隔
    public static final int UI_UPDATE_INTERVAL_MS = 1000; // 1秒
    public static final int MONITOR_INTERVAL_MS = 1500; // 1.5秒
    
    // 应用信息
    public static final String APP_NAME = "MyFocusme";
    public static final String APP_VERSION = "1.0.0";
    
    private AppConfig() {
        // 私有构造函数，防止实例化
    }
} 