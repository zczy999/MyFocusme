package com.tsymq.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 安全工具类
 * 处理密码验证、加密等安全相关功能
 */
public class SecurityUtils {
    
    private static final String DEFAULT_EMERGENCY_PASSWORD = "emergency123";
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * 验证紧急退出密码
     * @param inputPassword 用户输入的密码
     * @return 是否验证通过
     */
    public static boolean verifyEmergencyPassword(String inputPassword) {
        if (inputPassword == null || inputPassword.trim().isEmpty()) {
            return false;
        }
        
        // 简单验证，实际项目中应该使用更安全的方式
        return DEFAULT_EMERGENCY_PASSWORD.equals(inputPassword.trim());
    }
    
    /**
     * 生成密码哈希值
     * @param password 原始密码
     * @return 哈希值
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }
    
    /**
     * 验证密码哈希
     * @param password 原始密码
     * @param hash 存储的哈希值
     * @return 是否匹配
     */
    public static boolean verifyPasswordHash(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        
        String computedHash = hashPassword(password);
        return computedHash.equals(hash);
    }
    
    /**
     * 生成随机盐值
     * @return Base64编码的盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * 检查是否为安全的密码
     * @param password 密码
     * @return 是否安全
     */
    public static boolean isSecurePassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        return hasLetter && hasDigit;
    }
    
    /**
     * 获取默认紧急密码（仅用于开发测试）
     * @return 默认密码
     */
    public static String getDefaultEmergencyPassword() {
        return DEFAULT_EMERGENCY_PASSWORD;
    }
} 