package com.tsymq.mode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.*;

/**
 * ModeState 测试类
 */
@DisplayName("模式状态测试")
class ModeStateTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("模式创建测试")
    class ModeCreationTest {

        @Test
        @DisplayName("应该正确创建普通模式状态")
        void shouldCreateNormalModeCorrectly() {
            ModeState normalMode = ModeState.createNormalMode();

            assertThat(normalMode)
                .isNotNull()
                .extracting(
                    ModeState::getCurrentMode,
                    ModeState::getFocusModeEndTime,
                    ModeState::getFocusDurationMinutes
                )
                .containsExactly(
                    ModeState.Mode.NORMAL,
                    0L,
                    0
                );
        }

        @Test
        @DisplayName("应该正确创建学习模式状态")
        void shouldCreateFocusModeCorrectly() {
            int duration = 60; // 60分钟
            long beforeCreate = System.currentTimeMillis();

            ModeState focusMode = ModeState.createFocusMode(duration);

            long afterCreate = System.currentTimeMillis();

            assertThat(focusMode).isNotNull();
            assertThat(focusMode.getCurrentMode()).isEqualTo(ModeState.Mode.FOCUS);
            assertThat(focusMode.getFocusDurationMinutes()).isEqualTo(duration);

            // 验证结束时间是否正确计算
            long expectedEndTime = beforeCreate + (duration * 60 * 1000L);
            assertThat(focusMode.getFocusModeEndTime())
                .isGreaterThanOrEqualTo(expectedEndTime)
                .isLessThanOrEqualTo(afterCreate + (duration * 60 * 1000L));
        }

        @Test
        @DisplayName("应该正确设置模式开始时间")
        void shouldSetModeStartTimeCorrectly() {
            long beforeCreate = System.currentTimeMillis();

            ModeState state = ModeState.createNormalMode();

            long afterCreate = System.currentTimeMillis();

            assertThat(state.getModeStartTime())
                .isGreaterThanOrEqualTo(beforeCreate)
                .isLessThanOrEqualTo(afterCreate);
        }
    }

    @Nested
    @DisplayName("模式枚举测试")
    class ModeEnumTest {

        @Test
        @DisplayName("应该有正确的显示名称")
        void shouldHaveCorrectDisplayNames() {
            assertThat(ModeState.Mode.NORMAL.getDisplayName()).isEqualTo("普通模式");
            assertThat(ModeState.Mode.FOCUS.getDisplayName()).isEqualTo("学习模式");
        }

        @Test
        @DisplayName("应该包含所有预期的模式")
        void shouldContainAllExpectedModes() {
            assertThat(ModeState.Mode.values())
                .hasSize(2)
                .containsExactly(ModeState.Mode.NORMAL, ModeState.Mode.FOCUS);
        }
    }

    @Nested
    @DisplayName("剩余时间计算测试")
    class RemainingTimeTest {

        @Test
        @DisplayName("普通模式的剩余时间应该为0")
        void normalModeShouldHaveZeroRemainingTime() {
            ModeState normalMode = ModeState.createNormalMode();

            assertThat(normalMode.getRemainingTimeMs()).isEqualTo(0);
        }

        @Test
        @DisplayName("学习模式应该有正确的剩余时间")
        void focusModeShouldHaveCorrectRemainingTime() {
            int durationMinutes = 30;
            ModeState focusMode = ModeState.createFocusMode(durationMinutes);

            long remainingTime = focusMode.getRemainingTimeMs();
            long expectedTime = durationMinutes * 60 * 1000L;

            // 由于执行时间的关系，实际剩余时间会略少于期望时间
            assertThat(remainingTime)
                .isPositive()
                .isLessThanOrEqualTo(expectedTime)
                .isGreaterThan(expectedTime - 1000); // 允许1秒的误差
        }

        @Test
        @DisplayName("过期的学习模式剩余时间应该为0")
        void expiredFocusModeShouldHaveZeroRemainingTime() {
            // 创建一个已经过期的学习模式
            long pastEndTime = System.currentTimeMillis() - 10000; // 10秒前
            ModeState expiredMode = new ModeState(
                ModeState.Mode.FOCUS,
                pastEndTime,
                30,
                pastEndTime - (30 * 60 * 1000L)
            );

            assertThat(expiredMode.getRemainingTimeMs()).isEqualTo(0);
            assertThat(expiredMode.isFocusModeExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("过期检查测试")
    class ExpirationTest {

        @Test
        @DisplayName("普通模式不应该被认为是过期的")
        void normalModeShouldNotBeExpired() {
            ModeState normalMode = ModeState.createNormalMode();

            assertThat(normalMode.isFocusModeExpired()).isFalse();
        }

        @Test
        @DisplayName("新创建的学习模式不应该是过期的")
        void newFocusModeShouldNotBeExpired() {
            ModeState focusMode = ModeState.createFocusMode(60);

            assertThat(focusMode.isFocusModeExpired()).isFalse();
        }

        @Test
        @DisplayName("结束时间已过的学习模式应该是过期的")
        void focusModeWithPastEndTimeShouldBeExpired() {
            long pastEndTime = System.currentTimeMillis() - 1000;
            ModeState expiredMode = new ModeState(
                ModeState.Mode.FOCUS,
                pastEndTime,
                30,
                pastEndTime - (30 * 60 * 1000L)
            );

            assertThat(expiredMode.isFocusModeExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("JSON序列化测试")
    class JsonSerializationTest {

        @Test
        @DisplayName("应该正确序列化和反序列化普通模式")
        void shouldSerializeAndDeserializeNormalMode() throws Exception {
            ModeState originalMode = ModeState.createNormalMode();

            String json = objectMapper.writeValueAsString(originalMode);
            ModeState deserializedMode = objectMapper.readValue(json, ModeState.class);

            assertThat(deserializedMode.getCurrentMode()).isEqualTo(originalMode.getCurrentMode());
            assertThat(deserializedMode.getFocusModeEndTime()).isEqualTo(originalMode.getFocusModeEndTime());
            assertThat(deserializedMode.getFocusDurationMinutes()).isEqualTo(originalMode.getFocusDurationMinutes());
            assertThat(deserializedMode.getModeStartTime()).isEqualTo(originalMode.getModeStartTime());
        }

        @Test
        @DisplayName("应该正确序列化和反序列化学习模式")
        void shouldSerializeAndDeserializeFocusMode() throws Exception {
            ModeState originalMode = ModeState.createFocusMode(45);

            String json = objectMapper.writeValueAsString(originalMode);
            ModeState deserializedMode = objectMapper.readValue(json, ModeState.class);

            assertThat(deserializedMode.getCurrentMode()).isEqualTo(originalMode.getCurrentMode());
            assertThat(deserializedMode.getFocusModeEndTime()).isEqualTo(originalMode.getFocusModeEndTime());
            assertThat(deserializedMode.getFocusDurationMinutes()).isEqualTo(originalMode.getFocusDurationMinutes());
            assertThat(deserializedMode.getModeStartTime()).isEqualTo(originalMode.getModeStartTime());
        }

        @Test
        @DisplayName("JsonIgnore注解的字段不应该被序列化")
        void jsonIgnoredFieldsShouldNotBeSerialized() throws Exception {
            ModeState mode = ModeState.createFocusMode(30);
            String json = objectMapper.writeValueAsString(mode);

            assertThat(json)
                .doesNotContain("remainingTimeMs")
                .doesNotContain("focusModeExpired");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTest {

        @Test
        @DisplayName("应该返回包含所有字段的字符串")
        void shouldReturnStringWithAllFields() {
            ModeState mode = ModeState.createFocusMode(60);
            String str = mode.toString();

            assertThat(str)
                .contains("ModeState")
                .contains("currentMode=FOCUS")
                .contains("focusDurationMinutes=60")
                .contains("focusModeEndTime=")
                .contains("modeStartTime=");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("零分钟学习模式应该立即过期")
        void zeroMinuteFocusModeShouldExpireImmediately() throws InterruptedException {
            ModeState zeroMode = ModeState.createFocusMode(0);

            // 等待一小段时间确保过期
            Thread.sleep(10);

            assertThat(zeroMode.getRemainingTimeMs()).isEqualTo(0);
            assertThat(zeroMode.isFocusModeExpired()).isTrue();
        }

        @Test
        @DisplayName("非常长的学习模式应该正确处理")
        void veryLongFocusModeShouldBeHandledCorrectly() {
            int veryLongDuration = 999999; // 非常长的时间
            ModeState longMode = ModeState.createFocusMode(veryLongDuration);

            assertThat(longMode.getFocusDurationMinutes()).isEqualTo(veryLongDuration);
            assertThat(longMode.getRemainingTimeMs()).isPositive();
            assertThat(longMode.isFocusModeExpired()).isFalse();
        }
    }
}