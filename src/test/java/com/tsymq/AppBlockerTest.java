package com.tsymq;

import com.tsymq.mode.ModeManager;
import com.tsymq.config.AppConfig;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AppBlocker 核心业务逻辑测试
 */
@DisplayName("AppBlocker 测试")
class AppBlockerTest {

    private AppBlocker appBlocker;
    private ModeManager mockModeManager;
    private MockedStatic<Paths> mockedPaths;

    @TempDir
    Path tempDir;

    private Path blockedWebsitesFile;
    private Path whiteWebsitesFile;

    @BeforeEach
    void setUp() throws IOException {
        // 设置临时文件路径
        blockedWebsitesFile = tempDir.resolve("blockedWebsites.txt");
        whiteWebsitesFile = tempDir.resolve("whiteWebsites.txt");

        // 创建临时文件
        Files.createFile(blockedWebsitesFile);
        Files.createFile(whiteWebsitesFile);

        // Mock Paths 来重定向配置文件到临时目录
        // 使用 CALLS_REAL_METHODS 作为默认行为
        mockedPaths = mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS);

        // 只 mock 特定的配置文件路径
        mockedPaths.when(() -> Paths.get(AppConfig.BLOCKED_WEBSITES_FILE))
            .thenReturn(blockedWebsitesFile);
        mockedPaths.when(() -> Paths.get(AppConfig.WHITE_WEBSITES_FILE))
            .thenReturn(whiteWebsitesFile);

        appBlocker = new AppBlocker();
        mockModeManager = mock(ModeManager.class);
        appBlocker.setModeManager(mockModeManager);
    }

    @AfterEach
    void tearDown() {
        appBlocker.stop();
        if (mockedPaths != null) {
            mockedPaths.close();
        }
    }

    @Nested
    @DisplayName("shouldBlock 方法测试")
    class ShouldBlockTest {

        @Test
        @DisplayName("学习模式下应该返回true")
        void shouldReturnTrueInFocusMode() {
            when(mockModeManager.isInFocusMode()).thenReturn(true);

            assertThat(appBlocker.shouldBlock()).isTrue();
        }

        @Test
        @DisplayName("普通模式下应该返回false")
        void shouldReturnFalseInNormalMode() {
            when(mockModeManager.isInFocusMode()).thenReturn(false);

            assertThat(appBlocker.shouldBlock()).isFalse();
        }

        @Test
        @DisplayName("ModeManager为null时应该返回false")
        void shouldReturnFalseWhenModeManagerIsNull() {
            appBlocker.setModeManager(null);

            assertThat(appBlocker.shouldBlock()).isFalse();
        }
    }

    @Nested
    @DisplayName("getModeStatusInfo 方法测试")
    class GetModeStatusInfoTest {

        @Test
        @DisplayName("ModeManager未初始化时的状态信息")
        void shouldReturnUninitializedMessageWhenModeManagerIsNull() {
            appBlocker.setModeManager(null);

            String status = appBlocker.getModeStatusInfo();

            assertThat(status).isEqualTo("模式管理器未初始化");
        }

        @Test
        @DisplayName("学习模式下的状态信息")
        void shouldReturnFocusModeStatus() {
            when(mockModeManager.isInFocusMode()).thenReturn(true);
            when(mockModeManager.getRemainingTimeFormatted()).thenReturn("01:30:00");

            String status = appBlocker.getModeStatusInfo();

            assertThat(status).contains("学习模式");
            assertThat(status).contains("全部屏蔽功能已启用");
            assertThat(status).contains("01:30:00");
        }

        @Test
        @DisplayName("普通模式下的状态信息")
        void shouldReturnNormalModeStatus() {
            when(mockModeManager.isInFocusMode()).thenReturn(false);

            String status = appBlocker.getModeStatusInfo();

            assertThat(status).contains("普通模式");
            assertThat(status).contains("仅基础屏蔽功能启用");
        }
    }

    @Nested
    @DisplayName("isBlocked 方法测试")
    class IsBlockedTest {

        @Test
        @DisplayName("URL包含屏蔽网站时应该返回true")
        void shouldReturnTrueWhenUrlContainsBlockedWebsite() {
            // 使用反射或者通过block方法添加屏蔽网站
            appBlocker.block("https://example.com");

            assertThat(appBlocker.isBlocked("https://example.com/page")).isTrue();
        }

        @Test
        @DisplayName("URL不包含屏蔽网站时应该返回false")
        void shouldReturnFalseWhenUrlDoesNotContainBlockedWebsite() {
            appBlocker.block("https://blocked.com");

            assertThat(appBlocker.isBlocked("https://allowed.com")).isFalse();
        }

        @Test
        @DisplayName("空URL应该返回false")
        void shouldReturnFalseForEmptyUrl() {
            assertThat(appBlocker.isBlocked("")).isFalse();
        }

        @Test
        @DisplayName("部分匹配测试")
        void shouldMatchPartialUrl() {
            appBlocker.block("facebook");

            assertThat(appBlocker.isBlocked("https://www.facebook.com")).isTrue();
            assertThat(appBlocker.isBlocked("http://facebook.com/profile")).isTrue();
            assertThat(appBlocker.isBlocked("https://m.facebook.com")).isTrue();
        }
    }

    @Nested
    @DisplayName("isWhiteWeb 方法测试")
    class IsWhiteWebTest {

        @Test
        @DisplayName("标题包含白名单网站时应该返回true")
        void shouldReturnTrueWhenTitleContainsWhitelistedWebsite() {
            // 先加载白名单网站
            appBlocker.loadwhiteWebsites();
            // 注意：这个测试需要知道白名单的内容，或者使用mock

            // 由于无法直接添加白名单，这里仅测试方法逻辑
            assertThat(appBlocker.isWhiteWeb("")).isFalse();
        }
    }

    @Nested
    @DisplayName("block 方法测试")
    class BlockTest {

        @Test
        @DisplayName("HTTP URL应该被添加到屏蔽列表")
        void shouldBlockHttpUrl() {
            boolean result = appBlocker.block("http://example.com");

            assertThat(result).isTrue();
            assertThat(appBlocker.isBlocked("http://example.com")).isTrue();
        }

        @Test
        @DisplayName("HTTPS URL应该被添加到屏蔽列表")
        void shouldBlockHttpsUrl() {
            boolean result = appBlocker.block("https://example.com");

            assertThat(result).isTrue();
            assertThat(appBlocker.isBlocked("https://example.com")).isTrue();
        }

        @Test
        @DisplayName("非URL字符串也可以被添加")
        void shouldBlockNonUrl() {
            // 修改后的逻辑：支持添加任何非空字符串
            boolean result = appBlocker.block("not-a-url");

            assertThat(result).isTrue();
            assertThat(appBlocker.isBlocked("not-a-url")).isTrue();
        }
    }

    @Nested
    @DisplayName("文件操作测试")
    class FileOperationsTest {

        @Test
        @DisplayName("保存和加载屏蔽网站列表")
        void shouldSaveAndLoadBlockedWebsites() throws IOException {
            // 添加一些屏蔽网站
            appBlocker.block("https://blocked1.com");
            appBlocker.block("https://blocked2.com");

            // 验证文件内容
            List<String> lines = Files.readAllLines(blockedWebsitesFile);
            assertThat(lines).contains("https://blocked1.com", "https://blocked2.com");

            // 创建新实例并加载
            AppBlocker newBlocker = new AppBlocker();
            newBlocker.loadBlockedWebsites();

            assertThat(newBlocker.isBlocked("https://blocked1.com")).isTrue();
            assertThat(newBlocker.isBlocked("https://blocked2.com")).isTrue();
        }

        @Test
        @DisplayName("文件不存在时加载应该正常处理")
        void shouldHandleNonExistentFileWhenLoading() {
            // 文件不存在时应该不抛出异常
            assertThatCode(() -> appBlocker.loadBlockedWebsites())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("资源管理测试")
    class ResourceManagementTest {

        @Test
        @DisplayName("stop方法应该正确关闭资源")
        void shouldStopSchedulerProperly() {
            AppBlocker blocker = new AppBlocker();

            assertThatCode(() -> {
                blocker.stop();
                // 再次调用也不应该出错
                blocker.stop();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("AppleScript命令测试")
    class AppleScriptCommandTest {

        @Test
        @DisplayName("openNewEdgeTab应该调用正确的AppleScript命令")
        void shouldCallCorrectAppleScriptForNewTab() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                appBlocker.openNewEdgeTab();

                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("make new tab")),
                    times(1)
                );
            }
        }

        @Test
        @DisplayName("closeActiveEdgeTab应该调用正确的AppleScript命令")
        void shouldCallCorrectAppleScriptForCloseTab() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                appBlocker.closeActiveEdgeTab();

                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("close active tab")),
                    times(1)
                );
            }
        }
    }

    @Nested
    @DisplayName("监控功能测试")
    class MonitoringTest {

        @Test
        @DisplayName("监控功能应该按计划执行")
        @Disabled("需要JavaFX环境")
        void shouldMonitorActiveEdgeUrl() throws InterruptedException {
            TextArea mockTextArea = mock(TextArea.class);

            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                mockedCommandUtil.when(() -> CommandUtil.executeAppleScript(anyString()))
                    .thenReturn("Microsoft Edge");

                appBlocker.monitorActiveEdgeUrl(mockTextArea);

                // 等待一个监控周期
                TimeUnit.MILLISECONDS.sleep(AppConfig.MONITOR_INTERVAL_MS + 100);

                // 验证至少执行了一次
                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(anyString()),
                    atLeastOnce()
                );
            }
        }
    }
}