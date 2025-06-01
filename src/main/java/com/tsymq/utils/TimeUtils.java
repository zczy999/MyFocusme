package com.tsymq.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 * 提供时间格式化和计算相关的工具方法
 */
public class TimeUtils {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private TimeUtils() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 将毫秒转换为可读的时间格式 (HH:mm:ss)
     * @param milliseconds 毫秒数
     * @return 格式化的时间字符串
     */
    public static String formatDuration(long milliseconds) {
        if (milliseconds <= 0) {
            return "00:00:00";
        }
        
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
    
    /**
     * 将毫秒转换为简短的时间格式 (如: 1h 30m, 45m, 30s)
     * @param milliseconds 毫秒数
     * @return 简短的时间字符串
     */
    public static String formatDurationShort(long milliseconds) {
        if (milliseconds <= 0) {
            return "0s";
        }
        
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (remainingSeconds > 0 && hours == 0) {
            sb.append(remainingSeconds).append("s");
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 将分钟转换为毫秒
     * @param minutes 分钟数
     * @return 毫秒数
     */
    public static long minutesToMillis(int minutes) {
        return minutes * 60 * 1000L;
    }
    
    /**
     * 将毫秒转换为分钟
     * @param milliseconds 毫秒数
     * @return 分钟数
     */
    public static int millisToMinutes(long milliseconds) {
        return (int) (milliseconds / (60 * 1000));
    }
    
    /**
     * 格式化当前时间
     * @return 格式化的当前时间字符串
     */
    public static String formatCurrentTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }
    
    /**
     * 格式化时间戳
     * @param timestamp 时间戳（毫秒）
     * @return 格式化的时间字符串
     */
    public static String formatTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp),
                java.time.ZoneId.systemDefault()
        ).format(DATE_TIME_FORMATTER);
    }
    
    /**
     * 检查时间戳是否在指定的时间范围内
     * @param timestamp 要检查的时间戳
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否在范围内
     */
    public static boolean isInTimeRange(long timestamp, long startTime, long endTime) {
        return timestamp >= startTime && timestamp <= endTime;
    }
    
    /**
     * 计算两个时间戳之间的差值（毫秒）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间差（毫秒）
     */
    public static long getTimeDifference(long startTime, long endTime) {
        return Math.abs(endTime - startTime);
    }
} 