package com.tsymq.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("时间工具类测试")
class TimeUtilsTest {

    @Nested
    @DisplayName("时间格式化测试")
    class TimeFormattingTest {

        @ParameterizedTest
        @DisplayName("应该正确格式化不同的时间长度")
        @CsvSource({
            "0, 00:00:00",
            "1000, 00:00:01",
            "30000, 00:00:30",
            "60000, 00:01:00",
            "90000, 00:01:30",
            "3600000, 01:00:00",
            "3661000, 01:01:01",
            "7200000, 02:00:00"
        })
        void shouldFormatDurationCorrectly(long durationMs, String expected) {
            String result = TimeUtils.formatDuration(durationMs);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("负数时间应该返回00:00:00")
        void negativeDurationShouldReturnZero() {
            assertThat(TimeUtils.formatDuration(-1000)).isEqualTo("00:00:00");
            assertThat(TimeUtils.formatDuration(-60000)).isEqualTo("00:00:00");
        }

        @Test
        @DisplayName("超大时间值应该正确处理")
        void shouldHandleLargeDurations() {
            long largeValue = 24 * 60 * 60 * 1000L; // 24小时
            String result = TimeUtils.formatDuration(largeValue);
            assertThat(result).matches("\\d{2}:\\d{2}:\\d{2}");
        }

        @ParameterizedTest
        @DisplayName("应该正确格式化简短时间格式")
        @CsvSource({
            "0, 0s",
            "1000, 1s",
            "30000, 30s",
            "60000, 1m",
            "90000, 1m 30s",
            "3600000, 1h",
            "3661000, 1h 1m",
            "7200000, 2h"
        })
        void shouldFormatDurationShortCorrectly(long durationMs, String expected) {
            String result = TimeUtils.formatDurationShort(durationMs);
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("时间转换测试")
    class TimeConversionTest {

        @ParameterizedTest
        @DisplayName("分钟转毫秒应该正确")
        @CsvSource({
            "0, 0",
            "1, 60000",
            "5, 300000",
            "60, 3600000",
            "120, 7200000"
        })
        void shouldConvertMinutesToMillisCorrectly(int minutes, long expectedMs) {
            long result = TimeUtils.minutesToMillis(minutes);
            assertThat(result).isEqualTo(expectedMs);
        }

        @ParameterizedTest
        @DisplayName("毫秒转分钟应该正确")
        @CsvSource({
            "0, 0",
            "60000, 1",
            "300000, 5",
            "3600000, 60",
            "7200000, 120"
        })
        void shouldConvertMillisToMinutesCorrectly(long millis, int expectedMinutes) {
            int result = TimeUtils.millisToMinutes(millis);
            assertThat(result).isEqualTo(expectedMinutes);
        }

        @Test
        @DisplayName("毫秒转分钟应该向下取整")
        void millisToMinutesShouldFloor() {
            assertThat(TimeUtils.millisToMinutes(59999)).isEqualTo(0);
            assertThat(TimeUtils.millisToMinutes(119999)).isEqualTo(1);
            assertThat(TimeUtils.millisToMinutes(179999)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("时间戳格式化测试")
    class TimestampFormattingTest {

        @Test
        @DisplayName("应该正确格式化当前时间")
        void shouldFormatCurrentTimeCorrectly() {
            String result = TimeUtils.formatCurrentTime();
            
            assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        }

        @Test
        @DisplayName("应该正确格式化时间戳")
        void shouldFormatTimestampCorrectly() {
            long timestamp = 1640995200000L; // 2022-01-01 00:00:00 UTC
            String result = TimeUtils.formatTimestamp(timestamp);
            
            assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        }
    }

    @Nested
    @DisplayName("时间范围检查测试")
    class TimeRangeTest {

        @Test
        @DisplayName("应该正确检查时间是否在范围内")
        void shouldCheckTimeRangeCorrectly() {
            long startTime = 1000;
            long endTime = 5000;
            
            assertThat(TimeUtils.isInTimeRange(3000, startTime, endTime)).isTrue();
            assertThat(TimeUtils.isInTimeRange(1000, startTime, endTime)).isTrue();
            assertThat(TimeUtils.isInTimeRange(5000, startTime, endTime)).isTrue();
            assertThat(TimeUtils.isInTimeRange(500, startTime, endTime)).isFalse();
            assertThat(TimeUtils.isInTimeRange(6000, startTime, endTime)).isFalse();
        }

        @Test
        @DisplayName("边界值应该被包含在范围内")
        void boundaryValuesShouldBeIncluded() {
            long startTime = 1000;
            long endTime = 5000;
            
            assertThat(TimeUtils.isInTimeRange(startTime, startTime, endTime)).isTrue();
            assertThat(TimeUtils.isInTimeRange(endTime, startTime, endTime)).isTrue();
        }
    }

    @Nested
    @DisplayName("时间差计算测试")
    class TimeDifferenceTest {

        @Test
        @DisplayName("应该正确计算时间差")
        void shouldCalculateTimeDifferenceCorrectly() {
            long startTime = 1000;
            long endTime = 5000;
            
            long difference = TimeUtils.getTimeDifference(startTime, endTime);
            
            assertThat(difference).isEqualTo(4000);
        }

        @Test
        @DisplayName("时间差应该总是正数")
        void timeDifferenceShouldAlwaysBePositive() {
            long time1 = 5000;
            long time2 = 1000;
            
            long difference1 = TimeUtils.getTimeDifference(time1, time2);
            long difference2 = TimeUtils.getTimeDifference(time2, time1);
            
            assertThat(difference1).isEqualTo(4000).isPositive();
            assertThat(difference2).isEqualTo(4000).isPositive();
        }

        @Test
        @DisplayName("相同时间的差值应该为0")
        void sameTimeDifferenceShouldBeZero() {
            long time = 1000;
            
            long difference = TimeUtils.getTimeDifference(time, time);
            
            assertThat(difference).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("零值应该正确处理")
        void shouldHandleZeroValues() {
            assertThat(TimeUtils.formatDuration(0)).isEqualTo("00:00:00");
            assertThat(TimeUtils.formatDurationShort(0)).isEqualTo("0s");
            assertThat(TimeUtils.minutesToMillis(0)).isEqualTo(0);
            assertThat(TimeUtils.millisToMinutes(0)).isEqualTo(0);
            assertThat(TimeUtils.getTimeDifference(0, 0)).isEqualTo(0);
        }

        @Test
        @DisplayName("最大值应该正确处理")
        void shouldHandleMaxValues() {
            long maxValue = Long.MAX_VALUE;
            
            assertThatCode(() -> TimeUtils.formatDuration(maxValue))
                .doesNotThrowAnyException();
            
            assertThatCode(() -> TimeUtils.formatTimestamp(maxValue))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("负数时间戳应该正确处理")
        void shouldHandleNegativeTimestamps() {
            assertThatCode(() -> TimeUtils.formatTimestamp(-1))
                .doesNotThrowAnyException();
        }
    }
} 