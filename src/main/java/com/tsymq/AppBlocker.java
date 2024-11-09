package com.tsymq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class AppBlocker {

    private final String BLOCKED_WEBSITES_FILENAME = "/Users/tsymq/.config/myfocusme/blocked_websites.txt";

    private final String WHITE_WEBSITES_FILENAME = "/Users/tsymq/.config/myfocusme/white_websites.txt";

    private final Set<String> blockedWebsites = new HashSet<>();

    private final Set<String> whiteWebsites = new HashSet<>();

    private final Set<String> closedWebsites = new HashSet<>();


    private volatile boolean monitoring = false;

    public AppBlocker() {
        closedWebsites.add("javbus");
        closedWebsites.add("porn");
        closedWebsites.add("javlibrary");
        closedWebsites.add("jable");
        closedWebsites.add("missav");
        closedWebsites.add("hanime1.me");
        closedWebsites.add("2dfan");
        closedWebsites.add("njav");
    }


    public interface OnActiveEdgeUrlChangedListener {
        void onActiveEdgeUrlChanged(String url, String browser);
    }

    private OnActiveEdgeUrlChangedListener onActiveEdgeUrlChangedListener;

    public void setOnActiveEdgeUrlChangedListener(OnActiveEdgeUrlChangedListener onActiveEdgeUrlChangedListener) {
        this.onActiveEdgeUrlChangedListener = onActiveEdgeUrlChangedListener;
    }

    public boolean block(String itemToBlock) {
        if (itemToBlock.contains("http://") || itemToBlock.contains("https://")) {
            return blockWebsite(itemToBlock);
        } else {
            // 如果需要屏蔽应用程序，这里可以实现相应的逻辑。
            return false;
        }
    }

    private boolean blockWebsite(String website) {
        blockedWebsites.add(website);
        saveBlockedWebsites();
        return true;
    }

    public void openNewEdgeTab() {
        String[] command = {"osascript", "-e", "tell application \"Microsoft Edge\" to make new tab at end of tabs of front window"};
        runScript(command);
    }


    public void openNewSafariTab() {
        String newTabScript = "tell application \"Safari\"\n" +
                "   tell window 1\n" +
                "       set newTab to make new tab\n" +
                "       set current tab to newTab\n" +
                "   end tell\n" +
                "end tell";
        String[] args = {"osascript", "-e", newTabScript};
        runScript(args);
    }

    private static void runScript(String[] command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getActiveAppName() {
        String getActiveAppNameScript ="tell application \"System Events\" to name of first application process whose frontmost is true";
        String[] args = {"osascript", "-e", getActiveAppNameScript};
        String result = runScript1(args);

        if (result == null || result.isBlank()) {
            return null;
        }
        return result.trim();
    }

    private String getActiveSafariURL() {
        String safariURLScript = "tell application \"System Events\"\n" +
                "   set activeApp to name of first application process whose frontmost is true\n" +
                "end tell\n" +
                "if activeApp is \"Safari\" then\n" +
                "   tell application \"Safari\"\n" +
                "       if exists (document 1) then\n" +
                "           return URL of document 1\n" +
                "       end if\n" +
                "   end tell\n" +
                "else\n" +
                "   return \"\"\n" +
                "end if";
        String[] args = {"osascript", "-e", safariURLScript};
        String result = runScript1(args);

        if (result == null || result.isBlank()) {
            return null;
        }
        return result.trim();
    }

    private String runScript1(String[] args) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getActiveEdgeURL() {
        String[] command = {"osascript", "-e",
                "tell application \"System Events\" to set activeApp to name of first application process whose frontmost is true\n" +
                        "if activeApp is \"Microsoft Edge\" then\n" +
                        "tell application \"Microsoft Edge\" to set currentURL to URL of active tab of front window\n" +
                        "return currentURL\n" +
                        "else\n" +
                        "return \"\"\n" +
                        "end if"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void closeBrowserWindow(String browser) {
        String closeWindowScript;
        if (browser.equals("edge")) {
            closeWindowScript = "tell application \"Microsoft Edge\" to close front window";
        } else {
            closeWindowScript = "tell application \"Safari\" to close front window";
        }
        String[] args = {"osascript", "-e", closeWindowScript};
        runScript(args);
    }

    public void closeActiveTab(String browser) {
        String closeTabScript;

        if (browser.equals("edge")) {
            closeTabScript = "tell application \"Microsoft Edge\" to close active tab of front window";
        } else {
            closeTabScript = "tell application \"Safari\" to close current tab of front window";
        }

        String[] args = {"osascript", "-e", closeTabScript};
        runScript(args);
    }

    private void closeChrome() {
        String closeChromeScript = "tell application \"Google Chrome\" to quit";
        String[] args = {"osascript", "-e", closeChromeScript};
        runScript(args);
    }

    public boolean isBlocked(String url) {
        for (String blockedWebsite : blockedWebsites) {
            if (url.contains(blockedWebsite)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWhiteWeb(String url) {
        for (String whiteWebsite : whiteWebsites) {
            if (url.contains(whiteWebsite)) {
                return true;
            }
        }
        return false;
    }

    public boolean isClosed(String url) {
        for (String closedWebsite : closedWebsites) {
            if (url.contains(closedWebsite)) {
                return true;
            }
        }
        return false;
    }


    public boolean closeWebsite(String website) {
        closedWebsites.add(website);
        return true;
    }


    public void saveBlockedWebsites() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(BLOCKED_WEBSITES_FILENAME))) {
            for (String blockedWebsite : blockedWebsites) {
                writer.write(blockedWebsite);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBlockedWebsites() {
        Path blockedWebsitesPath = Paths.get(BLOCKED_WEBSITES_FILENAME);
        if (Files.exists(blockedWebsitesPath)) {
            try (BufferedReader reader = Files.newBufferedReader(blockedWebsitesPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    blockedWebsites.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadwhiteWebsites() {
        Path whiteWebsitesPath = Paths.get(WHITE_WEBSITES_FILENAME);
        if (Files.exists(whiteWebsitesPath)) {
            try (BufferedReader reader = Files.newBufferedReader(whiteWebsitesPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    whiteWebsites.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void monitorActiveEdgeUrl() {
        monitoring = true;
        new Thread(() -> {
            while (monitoring) {
                String activeAppName = getActiveAppName();
                if (activeAppName!=null && activeAppName.equals("Google Chrome")){
                    closeChrome();
                    continue;
                }

                String browser = "edge";
                String activeUrl = getActiveEdgeURL();
                if (activeUrl.isEmpty()) {
                    browser = "safari";
                    activeUrl = getActiveSafariURL();
                }
                if (activeUrl != null && !activeUrl.isEmpty() && onActiveEdgeUrlChangedListener != null) {
                    onActiveEdgeUrlChangedListener.onActiveEdgeUrlChanged(activeUrl, browser);
                }

                try {
                    Thread.sleep(2000); // 每1秒检查一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void monitorActiveEdgeUrlToClose() {
        new Thread(() -> {
            while (true) {
                String browser = "edge";
                String activeUrl = getActiveEdgeURL();
                if (activeUrl.isEmpty()) {
                    browser = "safari";
                    activeUrl = getActiveSafariURL();
                }
                if (activeUrl != null && !activeUrl.isEmpty() && isClosed(activeUrl)) {
                    closeActiveTab(browser);
                }


                try {
                    Thread.sleep(1000); // 每3秒检查一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void stopMonitoring() {
        monitoring = false;
    }


}
