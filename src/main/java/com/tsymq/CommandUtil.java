package com.tsymq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandUtil {

    /**
     * Executes an operating system command and returns the result.
     * @param command The command to execute.
     * @return The output of the command as a single string.
     */
    public static String executeCommand(String[] command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Redirect error stream to output stream

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            return output.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Executes an AppleScript and returns the result.
     * @param script The AppleScript code as a string.
     * @return The output of the script execution.
     */
    public static String executeAppleScript(String script) {
        String[] command = {"osascript", "-e", script};
        return executeCommand(command);
    }

}
