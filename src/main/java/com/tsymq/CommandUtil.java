package com.tsymq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * 系统命令执行工具类
 * 主要用于执行AppleScript命令以监控和控制浏览器
 */
public class CommandUtil {

    private static final int COMMAND_TIMEOUT_SECONDS = 10;

    /**
     * 执行系统命令并返回结果
     * @param command 要执行的命令数组
     * @return 命令输出结果
     */
    public static String executeCommand(String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // 设置超时
            boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.err.println("Command timed out: " + String.join(" ", command));
                return "";
            }
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            return output.toString().trim();
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + String.join(" ", command) + " - " + e.getMessage());
            return "";
        }
    }

    /**
     * 执行AppleScript并返回结果
     * @param script AppleScript代码字符串
     * @return 脚本执行输出
     */
    public static String executeAppleScript(String script) {
        return executeCommand(new String[]{"osascript", "-e", script});
    }
}
