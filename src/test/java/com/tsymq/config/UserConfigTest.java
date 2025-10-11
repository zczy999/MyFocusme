package com.tsymq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.*;

/**
 * UserConfig 测试类
 */
@DisplayName("用户配置测试")
class UserConfigTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("配置创建测试")
    class ConfigCreationTest {

        @Test
        @DisplayName("应该正确创建默认配置")
        void shouldCreateDefaultConfigCorrectly() {
            UserConfig config = UserConfig.createDefault();

            assertThat(config).isNotNull();
            assertThat(config.getDefaultFocusDuration()).isEqualTo(AppConfig.DEFAULT_FOCUS_DURATION_MINUTES);
            assertThat(config.isEnableNotifications()).isTrue();
            assertThat(config.isEnableSounds()).isTrue();
            assertThat(config.getTheme()).isEqualTo("default");
        }

        @Test
        @DisplayName("应该正确创建自定义配置")
        void shouldCreateCustomConfigCorrectly() {
            int duration = 45;
            boolean notifications = false;
            boolean sounds = true;
            String theme = "dark";

            UserConfig config = new UserConfig(duration, notifications, sounds, theme);

            assertThat(config.getDefaultFocusDuration()).isEqualTo(duration);
            assertThat(config.isEnableNotifications()).isEqualTo(notifications);
            assertThat(config.isEnableSounds()).isEqualTo(sounds);
            assertThat(config.getTheme()).isEqualTo(theme);
        }
    }

    @Nested
    @DisplayName("配置修改测试")
    class ConfigModificationTest {

        private UserConfig baseConfig;

        @BeforeEach
        void setUp() {
            baseConfig = UserConfig.createDefault();
        }

        @Test
        @DisplayName("应该正确修改默认焦点时长")
        void shouldModifyDefaultFocusDuration() {
            int newDuration = 90;
            UserConfig modifiedConfig = baseConfig.withDefaultFocusDuration(newDuration);

            assertThat(modifiedConfig.getDefaultFocusDuration()).isEqualTo(newDuration);
            // 其他属性应该保持不变
            assertThat(modifiedConfig.isEnableNotifications()).isEqualTo(baseConfig.isEnableNotifications());
            assertThat(modifiedConfig.isEnableSounds()).isEqualTo(baseConfig.isEnableSounds());
            assertThat(modifiedConfig.getTheme()).isEqualTo(baseConfig.getTheme());
        }

        @Test
        @DisplayName("应该正确修改通知设置")
        void shouldModifyNotificationSetting() {
            UserConfig modifiedConfig = baseConfig.withNotifications(false);

            assertThat(modifiedConfig.isEnableNotifications()).isFalse();
            // 其他属性应该保持不变
            assertThat(modifiedConfig.getDefaultFocusDuration()).isEqualTo(baseConfig.getDefaultFocusDuration());
            assertThat(modifiedConfig.isEnableSounds()).isEqualTo(baseConfig.isEnableSounds());
            assertThat(modifiedConfig.getTheme()).isEqualTo(baseConfig.getTheme());
        }

        @Test
        @DisplayName("应该正确修改声音设置")
        void shouldModifySoundSetting() {
            UserConfig modifiedConfig = baseConfig.withSounds(false);

            assertThat(modifiedConfig.isEnableSounds()).isFalse();
            // 其他属性应该保持不变
            assertThat(modifiedConfig.getDefaultFocusDuration()).isEqualTo(baseConfig.getDefaultFocusDuration());
            assertThat(modifiedConfig.isEnableNotifications()).isEqualTo(baseConfig.isEnableNotifications());
            assertThat(modifiedConfig.getTheme()).isEqualTo(baseConfig.getTheme());
        }

        @Test
        @DisplayName("应该正确修改主题")
        void shouldModifyTheme() {
            String newTheme = "dark";
            UserConfig modifiedConfig = baseConfig.withTheme(newTheme);

            assertThat(modifiedConfig.getTheme()).isEqualTo(newTheme);
            // 其他属性应该保持不变
            assertThat(modifiedConfig.getDefaultFocusDuration()).isEqualTo(baseConfig.getDefaultFocusDuration());
            assertThat(modifiedConfig.isEnableNotifications()).isEqualTo(baseConfig.isEnableNotifications());
            assertThat(modifiedConfig.isEnableSounds()).isEqualTo(baseConfig.isEnableSounds());
        }

        @Test
        @DisplayName("应该支持链式修改")
        void shouldSupportChainedModifications() {
            UserConfig modifiedConfig = baseConfig
                .withDefaultFocusDuration(120)
                .withNotifications(false)
                .withSounds(false)
                .withTheme("dark");

            assertThat(modifiedConfig.getDefaultFocusDuration()).isEqualTo(120);
            assertThat(modifiedConfig.isEnableNotifications()).isFalse();
            assertThat(modifiedConfig.isEnableSounds()).isFalse();
            assertThat(modifiedConfig.getTheme()).isEqualTo("dark");
        }
    }

    @Nested
    @DisplayName("不可变性测试")
    class ImmutabilityTest {

        @Test
        @DisplayName("修改方法应该返回新实例而不是修改原实例")
        void modificationMethodsShouldReturnNewInstance() {
            UserConfig original = UserConfig.createDefault();
            UserConfig modified = original.withDefaultFocusDuration(100);

            assertThat(modified).isNotSameAs(original);
            assertThat(original.getDefaultFocusDuration()).isEqualTo(AppConfig.DEFAULT_FOCUS_DURATION_MINUTES);
            assertThat(modified.getDefaultFocusDuration()).isEqualTo(100);
        }

        @Test
        @DisplayName("多次修改应该每次都创建新实例")
        void multipleModificationsShouldCreateNewInstances() {
            UserConfig config1 = UserConfig.createDefault();
            UserConfig config2 = config1.withNotifications(false);
            UserConfig config3 = config2.withSounds(false);
            UserConfig config4 = config3.withTheme("dark");

            assertThat(config1).isNotSameAs(config2);
            assertThat(config2).isNotSameAs(config3);
            assertThat(config3).isNotSameAs(config4);

            // 验证每个实例的值都是独立的
            assertThat(config1.isEnableNotifications()).isTrue();
            assertThat(config2.isEnableNotifications()).isFalse();
            assertThat(config2.isEnableSounds()).isTrue();
            assertThat(config3.isEnableSounds()).isFalse();
            assertThat(config3.getTheme()).isEqualTo("default");
            assertThat(config4.getTheme()).isEqualTo("dark");
        }
    }

    @Nested
    @DisplayName("JSON序列化测试")
    class JsonSerializationTest {

        @Test
        @DisplayName("应该正确序列化和反序列化默认配置")
        void shouldSerializeAndDeserializeDefaultConfig() throws Exception {
            UserConfig original = UserConfig.createDefault();

            String json = objectMapper.writeValueAsString(original);
            UserConfig deserialized = objectMapper.readValue(json, UserConfig.class);

            assertThat(deserialized.getDefaultFocusDuration()).isEqualTo(original.getDefaultFocusDuration());
            assertThat(deserialized.isEnableNotifications()).isEqualTo(original.isEnableNotifications());
            assertThat(deserialized.isEnableSounds()).isEqualTo(original.isEnableSounds());
            assertThat(deserialized.getTheme()).isEqualTo(original.getTheme());
        }

        @Test
        @DisplayName("应该正确序列化和反序列化自定义配置")
        void shouldSerializeAndDeserializeCustomConfig() throws Exception {
            UserConfig original = new UserConfig(75, false, true, "dark");

            String json = objectMapper.writeValueAsString(original);
            UserConfig deserialized = objectMapper.readValue(json, UserConfig.class);

            assertThat(deserialized.getDefaultFocusDuration()).isEqualTo(75);
            assertThat(deserialized.isEnableNotifications()).isFalse();
            assertThat(deserialized.isEnableSounds()).isTrue();
            assertThat(deserialized.getTheme()).isEqualTo("dark");
        }

        @Test
        @DisplayName("JSON应该包含所有配置字段")
        void jsonShouldContainAllConfigFields() throws Exception {
            UserConfig config = UserConfig.createDefault();
            String json = objectMapper.writeValueAsString(config);

            assertThat(json)
                .contains("defaultFocusDuration")
                .contains("enableNotifications")
                .contains("enableSounds")
                .contains("theme");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTest {

        @Test
        @DisplayName("应该返回包含所有字段的字符串")
        void shouldReturnStringWithAllFields() {
            UserConfig config = new UserConfig(60, true, false, "light");
            String str = config.toString();

            assertThat(str)
                .contains("UserConfig")
                .contains("defaultFocusDuration=60")
                .contains("enableNotifications=true")
                .contains("enableSounds=false")
                .contains("theme='light'");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("应该接受零作为焦点时长")
        void shouldAcceptZeroFocusDuration() {
            UserConfig config = new UserConfig(0, true, true, "default");
            assertThat(config.getDefaultFocusDuration()).isEqualTo(0);
        }

        @Test
        @DisplayName("应该接受负数作为焦点时长")
        void shouldAcceptNegativeFocusDuration() {
            UserConfig config = new UserConfig(-10, true, true, "default");
            assertThat(config.getDefaultFocusDuration()).isEqualTo(-10);
        }

        @Test
        @DisplayName("应该接受null作为主题")
        void shouldAcceptNullTheme() {
            UserConfig config = new UserConfig(60, true, true, null);
            assertThat(config.getTheme()).isNull();
        }

        @Test
        @DisplayName("应该接受空字符串作为主题")
        void shouldAcceptEmptyStringTheme() {
            UserConfig config = new UserConfig(60, true, true, "");
            assertThat(config.getTheme()).isEmpty();
        }

        @Test
        @DisplayName("应该接受非常大的焦点时长")
        void shouldAcceptVeryLargeFocusDuration() {
            int veryLarge = Integer.MAX_VALUE;
            UserConfig config = new UserConfig(veryLarge, true, true, "default");
            assertThat(config.getDefaultFocusDuration()).isEqualTo(veryLarge);
        }
    }
}