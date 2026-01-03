package com.tsymq.browser.cdp;

/**
 * CDP 标签页信息
 */
public class CDPTab {
    public final String id;
    public final String url;
    public final String title;
    public final int port;
    public final String webSocketUrl;

    public CDPTab(String id, String url, String title, int port, String webSocketUrl) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.port = port;
        this.webSocketUrl = webSocketUrl;
    }

    @Override
    public String toString() {
        return "CDPTab{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", port=" + port +
                '}';
    }
}
