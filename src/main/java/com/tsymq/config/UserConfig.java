package com.tsymq.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户配置数据类
 * 用于存储用户的个性化设置
 */
public class UserConfig {
    
    private final int defaultFocusDuration;
    private final boolean enableNotifications;
    private final boolean enableSounds;
    private final String theme;
    
    @JsonCreator
    public UserConfig(
            @JsonProperty("defaultFocusDuration") int defaultFocusDuration,
            @JsonProperty("enableNotifications") boolean enableNotifications,
            @JsonProperty("enableSounds") boolean enableSounds,
            @JsonProperty("theme") String theme) {
        this.defaultFocusDuration = defaultFocusDuration;
        this.enableNotifications = enableNotifications;
        this.enableSounds = enableSounds;
        this.theme = theme;
    }
    
    // 创建默认配置
    public static UserConfig createDefault() {
        return new UserConfig(
                AppConfig.DEFAULT_FOCUS_DURATION_MINUTES,
                true,
                true,
                "default"
        );
    }
    
    // Getters
    public int getDefaultFocusDuration() {
        return defaultFocusDuration;
    }
    
    public boolean isEnableNotifications() {
        return enableNotifications;
    }
    
    public boolean isEnableSounds() {
        return enableSounds;
    }
    
    public String getTheme() {
        return theme;
    }
    
    // 创建修改后的配置
    public UserConfig withDefaultFocusDuration(int duration) {
        return new UserConfig(duration, enableNotifications, enableSounds, theme);
    }
    
    public UserConfig withNotifications(boolean enabled) {
        return new UserConfig(defaultFocusDuration, enabled, enableSounds, theme);
    }
    
    public UserConfig withSounds(boolean enabled) {
        return new UserConfig(defaultFocusDuration, enableNotifications, enabled, theme);
    }
    
    public UserConfig withTheme(String theme) {
        return new UserConfig(defaultFocusDuration, enableNotifications, enableSounds, theme);
    }
    
    @Override
    public String toString() {
        return "UserConfig{" +
                "defaultFocusDuration=" + defaultFocusDuration +
                ", enableNotifications=" + enableNotifications +
                ", enableSounds=" + enableSounds +
                ", theme='" + theme + '\'' +
                '}';
    }
} 