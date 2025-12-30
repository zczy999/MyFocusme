package com.tsymq.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 屏蔽网站配置类
 * 管理硬编码的色情网站屏蔽列表
 */
public class BlockedSitesConfig {

    /**
     * 获取硬编码的色情网站屏蔽列表
     * 这些网站在所有模式下都会被屏蔽
     * @return 屏蔽网站集合
     */
    public static Set<String> getHardcodedBlockedSites() {
        return new HashSet<>(Arrays.asList(
            //新加
            "nsfw",
            "av-wiki",
            "dmm.co.jp",
            "tktube.com",
            "javdb",
            "laowang",
            "fansky",
            "pixiv",
            "south-plus",
            "laoli.one",
            "ctee.kr",
            "puremedia",
            "yeha_",


            // 日本成人网站
            "javbus",
            "javlibrary",
            "jable",
            "missav",
            "hanime1",
            "2dfan",
            "njav",
            "avmoo",
            "javmost",
            "javfree",
            "javhd",
            "18comic",

            // 国际知名色情网站
            "pornhub",
            "xvideos",
            "xnxx",
            "redtube",
            "youporn",
            "tube8",
            "spankbang",
            "xhamster",
            "beeg",
            "tnaflix",
            "drtuber",
            "slutload",

            // 中文色情网站
            "91porn",
            "caoliu",
            "1024",

            // 直播色情网站
            "chaturbate",
            "myfreecams",
            "camsoda",
            "stripchat",
            "bongacams",
            "livejasmin",
            "flirt4free",

            // 订阅制成人内容平台
            "patreon",
            "onlyfans",
            "fansly",
            "fanvue",
            "loyalfans",
            "justfor.fans",
            "fancentro",
            "admireme",
            "frisk.chat",
            "unlockd",

            // 订阅内容泄露站
            "coomer",
            "kemono",
            "simpcity",
            "thothub",
            "fapello",
            "leakedbb",
            "socialmediagirls",
            "thotsbay",
            "nekohouse",
            "simpcity",

            // 特定关键词
            "porn",
            "xxx",
            "erotic",
            "hentai",
            "pornsite",
            "sexvideo",
            "adultsite"
        ));
    }

    /**
     * 检查URL是否包含硬编码的屏蔽关键词
     * @param url 要检查的URL
     * @return 是否应该被屏蔽
     */
    public static boolean isHardcodedBlocked(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        return getHardcodedBlockedSites().stream()
                .anyMatch(lowerUrl::contains);
    }

    /**
     * 获取屏蔽网站数量
     * @return 屏蔽网站总数
     */
    public static int getBlockedSitesCount() {
        return getHardcodedBlockedSites().size();
    }

    /**
     * 获取软屏蔽列表（打开新标签页方式，不受白名单影响）
     * @return 软屏蔽网站集合
     */
    public static Set<String> getSoftBlockedSites() {
        return new HashSet<>(Arrays.asList(
            // 在这里添加软屏蔽网站
            "bilibili.com",
            "youtube.com"
        ));
    }

    /**
     * 检查URL是否在软屏蔽列表中
     * @param url 要检查的URL
     * @return 是否应该被软屏蔽
     */
    public static boolean isSoftBlocked(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        return getSoftBlockedSites().stream()
                .anyMatch(lowerUrl::contains);
    }

    /**
     * 精确匹配屏蔽列表（仅主页）
     * 只屏蔽这些网站的首页，不屏蔽子路径
     * 软屏蔽：仅学习模式生效，打开新标签页
     */
    private static final Set<String> EXACT_MATCH_BLOCKED_SITES = new HashSet<>(Arrays.asList(
        "linux.do"
    ));

    /**
     * 获取精确匹配屏蔽网站列表
     * @return 精确匹配屏蔽域名集合
     */
    public static Set<String> getExactMatchBlockedSites() {
        return new HashSet<>(EXACT_MATCH_BLOCKED_SITES);
    }

    /**
     * 判断路径是否为主页
     * 主页定义：路径为空、"/"、"/index.html"、"/index.htm"、"/index.php" 等
     */
    private static boolean isHomePage(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return true;
        }
        String cleanPath = path.split("[?#]")[0].toLowerCase();
        return Set.of("/index.html", "/index.htm", "/index.php").contains(cleanPath);
    }

    /**
     * 检查 URL 是否被精确匹配屏蔽（仅主页）
     * @param url 要检查的 URL
     * @return 是否应该被精确匹配屏蔽
     */
    public static boolean isExactMatchBlocked(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            String path = uri.getPath();

            if (host == null) {
                return false;
            }

            String normalizedHost = host.toLowerCase();
            if (normalizedHost.startsWith("www.")) {
                normalizedHost = normalizedHost.substring(4);
            }

            boolean domainMatches = EXACT_MATCH_BLOCKED_SITES.contains(normalizedHost);
            return domainMatches && isHomePage(path);
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }

    /**
     * 添加新的屏蔽网站（运行时添加，不持久化）
     * @param sites 要添加的网站集合
     * @return 更新后的屏蔽网站集合
     */
    public static Set<String> addRuntimeBlockedSites(Set<String> additionalSites) {
        Set<String> allSites = getHardcodedBlockedSites();
        if (additionalSites != null) {
            allSites.addAll(additionalSites);
        }
        return allSites;
    }
} 