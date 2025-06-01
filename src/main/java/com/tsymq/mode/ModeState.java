package com.tsymq.mode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 模式状态数据类
 * 用于存储和传递模式相关的状态信息
 */
public class ModeState {
    
    public enum Mode {
        NORMAL("普通模式"),
        FOCUS("学习模式");
        
        private final String displayName;
        
        Mode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final Mode currentMode;
    private final long focusModeEndTime;
    private final int focusDurationMinutes;
    private final long modeStartTime;
    
    @JsonCreator
    public ModeState(
            @JsonProperty("currentMode") Mode currentMode,
            @JsonProperty("focusModeEndTime") long focusModeEndTime,
            @JsonProperty("focusDurationMinutes") int focusDurationMinutes,
            @JsonProperty("modeStartTime") long modeStartTime) {
        this.currentMode = currentMode;
        this.focusModeEndTime = focusModeEndTime;
        this.focusDurationMinutes = focusDurationMinutes;
        this.modeStartTime = modeStartTime;
    }
    
    // 创建普通模式状态
    public static ModeState createNormalMode() {
        return new ModeState(Mode.NORMAL, 0, 0, System.currentTimeMillis());
    }
    
    // 创建学习模式状态
    public static ModeState createFocusMode(int durationMinutes) {
        long currentTime = System.currentTimeMillis();
        long endTime = currentTime + (durationMinutes * 60 * 1000L);
        return new ModeState(Mode.FOCUS, endTime, durationMinutes, currentTime);
    }
    
    // Getters
    public Mode getCurrentMode() {
        return currentMode;
    }
    
    public long getFocusModeEndTime() {
        return focusModeEndTime;
    }
    
    public int getFocusDurationMinutes() {
        return focusDurationMinutes;
    }
    
    public long getModeStartTime() {
        return modeStartTime;
    }
    
    // 计算剩余时间（毫秒）
    public long getRemainingTimeMs() {
        if (currentMode != Mode.FOCUS) {
            return 0;
        }
        return Math.max(0, focusModeEndTime - System.currentTimeMillis());
    }
    
    // 检查学习模式是否已过期
    public boolean isFocusModeExpired() {
        return currentMode == Mode.FOCUS && getRemainingTimeMs() <= 0;
    }
    
    @Override
    public String toString() {
        return "ModeState{" +
                "currentMode=" + currentMode +
                ", focusModeEndTime=" + focusModeEndTime +
                ", focusDurationMinutes=" + focusDurationMinutes +
                ", modeStartTime=" + modeStartTime +
                '}';
    }
} 