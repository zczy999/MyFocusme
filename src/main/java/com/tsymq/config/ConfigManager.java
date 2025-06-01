package com.tsymq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tsymq.mode.ModeState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 配置管理类
 * 负责读写JSON配置文件和状态持久化
 */
public class ConfigManager {
    
    private final ObjectMapper objectMapper;
    
    public ConfigManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // 确保配置目录存在
        ensureConfigDirectoryExists();
    }
    
    /**
     * 确保配置目录存在
     */
    private void ensureConfigDirectoryExists() {
        try {
            Path configDir = Paths.get(AppConfig.CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                System.out.println("Created config directory: " + configDir);
            }
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + e.getMessage());
        }
    }
    
    /**
     * 保存模式状态（已禁用）
     * 应用不再保存状态，每次启动都是全新状态
     * @param modeState 要保存的模式状态
     */
    public void saveModeState(ModeState modeState) {
        // 状态保存功能已禁用
        System.out.println("Mode state save disabled - current mode: " + modeState.getCurrentMode());
    }
    
    /**
     * 加载模式状态（已禁用）
     * 总是返回默认的普通模式，不从文件加载
     * @return 默认的普通模式状态
     */
    public ModeState loadModeState() {
        // 总是返回默认的普通模式，不加载保存的状态
        ModeState defaultState = ModeState.createNormalMode();
        System.out.println("Mode state load disabled - starting with normal mode");
        return defaultState;
    }
    
    /**
     * 保存用户配置
     * @param userConfig 要保存的用户配置
     */
    public void saveUserConfig(UserConfig userConfig) {
        try {
            File file = new File(AppConfig.USER_CONFIG_FILE);
            objectMapper.writeValue(file, userConfig);
            System.out.println("User config saved: " + userConfig);
        } catch (IOException e) {
            System.err.println("Failed to save user config: " + e.getMessage());
        }
    }
    
    /**
     * 加载用户配置
     * @return 加载的用户配置，如果文件不存在或加载失败则返回默认配置
     */
    public UserConfig loadUserConfig() {
        try {
            File file = new File(AppConfig.USER_CONFIG_FILE);
            if (file.exists()) {
                UserConfig userConfig = objectMapper.readValue(file, UserConfig.class);
                System.out.println("User config loaded: " + userConfig);
                return userConfig;
            }
        } catch (IOException e) {
            System.err.println("Failed to load user config: " + e.getMessage());
        }
        
        // 返回默认配置
        UserConfig defaultConfig = UserConfig.createDefault();
        saveUserConfig(defaultConfig);
        return defaultConfig;
    }
    
    /**
     * 删除模式状态文件
     */
    public void deleteModeState() {
        try {
            File file = new File(AppConfig.MODE_STATE_FILE);
            if (file.exists() && file.delete()) {
                System.out.println("Mode state file deleted");
            }
        } catch (Exception e) {
            System.err.println("Failed to delete mode state file: " + e.getMessage());
        }
    }
    
    /**
     * 检查配置文件是否存在
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    public boolean configFileExists(String filePath) {
        return new File(filePath).exists();
    }
    
    /**
     * 获取配置目录路径
     * @return 配置目录路径
     */
    public String getConfigDirectory() {
        return AppConfig.CONFIG_DIR;
    }
} 