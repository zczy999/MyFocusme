package com.tsymq.browser.cdp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chrome DevTools Protocol 客户端
 * 用于获取和控制 SunBrowser (AdsPower) 多实例的标签页
 */
public class CDPClient {

    private static final String ADSPOWER_CACHE_BASE = System.getProperty("user.home")
            + "/Library/Application Support/adspower_global/cwd_global/source/cache/";
    private static final Pattern ENV_ID_PATTERN = Pattern.compile("source/cache/([^\\s/]+)");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取所有运行中的 SunBrowser 实例的环境 ID
     */
    public static Set<String> getRunningEnvIds() {
        Set<String> envIds = new HashSet<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("ps", "aux");
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("SunBrowser.app/Contents/MacOS/SunBrowser") && line.contains("--user-data-dir=")) {
                        Matcher matcher = ENV_ID_PATTERN.matcher(line);
                        if (matcher.find()) {
                            envIds.add(matcher.group(1));
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Error getting running SunBrowser instances: " + e.getMessage());
        }
        return envIds;
    }

    /**
     * 获取指定环境的 CDP 调试端口
     */
    public static int getDebugPort(String envId) {
        Path portFile = Paths.get(ADSPOWER_CACHE_BASE, envId, "DevToolsActivePort");
        if (Files.exists(portFile)) {
            try {
                List<String> lines = Files.readAllLines(portFile);
                if (!lines.isEmpty()) {
                    return Integer.parseInt(lines.get(0).trim());
                }
            } catch (Exception e) {
                System.err.println("Error reading DevToolsActivePort for " + envId + ": " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * 获取指定端口的所有标签页
     */
    public static List<CDPTab> getTabs(int port) {
        List<CDPTab> tabs = new ArrayList<>();
        try {
            URL url = new URL("http://127.0.0.1:" + port + "/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (InputStream is = conn.getInputStream()) {
                    JsonNode root = objectMapper.readTree(is);
                    for (JsonNode node : root) {
                        String type = node.has("type") ? node.get("type").asText() : "";
                        // 只处理 page 类型，跳过 iframe、service_worker 等
                        if ("page".equals(type)) {
                            String id = node.has("id") ? node.get("id").asText() : "";
                            String tabUrl = node.has("url") ? node.get("url").asText() : "";
                            String title = node.has("title") ? node.get("title").asText() : "";
                            String wsUrl = node.has("webSocketDebuggerUrl") ? node.get("webSocketDebuggerUrl").asText() : "";
                            tabs.add(new CDPTab(id, tabUrl, title, port, wsUrl));
                        }
                    }
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            // 连接失败说明实例可能已关闭，忽略
        }
        return tabs;
    }

    /**
     * 关闭指定标签页
     */
    public static boolean closeTab(int port, String tabId) {
        try {
            URL url = new URL("http://127.0.0.1:" + port + "/json/close/" + tabId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            System.err.println("Error closing tab: " + e.getMessage());
            return false;
        }
    }

    /**
     * 在指定端口打开新标签页
     */
    public static boolean openNewTab(int port) {
        try {
            URL url = new URL("http://127.0.0.1:" + port + "/json/new?about:blank");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            System.err.println("Error opening new tab: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取所有运行中 SunBrowser 实例的所有标签页
     */
    public static List<CDPTab> getAllTabs() {
        List<CDPTab> allTabs = new ArrayList<>();
        Set<String> envIds = getRunningEnvIds();

        for (String envId : envIds) {
            int port = getDebugPort(envId);
            if (port > 0) {
                List<CDPTab> tabs = getTabs(port);
                allTabs.addAll(tabs);
            }
        }
        return allTabs;
    }
}
