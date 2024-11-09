package com.tsymq;

import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;


public class AppBlocker {

    private final String BLOCKED_WEBSITES_FILENAME = "/Users/tsymq/.config/myfocusme/blocked_websites.txt";

    private final String WHITE_WEBSITES_FILENAME = "/Users/tsymq/.config/myfocusme/white_websites.txt";

    private final Set<String> blockedWebsites = new HashSet<>();

    private final Set<String> whiteWebsites = new HashSet<>();

    private final Set<String> closedWebsites = new HashSet<>();

    private TextArea outputArea;


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

    public void monitorActiveEdgeUrl(TextArea outputArea) {
        this.outputArea = outputArea;
        new Thread(() -> {
            while (true) {
                String activeAppName = getActiveAppName();
                if (activeAppName.equals("Google Chrome") || activeAppName.equals("Safari")) {
                    closeApp(activeAppName);
                    outputArea.appendText("close " + activeAppName + "\n");
                    continue;
                }

                if (activeAppName.equals("Microsoft Edge")) {
                    String activeEdgeURL = getActiveEdgeURL();
                    String activeEdgeTitle = getActiveEdgeTitle();

                    if (isWhiteWeb(activeEdgeTitle)) {
                        stopTimes(1500);
                        continue;
                    }

                    if (isBlocked(activeEdgeURL)) {
                        openNewEdgeTab();
                        continue;
                    }

                    if (isClosed(activeEdgeURL)) {
                        closeActiveEdgeTab();
                        outputArea.appendText("close web" + activeEdgeURL + "\n");
                        continue;
                    }

                }

                stopTimes(1500);
            }
        }).start();
    }


    private void stopTimes(int millis) {
        try {
            Thread.sleep(millis); // 每2秒检查一次
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        String command = "tell application \"Microsoft Edge\" to make new tab at end of tabs of front window";
        CommandUtil.executeAppleScript(command);
    }


    private String getActiveAppName() {
        String getActiveAppNameScript = "tell application \"System Events\" to name of first application process whose frontmost is true";
        return CommandUtil.executeAppleScript(getActiveAppNameScript);
    }


    private String getActiveEdgeURL() {
        String command = "tell application \"Microsoft Edge\" to get URL of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }

    private String getActiveEdgeTitle() {
        String command = "tell application \"Microsoft Edge\" to set currentTitle to title of active tab of front window";
        return CommandUtil.executeAppleScript(command);
    }


    public void closeActiveEdgeTab() {
        String closeTabScript;
        closeTabScript = "tell application \"Microsoft Edge\" to close active tab of front window";
        CommandUtil.executeAppleScript(closeTabScript);
    }

    private void closeApp(String activeAppName) {
        String closeAppScript = "tell application \"%s\" to quit";
        closeAppScript = String.format(closeAppScript, activeAppName);
        CommandUtil.executeAppleScript(closeAppScript);
    }

    public boolean isBlocked(String url) {
        for (String blockedWebsite : blockedWebsites) {
            if (url.contains(blockedWebsite)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWhiteWeb(String title) {
        for (String whiteWebsite : whiteWebsites) {
            if (title.contains(whiteWebsite)) {
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


}
