package com.tsymq.mode;

import com.tsymq.config.AppConfig;
import com.tsymq.config.ConfigManager;
import com.tsymq.utils.TimeUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 模式管理核心类
 * 负责模式切换、时间管理和状态监控
 */
public class ModeManager {
    
    private final ConfigManager configManager;
    private final ScheduledExecutorService scheduler;
    
    private ModeState currentModeState;
    private Consumer<ModeState> modeChangeListener;
    private Consumer<Long> timeUpdateListener;
    
    public ModeManager() {
        this.configManager = new ConfigManager();
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // 加载初始状态
        this.currentModeState = configManager.loadModeState();
        
        // 启动定时器
        startTimers();
        // 启动每天17:00定时切换
        scheduleDailySwitch(17, 0);
    }
    
    /**
     * 启动定时器
     */
    private void startTimers() {
        // 每秒更新时间显示
        // scheduler.scheduleWithFixedDelay(this::updateTimeDisplay, 0, 1, TimeUnit.SECONDS);
        
        // 每60秒检查模式状态
        scheduler.scheduleWithFixedDelay(this::checkModeStatus, 0, 60, TimeUnit.SECONDS);
    }
    
    /**
     * 更新时间显示
     */
    private void updateTimeDisplay() {
        if (currentModeState.getCurrentMode() == ModeState.Mode.FOCUS) {
            long remainingTime = currentModeState.getRemainingTimeMs();
            if (timeUpdateListener != null) {
                timeUpdateListener.accept(remainingTime);
            }
        }
    }
    
    /**
     * 检查模式状态
     */
    private void checkModeStatus() {
        if (currentModeState.getCurrentMode() == ModeState.Mode.FOCUS) {
            if (currentModeState.isFocusModeExpired()) {
                System.out.println("Focus mode expired, switching to normal mode");
                switchToNormalMode();
            }
        }
    }
    
    /**
     * 切换到学习模式
     * @param durationMinutes 学习时长（分钟）
     * @return 是否切换成功
     */
    public boolean switchToFocusMode(int durationMinutes) {
        // 新增：如果当前时间晚于17:00，则不切换
        java.time.LocalTime now = java.time.LocalTime.now();
        java.time.LocalTime cutoff = java.time.LocalTime.of(17, 0);
        if (now.isAfter(cutoff)) {
            System.out.println("当前已超过17:00，无法切换到学习模式");
            return false;
        }
        if (durationMinutes < AppConfig.MIN_FOCUS_DURATION_MINUTES || 
            durationMinutes > AppConfig.MAX_FOCUS_DURATION_MINUTES) {
            System.err.println("Invalid focus duration: " + durationMinutes + " minutes");
            return false;
        }
        
        if (currentModeState.getCurrentMode() == ModeState.Mode.FOCUS) {
            System.out.println("Already in focus mode");
            return false;
        }
        
        // 创建新的学习模式状态
        ModeState newState = ModeState.createFocusMode(durationMinutes);
        updateModeState(newState);
        
        System.out.println("Switched to focus mode for " + durationMinutes + " minutes");
        return true;
    }
    
    /**
     * 切换到普通模式
     */
    public void switchToNormalMode() {
        if (currentModeState.getCurrentMode() == ModeState.Mode.NORMAL) {
            System.out.println("Already in normal mode");
            return;
        }
        
        // 创建新的普通模式状态
        ModeState newState = ModeState.createNormalMode();
        updateModeState(newState);
        
        System.out.println("Switched to normal mode");
    }
    
    /**
     * 更新模式状态
     * @param newState 新的模式状态
     */
    private void updateModeState(ModeState newState) {
        this.currentModeState = newState;
        // 状态保存功能已禁用，不再保存到文件
        configManager.saveModeState(newState);
        
        // 通知监听器
        if (modeChangeListener != null) {
            modeChangeListener.accept(newState);
        }
    }
    
    /**
     * 获取当前模式
     * @return 当前模式
     */
    public ModeState.Mode getCurrentMode() {
        return currentModeState.getCurrentMode();
    }
    
    /**
     * 检查是否在学习模式
     * @return 是否在学习模式
     */
    public boolean isInFocusMode() {
        return currentModeState.getCurrentMode() == ModeState.Mode.FOCUS;
    }
    
    /**
     * 获取剩余时间（毫秒）
     * @return 剩余时间，如果不在学习模式则返回0
     */
    public long getRemainingTimeMs() {
        return currentModeState.getRemainingTimeMs();
    }
    
    /**
     * 获取剩余时间的格式化字符串
     * @return 格式化的剩余时间
     */
    public String getRemainingTimeFormatted() {
        return TimeUtils.formatDuration(getRemainingTimeMs());
    }
    
    /**
     * 获取当前模式状态
     * @return 当前模式状态
     */
    public ModeState getCurrentModeState() {
        return currentModeState;
    }
    
    /**
     * 设置模式变更监听器
     * @param listener 监听器
     */
    public void setModeChangeListener(Consumer<ModeState> listener) {
        this.modeChangeListener = listener;
    }
    
    /**
     * 设置时间更新监听器
     * @param listener 监听器
     */
    public void setTimeUpdateListener(Consumer<Long> listener) {
        this.timeUpdateListener = listener;
    }
    
    /**
     * 强制刷新状态（已禁用）
     * 由于应用不再保存状态，此功能已禁用
     */
    public void refreshState() {
        // 状态刷新功能已禁用，因为应用不再保存状态
        System.out.println("State refresh disabled - application does not persist state");
    }
    
    /**
     * 获取学习模式的总时长（分钟）
     * @return 学习模式总时长，如果不在学习模式则返回0
     */
    public int getFocusModeDurationMinutes() {
        return currentModeState.getFocusDurationMinutes();
    }
    
    /**
     * 获取已经过的时间（毫秒）
     * @return 已经过的时间
     */
    public long getElapsedTimeMs() {
        if (currentModeState.getCurrentMode() != ModeState.Mode.FOCUS) {
            return 0;
        }
        
        long totalDuration = TimeUtils.minutesToMillis(currentModeState.getFocusDurationMinutes());
        long remaining = getRemainingTimeMs();
        return totalDuration - remaining;
    }
    
    /**
     * 获取学习进度百分比
     * @return 进度百分比（0-100）
     */
    public double getProgressPercentage() {
        if (currentModeState.getCurrentMode() != ModeState.Mode.FOCUS) {
            return 0.0;
        }
        
        long totalDuration = TimeUtils.minutesToMillis(currentModeState.getFocusDurationMinutes());
        long elapsed = getElapsedTimeMs();
        
        if (totalDuration <= 0) {
            return 0.0;
        }
        
        return Math.min(100.0, (elapsed * 100.0) / totalDuration);
    }
    
    /**
     * 每天指定时间定时切换模式（如17:00）
     */
    private void scheduleDailySwitch(int hour, int minute) {
        long delay = computeDelayToNextTime(hour, minute);
        scheduler.schedule(() -> {
            // 每天定时只切换到普通模式
            ModeState newState = ModeState.createNormalMode();
            updateModeState(newState);
            System.out.println("[定时任务] 已切换到普通模式 (NORMAL)");
            // 递归安排下一天的定时切换
            scheduleDailySwitch(hour, minute);
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 计算距离下一个指定时间（如17:00）的毫秒数
     */
    private long computeDelayToNextTime(int hour, int minute) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        java.time.Duration duration = java.time.Duration.between(now, next);
        return duration.toMillis();
    }
    
    /**
     * 关闭模式管理器
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
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
    }
} 