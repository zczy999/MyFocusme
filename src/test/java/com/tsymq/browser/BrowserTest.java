package com.tsymq.browser;

import com.tsymq.CommandUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * 浏览器抽象层测试
 */
@DisplayName("浏览器适配器测试")
class BrowserTest {

    @Nested
    @DisplayName("BrowserFactory 测试")
    class BrowserFactoryTest {

        @Test
        @DisplayName("应该支持 Microsoft Edge")
        void shouldSupportEdge() {
            Optional<Browser> browser = BrowserFactory.getBrowser("Microsoft Edge");

            assertThat(browser).isPresent();
            assertThat(browser.get()).isInstanceOf(EdgeBrowser.class);
        }

        @Test
        @DisplayName("应该支持 Google Chrome")
        void shouldSupportChrome() {
            Optional<Browser> browser = BrowserFactory.getBrowser("Google Chrome");

            assertThat(browser).isPresent();
            assertThat(browser.get()).isInstanceOf(ChromeBrowser.class);
        }

        @Test
        @DisplayName("应该支持 Safari")
        void shouldSupportSafari() {
            Optional<Browser> browser = BrowserFactory.getBrowser("Safari");

            assertThat(browser).isPresent();
            assertThat(browser.get()).isInstanceOf(SafariBrowser.class);
        }

        @Test
        @DisplayName("应该支持 SunBrowser")
        void shouldSupportSunBrowser() {
            Optional<Browser> browser = BrowserFactory.getBrowser("SunBrowser");

            assertThat(browser).isPresent();
            assertThat(browser.get()).isInstanceOf(SunBrowser.class);
        }

        @Test
        @DisplayName("不支持的浏览器应该返回 empty")
        void shouldReturnEmptyForUnsupportedBrowser() {
            Optional<Browser> browser = BrowserFactory.getBrowser("Firefox");

            assertThat(browser).isEmpty();
        }

        @Test
        @DisplayName("isSupported 应该正确判断")
        void shouldCorrectlyCheckSupport() {
            assertThat(BrowserFactory.isSupported("Microsoft Edge")).isTrue();
            assertThat(BrowserFactory.isSupported("Google Chrome")).isTrue();
            assertThat(BrowserFactory.isSupported("Safari")).isTrue();
            assertThat(BrowserFactory.isSupported("SunBrowser")).isTrue();
            assertThat(BrowserFactory.isSupported("Firefox")).isFalse();
            assertThat(BrowserFactory.isSupported("Arc")).isFalse();
        }

        @Test
        @DisplayName("getSupportedBrowsers 应该返回所有支持的浏览器")
        void shouldReturnAllSupportedBrowsers() {
            Set<String> browsers = BrowserFactory.getSupportedBrowsers();

            assertThat(browsers).containsExactlyInAnyOrder(
                "Microsoft Edge",
                "Google Chrome",
                "Safari",
                "SunBrowser"
            );
        }
    }

    @Nested
    @DisplayName("EdgeBrowser 测试")
    class EdgeBrowserTest {

        private Browser edge;

        @BeforeEach
        void setUp() {
            edge = new EdgeBrowser();
        }

        @Test
        @DisplayName("getName 应该返回正确的应用名称")
        void shouldReturnCorrectName() {
            assertThat(edge.getName()).isEqualTo("Microsoft Edge");
        }

        @Test
        @DisplayName("getActiveTabUrl 应该调用正确的 AppleScript")
        void shouldCallCorrectAppleScriptForUrl() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                mockedCommandUtil.when(() -> CommandUtil.executeAppleScript(anyString()))
                    .thenReturn("https://example.com");

                String url = edge.getActiveTabUrl();

                assertThat(url).isEqualTo("https://example.com");
                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("Microsoft Edge")),
                    times(1)
                );
                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("URL of active tab")),
                    times(1)
                );
            }
        }

        @Test
        @DisplayName("closeActiveTab 应该调用正确的 AppleScript")
        void shouldCallCorrectAppleScriptForClose() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                edge.closeActiveTab();

                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("close active tab")),
                    times(1)
                );
            }
        }

        @Test
        @DisplayName("openNewTab 应该调用正确的 AppleScript")
        void shouldCallCorrectAppleScriptForNewTab() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                edge.openNewTab();

                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("make new tab")),
                    times(1)
                );
            }
        }
    }

    @Nested
    @DisplayName("ChromeBrowser 测试")
    class ChromeBrowserTest {

        private Browser chrome;

        @BeforeEach
        void setUp() {
            chrome = new ChromeBrowser();
        }

        @Test
        @DisplayName("getName 应该返回正确的应用名称")
        void shouldReturnCorrectName() {
            assertThat(chrome.getName()).isEqualTo("Google Chrome");
        }

        @Test
        @DisplayName("getActiveTabUrl 应该调用正确的 AppleScript")
        void shouldCallCorrectAppleScriptForUrl() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                mockedCommandUtil.when(() -> CommandUtil.executeAppleScript(anyString()))
                    .thenReturn("https://example.com");

                String url = chrome.getActiveTabUrl();

                assertThat(url).isEqualTo("https://example.com");
                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("Google Chrome")),
                    times(1)
                );
            }
        }

        @Test
        @DisplayName("closeActiveTab 应该调用正确的 AppleScript")
        void shouldCallCorrectAppleScriptForClose() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                chrome.closeActiveTab();

                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("Google Chrome")),
                    times(1)
                );
                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("close active tab")),
                    times(1)
                );
            }
        }
    }

    @Nested
    @DisplayName("SafariBrowser 测试")
    class SafariBrowserTest {

        private Browser safari;

        @BeforeEach
        void setUp() {
            safari = new SafariBrowser();
        }

        @Test
        @DisplayName("getName 应该返回正确的应用名称")
        void shouldReturnCorrectName() {
            assertThat(safari.getName()).isEqualTo("Safari");
        }

        @Test
        @DisplayName("getActiveTabUrl 应该使用 Safari 特有语法")
        void shouldUseSafariSpecificSyntaxForUrl() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                mockedCommandUtil.when(() -> CommandUtil.executeAppleScript(anyString()))
                    .thenReturn("https://example.com");

                safari.getActiveTabUrl();

                // Safari 使用 "current tab" 而不是 "active tab"
                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("current tab")),
                    times(1)
                );
            }
        }

        @Test
        @DisplayName("getActiveTabTitle 应该使用 name 而不是 title")
        void shouldUseSafariSpecificSyntaxForTitle() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                mockedCommandUtil.when(() -> CommandUtil.executeAppleScript(anyString()))
                    .thenReturn("Page Title");

                safari.getActiveTabTitle();

                // Safari 使用 "name of current tab"
                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("name of current tab")),
                    times(1)
                );
            }
        }

        @Test
        @DisplayName("closeActiveTab 应该使用 Safari 特有语法")
        void shouldUseSafariSpecificSyntaxForClose() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                safari.closeActiveTab();

                // Safari 使用 "close current tab"
                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("close current tab")),
                    times(1)
                );
            }
        }
    }

    @Nested
    @DisplayName("SunBrowser 测试")
    class SunBrowserTest {

        private Browser sunBrowser;

        @BeforeEach
        void setUp() {
            sunBrowser = new SunBrowser();
        }

        @Test
        @DisplayName("getName 应该返回正确的应用名称")
        void shouldReturnCorrectName() {
            assertThat(sunBrowser.getName()).isEqualTo("SunBrowser");
        }

        @Test
        @DisplayName("应该继承 ChromiumBrowser")
        void shouldExtendChromiumBrowser() {
            assertThat(sunBrowser).isInstanceOf(ChromiumBrowser.class);
        }

        @Test
        @DisplayName("getActiveTabUrl 应该使用 SunBrowser 名称")
        void shouldUseSunBrowserName() {
            try (MockedStatic<CommandUtil> mockedCommandUtil = mockStatic(CommandUtil.class)) {
                mockedCommandUtil.when(() -> CommandUtil.executeAppleScript(anyString()))
                    .thenReturn("https://example.com");

                sunBrowser.getActiveTabUrl();

                mockedCommandUtil.verify(() ->
                    CommandUtil.executeAppleScript(contains("SunBrowser")),
                    times(1)
                );
            }
        }
    }

    @Nested
    @DisplayName("ChromiumBrowser 继承测试")
    class ChromiumBrowserInheritanceTest {

        @Test
        @DisplayName("EdgeBrowser 应该继承 ChromiumBrowser")
        void edgeShouldExtendChromiumBrowser() {
            assertThat(new EdgeBrowser()).isInstanceOf(ChromiumBrowser.class);
        }

        @Test
        @DisplayName("ChromeBrowser 应该继承 ChromiumBrowser")
        void chromeShouldExtendChromiumBrowser() {
            assertThat(new ChromeBrowser()).isInstanceOf(ChromiumBrowser.class);
        }

        @Test
        @DisplayName("SunBrowser 应该继承 ChromiumBrowser")
        void sunBrowserShouldExtendChromiumBrowser() {
            assertThat(new SunBrowser()).isInstanceOf(ChromiumBrowser.class);
        }

        @Test
        @DisplayName("SafariBrowser 不应该继承 ChromiumBrowser")
        void safariShouldNotExtendChromiumBrowser() {
            assertThat(new SafariBrowser()).isNotInstanceOf(ChromiumBrowser.class);
        }
    }
}
