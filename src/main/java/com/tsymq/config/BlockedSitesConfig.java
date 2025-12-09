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