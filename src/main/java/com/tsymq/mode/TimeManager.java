package com.tsymq.mode;

import com.tsymq.utils.TimeUtils;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 时间管理器
 * 负责学习模式的时间控制、倒计时和自动退出
 */
public class TimeManager {
    
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> countdownTask;
    private ScheduledFuture<?> autoExitTask;
    
    // 剩余时间属性（毫秒）
    private final LongProperty remainingTimeProperty;
    
    // 回调函数
    private Consumer<Long> onTimeUpdate;
    private Runnable onTimeExpired;
    
    // 时间相关
    private long focusModeEndTime;
    private boolean isRunning;
    
    public TimeManager() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.remainingTimeProperty = new SimpleLongProperty(0);
        this.isRunning = false;
    }
    
    /**
     * 启动学习模式倒计时
     * @param durationMinutes 持续时间（分钟）
     */
    public void startFocusMode(int durationMinutes) {
        if (isRunning) {
            stopFocusMode();
        }
        
        this.focusModeEndTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L);
        this.isRunning = true;
        
        // 启动倒计时更新任务（每秒更新一次）
        startCountdownTask();
        
        // 启动自动退出任务
        startAutoExitTask(durationMinutes);
    }
    
    /**
     * 停止学习模式
     */
    public void stopFocusMode() {
        isRunning = false;
        
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel(true);
        }
        
        if (autoExitTask != null && !autoExitTask.isCancelled()) {
            autoExitTask.cancel(true);
        }
        
        remainingTimeProperty.set(0);
    }
    
    /**
     * 获取剩余时间（毫秒）
     * @return 剩余时间
     */
    public long getRemainingTime() {
        if (!isRunning) {
            return 0;
        }
        
        long remaining = focusModeEndTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * 获取剩余时间属性（用于UI绑定）
     * @return 剩余时间属性
     */
    public LongProperty remainingTimeProperty() {
        return remainingTimeProperty;
    }
    
    /**
     * 检查是否还在运行
     * @return 是否运行中
     */
    public boolean isRunning() {
        return isRunning && getRemainingTime() > 0;
    }
    
    /**
     * 设置时间更新回调
     * @param callback 回调函数，参数为剩余时间（毫秒）
     */
    public void setOnTimeUpdate(Consumer<Long> callback) {
        this.onTimeUpdate = callback;
    }
    
    /**
     * 设置时间到期回调
     * @param callback 回调函数
     */
    public void setOnTimeExpired(Runnable callback) {
        this.onTimeExpired = callback;
    }
    
    /**
     * 获取格式化的剩余时间字符串
     * @return 格式化时间字符串
     */
    public String getFormattedRemainingTime() {
        long remaining = getRemainingTime();
        return TimeUtils.formatDuration(remaining);
    }
    
    /**
     * 延长学习时间
     * @param additionalMinutes 额外的分钟数
     */
    public void extendTime(int additionalMinutes) {
        if (isRunning) {
            this.focusModeEndTime += additionalMinutes * 60 * 1000L;
            
            // 重新启动自动退出任务
            if (autoExitTask != null && !autoExitTask.isCancelled()) {
                autoExitTask.cancel(true);
            }
            
            long remainingMinutes = getRemainingTime() / (60 * 1000);
            startAutoExitTask((int) remainingMinutes);
        }
    }
    
    /**
     * 启动倒计时更新任务
     */
    private void startCountdownTask() {
        countdownTask = scheduler.scheduleAtFixedRate(() -> {
            long remaining = getRemainingTime();
            
            // 在JavaFX应用线程中更新UI
            Platform.runLater(() -> {
                remainingTimeProperty.set(remaining);
                
                if (onTimeUpdate != null) {
                    onTimeUpdate.accept(remaining);
                }
            });
            
            // 如果时间到了，停止任务
            if (remaining <= 0) {
                stopFocusMode();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    /**
     * 启动自动退出任务
     * @param durationMinutes 持续时间
     */
    private void startAutoExitTask(int durationMinutes) {
        autoExitTask = scheduler.schedule(() -> {
            Platform.runLater(() -> {
                isRunning = false;
                remainingTimeProperty.set(0);
                
                if (onTimeExpired != null) {
                    onTimeExpired.run();
                }
            });
        }, durationMinutes, TimeUnit.MINUTES);
    }
    
    /**
     * 关闭时间管理器
     */
    public void shutdown() {
        stopFocusMode();
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 获取学习模式结束时间
     * @return 结束时间戳
     */
    public long getFocusModeEndTime() {
        return focusModeEndTime;
    }
    
    /**
     * 从保存的结束时间恢复倒计时
     * @param endTime 结束时间戳
     */
    public void restoreFromEndTime(long endTime) {
        this.focusModeEndTime = endTime;
        long remaining = getRemainingTime();
        
        if (remaining > 0) {
            this.isRunning = true;
            startCountdownTask();
            
            // 计算剩余分钟数并启动自动退出任务
            int remainingMinutes = (int) Math.ceil(remaining / (60.0 * 1000));
            startAutoExitTask(remainingMinutes);
        } else {
            this.isRunning = false;
            remainingTimeProperty.set(0);
        }
    }
} 