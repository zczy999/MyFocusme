package com.tsymq.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;

@DisplayName("屏蔽网站配置测试")
class BlockedSitesConfigTest {

    @Nested
    @DisplayName("硬编码屏蔽网站列表测试")
    class HardcodedBlockedSitesTest {

        @Test
        @DisplayName("应该返回非空的屏蔽网站集合")
        void shouldReturnNonEmptyBlockedSites() {
            Set<String> blockedSites = BlockedSitesConfig.getHardcodedBlockedSites();
            
            assertThat(blockedSites)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(30); // 至少应该有30个网站
        }

        @Test
        @DisplayName("应该包含关键的屏蔽网站")
        void shouldContainKeyBlockedSites() {
            Set<String> blockedSites = BlockedSitesConfig.getHardcodedBlockedSites();
            
            assertThat(blockedSites)
                .contains(
                    "18comic",
                    "pornhub",
                    "xvideos",
                    "javbus",
                    "missav"
                );
        }

        @Test
        @DisplayName("应该包含不同类型的屏蔽内容")
        void shouldContainDifferentTypesOfBlockedContent() {
            Set<String> blockedSites = BlockedSitesConfig.getHardcodedBlockedSites();
            
            // 日本成人网站
            assertThat(blockedSites).contains("javbus", "javlibrary", "jable");
            
            // 国际知名网站
            assertThat(blockedSites).contains("pornhub", "xvideos", "xnxx");
            
            // 中文网站
            assertThat(blockedSites).contains("91porn", "caoliu");
            
            // 直播网站
            assertThat(blockedSites).contains("chaturbate", "myfreecams");
            
            // 关键词
            assertThat(blockedSites).contains("porn", "xxx", "erotic");
        }

        @Test
        @DisplayName("屏蔽网站列表不应包含重复项")
        void shouldNotContainDuplicates() {
            Set<String> blockedSites = BlockedSitesConfig.getHardcodedBlockedSites();
            Set<String> originalSites = new HashSet<>(blockedSites);
            
            assertThat(blockedSites).hasSize(originalSites.size());
        }

        @Test
        @DisplayName("所有屏蔽网站应该是小写字符串")
        void allBlockedSitesShouldBeLowercase() {
            Set<String> blockedSites = BlockedSitesConfig.getHardcodedBlockedSites();
            
            for (String site : blockedSites) {
                assertThat(site)
                    .isEqualTo(site.toLowerCase())
                    .doesNotContain(" ", "\t", "\n", "\r");
            }
        }
    }

    @Nested
    @DisplayName("URL屏蔽检查测试")
    class UrlBlockingTest {

        @ParameterizedTest
        @DisplayName("应该屏蔽包含屏蔽关键词的URL")
        @ValueSource(strings = {
            "https://18comic.vip/album/123456",
            "https://18comic.org/manga/test",
            "https://www.pornhub.com/video/123",
            "https://javbus.com/JUL-123",
            "https://missav.com/dm123/abc-456",
            "https://example.com/porn/video",
            "https://test.xxx.com",
            "https://site.com/erotic-content"
        })
        void shouldBlockUrlsContainingBlockedKeywords(String url) {
            assertThat(BlockedSitesConfig.isHardcodedBlocked(url))
                .as("URL %s should be blocked", url)
                .isTrue();
        }

        @ParameterizedTest
        @DisplayName("不应该屏蔽正常网站的URL")
        @ValueSource(strings = {
            "https://www.google.com",
            "https://www.baidu.com",
            "https://github.com/user/repo",
            "https://stackoverflow.com/questions/123",
            "https://www.youtube.com/watch?v=abc123",
            "https://www.bilibili.com/video/BV123",
            "https://www.zhihu.com/question/123"
        })
        void shouldNotBlockNormalUrls(String url) {
            assertThat(BlockedSitesConfig.isHardcodedBlocked(url))
                .as("URL %s should not be blocked", url)
                .isFalse();
        }

        @ParameterizedTest
        @DisplayName("URL检查应该不区分大小写")
        @CsvSource({
            "https://18COMIC.vip/album/123, true",
            "https://PORNHUB.com/video/123, true",
            "https://JAVBUS.com/test, true",
            "https://www.GOOGLE.com, false",
            "https://GITHUB.com/user/repo, false"
        })
        void urlCheckShouldBeCaseInsensitive(String url, boolean shouldBeBlocked) {
            assertThat(BlockedSitesConfig.isHardcodedBlocked(url))
                .as("URL %s blocking result should be %s", url, shouldBeBlocked)
                .isEqualTo(shouldBeBlocked);
        }

        @Test
        @DisplayName("空URL或null应该返回false")
        void shouldReturnFalseForNullOrEmptyUrl() {
            assertThat(BlockedSitesConfig.isHardcodedBlocked(null)).isFalse();
            assertThat(BlockedSitesConfig.isHardcodedBlocked("")).isFalse();
            assertThat(BlockedSitesConfig.isHardcodedBlocked("   ")).isFalse();
        }

        @Test
        @DisplayName("应该检查URL中的子字符串匹配")
        void shouldCheckSubstringMatching() {
            // 测试子域名匹配
            assertThat(BlockedSitesConfig.isHardcodedBlocked("https://sub.pornhub.com")).isTrue();
            assertThat(BlockedSitesConfig.isHardcodedBlocked("https://pornhub.example.com")).isTrue();
            
            // 测试路径匹配
            assertThat(BlockedSitesConfig.isHardcodedBlocked("https://example.com/javbus/test")).isTrue();
            assertThat(BlockedSitesConfig.isHardcodedBlocked("https://site.com/path/18comic/album")).isTrue();
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityMethodsTest {

        @Test
        @DisplayName("应该返回正确的屏蔽网站数量")
        void shouldReturnCorrectBlockedSitesCount() {
            Set<String> blockedSites = BlockedSitesConfig.getHardcodedBlockedSites();
            int expectedCount = blockedSites.size();
            
            assertThat(BlockedSitesConfig.getBlockedSitesCount())
                .isEqualTo(expectedCount)
                .isGreaterThan(30);
        }

        @Test
        @DisplayName("应该正确添加运行时屏蔽网站")
        void shouldCorrectlyAddRuntimeBlockedSites() {
            Set<String> additionalSites = Set.of("test1.com", "test2.com", "test3.com");
            Set<String> allSites = BlockedSitesConfig.addRuntimeBlockedSites(additionalSites);
            
            assertThat(allSites)
                .containsAll(BlockedSitesConfig.getHardcodedBlockedSites())
                .containsAll(additionalSites)
                .hasSizeGreaterThanOrEqualTo(
                    BlockedSitesConfig.getBlockedSitesCount() + additionalSites.size()
                );
        }

        @Test
        @DisplayName("添加null的运行时网站应该返回原始列表")
        void shouldReturnOriginalListWhenAddingNullRuntimeSites() {
            Set<String> originalSites = BlockedSitesConfig.getHardcodedBlockedSites();
            Set<String> resultSites = BlockedSitesConfig.addRuntimeBlockedSites(null);
            
            assertThat(resultSites)
                .containsExactlyInAnyOrderElementsOf(originalSites);
        }

        @Test
        @DisplayName("添加空集合应该返回原始列表")
        void shouldReturnOriginalListWhenAddingEmptySet() {
            Set<String> originalSites = BlockedSitesConfig.getHardcodedBlockedSites();
            Set<String> resultSites = BlockedSitesConfig.addRuntimeBlockedSites(new HashSet<>());
            
            assertThat(resultSites)
                .containsExactlyInAnyOrderElementsOf(originalSites);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("应该处理特殊字符的URL")
        void shouldHandleSpecialCharactersInUrl() {
            String urlWithSpecialChars = "https://18comic.vip/album/123?param=value&other=test#section";
            assertThat(BlockedSitesConfig.isHardcodedBlocked(urlWithSpecialChars)).isTrue();
        }

        @Test
        @DisplayName("应该处理非HTTP协议的URL")
        void shouldHandleNonHttpUrls() {
            assertThat(BlockedSitesConfig.isHardcodedBlocked("ftp://pornhub.com/file")).isTrue();
            assertThat(BlockedSitesConfig.isHardcodedBlocked("file:///path/to/javbus/file")).isTrue();
        }

        @Test
        @DisplayName("应该处理包含端口号的URL")
        void shouldHandleUrlsWithPorts() {
            assertThat(BlockedSitesConfig.isHardcodedBlocked("https://18comic.vip:8080/album")).isTrue();
            assertThat(BlockedSitesConfig.isHardcodedBlocked("http://pornhub.com:80/video")).isTrue();
        }
    }
} 