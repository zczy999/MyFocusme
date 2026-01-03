package com.tsymq.browser.cdp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CDPClient 测试类
 */
class CDPClientTest {

    @Test
    @DisplayName("测试获取运行中的 SunBrowser 环境 ID")
    void testGetRunningEnvIds() {
        Set<String> envIds = CDPClient.getRunningEnvIds();

        System.out.println("=== 运行中的环境 ===");
        System.out.println("环境数量: " + envIds.size());
        for (String envId : envIds) {
            System.out.println("  - " + envId);
            // 验证 envId 格式正确（不包含空格和其他参数）
            assertThat(envId).doesNotContain(" ");
            assertThat(envId).doesNotContain("--");
            assertThat(envId).matches("[a-zA-Z0-9_]+");
        }
    }

    @Test
    @DisplayName("测试获取调试端口")
    void testGetDebugPort() {
        Set<String> envIds = CDPClient.getRunningEnvIds();

        System.out.println("=== 调试端口 ===");
        for (String envId : envIds) {
            int port = CDPClient.getDebugPort(envId);
            System.out.println("  " + envId + " -> 端口: " + port);
            if (port > 0) {
                assertThat(port).isGreaterThan(1000);
                assertThat(port).isLessThan(65536);
            }
        }
    }

    @Test
    @DisplayName("测试获取所有标签页")
    void testGetAllTabs() {
        List<CDPTab> tabs = CDPClient.getAllTabs();

        System.out.println("=== 所有标签页 ===");
        System.out.println("总数: " + tabs.size());
        System.out.println();

        for (CDPTab tab : tabs) {
            System.out.println("端口: " + tab.port);
            System.out.println("  标题: " + truncate(tab.title, 50));
            System.out.println("  URL: " + truncate(tab.url, 60));
            System.out.println();

            // 验证标签页数据
            assertThat(tab.id).isNotEmpty();
            assertThat(tab.port).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("测试获取指定端口的标签页")
    void testGetTabs() {
        Set<String> envIds = CDPClient.getRunningEnvIds();

        for (String envId : envIds) {
            int port = CDPClient.getDebugPort(envId);
            if (port > 0) {
                List<CDPTab> tabs = CDPClient.getTabs(port);
                System.out.println("环境 " + envId + " (端口 " + port + ") 有 " + tabs.size() + " 个标签页");

                for (CDPTab tab : tabs) {
                    assertThat(tab.port).isEqualTo(port);
                }
            }
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
}
