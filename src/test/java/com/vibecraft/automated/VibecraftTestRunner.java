package com.vibecraft.automated;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

public class VibecraftTestRunner {

    private static final String TEST_RESULT_FILE = "test-result.txt";
    private static final String MINECRAFT_LOG_FILE = "run/logs/latest.log"; // dev client log path
    private static final String MC_OPTIONS_FILE = "run/options.txt"; // dev client options
    private static final int MINECRAFT_LAUNCH_TIMEOUT = 10000; // 10 seconds
    private static final int WORLD_LOAD_TIMEOUT = 2000; // 2 seconds
    private static final int TEST_COMPLETION_TIMEOUT_MS = 30000; // 30 seconds max wait for test completion
    private static final int LOG_POLL_INTERVAL_MS = 250; // poll interval for console log

    private static final TestStatus testStatus = new TestStatus();

    public static void main(String[] args) throws Exception {
        System.out.println("🧪 Starting automated Vibecraft mod test...");

        testStatus.reset();

        // Clean up any previous test results
        cleanupPreviousResults();

        // Force windowed mode to avoid macOS fullscreen/minimize behavior
        ensureWindowedOption();

        // Launch Minecraft with Fabric mod
        Process minecraftProcess = launchMinecraft();
        startProcessExitWatcher(minecraftProcess);
        startLogWatcher();

        Robot robot = null;
        try {
            // Wait for Minecraft to launch
            System.out.println("⏳ Waiting for Minecraft to launch...");
            Thread.sleep(MINECRAFT_LAUNCH_TIMEOUT); // Wait for initial launch

            // Create Robot for UI automation
            robot = new Robot();
            robot.setAutoDelay(10);

            // Navigate to singleplayer and join world
            navigateToWorld(robot);

            // Wait for world to load
            System.out.println("⏳ Waiting for world to load...");
            Thread.sleep(WORLD_LOAD_TIMEOUT);

            // Run the automated tests
            runTests(robot);

            // Ask client to quit cleanly
            try {
                sendClientQuit(robot);
                Thread.sleep(2000);
            } catch (Throwable ignored) {
                // If the command isn't available or fails, allow natural shutdown to proceed
            }

            // Check test results
            checkTestResults();

        } catch (Exception e) {
            System.err.println("❌ Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            writeTestResult("FAIL");
            System.exit(1);
        } finally {
            // Clean up - close Minecraft
            System.out.println("🧹 Cleaning up...");
            closeMinecraft(minecraftProcess, robot);
        }
    }

    private static void startLogWatcher() {
        new Thread(() -> {
            try {
                Path logPath = Path.of(MINECRAFT_LOG_FILE);
                long lastKnownPosition = 0;
                while (true) {
                    if (Files.exists(logPath)) {
                        try (RandomAccessFile file = new RandomAccessFile(logPath.toFile(), "r")) {
                            if (file.length() > lastKnownPosition) {
                                file.seek(lastKnownPosition);
                                String line;
                                while ((line = file.readLine()) != null) {
                                    testStatus.update(line);
                                }
                                lastKnownPosition = file.getFilePointer();
                            }
                        }
                    }
                    Thread.sleep(LOG_POLL_INTERVAL_MS);
                }
            } catch (IOException | InterruptedException e) {
                // Ignore
            }
        }).start();
    }

    private static void startProcessExitWatcher(Process process) {
        new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println(
                            "❌ Minecraft process exited unexpectedly with code " + exitCode + ". Assuming crash.");
                    writeTestResult("FAIL");
                    System.exit(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private static void ensureWindowedOption() {
        try {
            Path optionsPath = Path.of(MC_OPTIONS_FILE);
            Files.createDirectories(optionsPath.getParent());
            String content = Files.exists(optionsPath) ? Files.readString(optionsPath, StandardCharsets.UTF_8) : "";
            String[] lines = content.split("\n");
            boolean found = false;
            StringBuilder sb = new StringBuilder();
            for (String raw : lines) {
                String line = raw.strip();
                if (line.isEmpty())
                    continue;
                if (line.startsWith("fullscreen:")) {
                    sb.append("fullscreen:false").append('\n');
                    found = true;
                } else {
                    sb.append(raw).append('\n');
                }
            }
            if (!found) {
                sb.append("fullscreen:false\n");
            }
            Files.writeString(optionsPath, sb.toString(), StandardCharsets.UTF_8);
            System.out.println("🪟 Ensured windowed mode (fullscreen:false) in options.txt");
        } catch (IOException e) {
            System.err.println("Warning: could not set windowed option: " + e.getMessage());
        }
    }

    private static Process launchMinecraft() throws IOException {
        System.out.println("🚀 Launching Minecraft...");

        ProcessBuilder builder = new ProcessBuilder("./gradlew", "runClient");
        // Hint the client JVM to behave like a background UI element on macOS to avoid
        // stealing focus
        builder.environment().putIfAbsent("ORG_GRADLE_PROJECT_JVM_ARGS",
                "-Dapple.awt.UIElement=true -Djava.awt.headless=false");
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        builder.directory(new File("."));

        Process process = builder.start();

        // Wait a bit for the process to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return process;
    }

    private static void navigateToWorld(Robot robot) throws InterruptedException {
        System.out.println("🎮 Navigating to singleplayer world...");

        // Give UI a moment
        Thread.sleep(500);

        // Sometimes focus lands on Accessibility prompt; Tab once to ensure menu
        // selection
        pressKey(robot, KeyEvent.VK_TAB);
        Thread.sleep(100);

        // Press Enter to select Singleplayer (usually the first option)
        pressKey(robot, KeyEvent.VK_ENTER);
        Thread.sleep(1200);

        // Press Tab to select the first world (if any exist)
        pressKey(robot, KeyEvent.VK_TAB);
        Thread.sleep(600);

        // Press Enter to enter the world
        pressKey(robot, KeyEvent.VK_ENTER);
        Thread.sleep(1500);

        System.out.println("✅ World navigation completed");
    }

    private static void runTests(Robot robot) throws InterruptedException {
        System.out.println("🧪 Running automated tests...");

        // Wait for world to fully load
        Thread.sleep(1000);

        // Open chat (T key)
        pressKey(robot, KeyEvent.VK_T);
        Thread.sleep(300);

        // Type the test command
        typeString(robot, "/runalltests");
        Thread.sleep(500);

        // Press Enter to execute
        pressKey(robot, KeyEvent.VK_ENTER);
        Thread.sleep(1000);

        // Wait for test completion by polling the console log (up to 30s)
        checkTestCompletion();
    }

    private static void checkTestCompletion() throws InterruptedException {
        System.out.println("🔍 Checking console for test completion (up to 30s)...");

        long deadline = System.currentTimeMillis() + TEST_COMPLETION_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            TestStatus.Status status = testStatus.getStatus();
            System.out.println("Polling for test status... Current status: " + status);
            if (status == TestStatus.Status.PASS) {
                System.out.println("✅ Tests completed successfully (from console)");
                writeTestResult("PASS");
                return;
            } else if (status == TestStatus.Status.FAIL) {
                System.out.println("❌ Tests failed (from console)");
                writeTestResult("FAIL");
                return;
            }

            Thread.sleep(LOG_POLL_INTERVAL_MS);
        }

        System.out.println("⏰ Timed out waiting for test results (30s)");
        writeTestResult("FAIL");
    }

    private enum TestStatusEnum {
        PASS, FAIL, UNKNOWN
    }

    private static class TestStatus {
        private enum Status {
            PASS, FAIL, UNKNOWN
        }

        private Status status = Status.UNKNOWN;

        public synchronized void update(String logLine) {
            if (logLine.contains("❌ Some tests failed") ||
                    logLine.contains("Test execution failed") ||
                    logLine.contains("Vibecraft multiplier mismatch") ||
                    logLine.contains("TNT multiplier mismatch")) {
                status = Status.FAIL;
            } else if (logLine.contains("✅ All tests passed") || logLine.contains("All tests passed")) {
                status = Status.PASS;
            }
        }

        public synchronized Status getStatus() {
            return status;
        }

        public synchronized void reset() {
            status = Status.UNKNOWN;
        }
    }

    private static void checkLogFileForResults() {
        try {
            Path logPath = Path.of(MINECRAFT_LOG_FILE);
            if (Files.exists(logPath)) {
                String logContent = Files.readString(logPath);

                // Look for test success indicators
                if (logContent.contains("✅ All tests passed") ||
                        logContent.contains("Test completed successfully") ||
                        logContent.contains("Vibecraft explosion radius modified")) {

                    System.out.println("✅ Tests appear to have completed successfully");
                    writeTestResult("PASS");
                } else if (logContent.contains("❌ Test failed") ||
                        logContent.contains("Test failed")) {

                    System.out.println("❌ Tests appear to have failed");
                    writeTestResult("FAIL");
                } else {
                    System.out.println("⚠️ Test results unclear, checking for mod activity...");

                    // Check if the mod is working by looking for explosion modifications
                    if (logContent.contains("Vibecraft explosion radius modified")) {
                        System.out.println("✅ Mod is working (explosions modified)");
                        writeTestResult("PASS");
                    } else {
                        System.out.println("❌ No evidence of mod activity found");
                        writeTestResult("FAIL");
                    }
                }
            } else {
                System.out.println("⚠️ Log file not found, assuming test failed");
                writeTestResult("FAIL");
            }
        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
            writeTestResult("FAIL");
        }
    }

    private static void checkTestResults() throws IOException {
        System.out.println("📊 Checking final test results...");

        Path resultPath = Path.of(TEST_RESULT_FILE);
        if (Files.exists(resultPath)) {
            String result = Files.readString(resultPath).trim();
            if ("PASS".equals(result)) {
                System.out.println("🎉 ✅ All tests passed!");
                System.exit(0);
            } else {
                System.err.println("💥 ❌ Tests failed.");
                System.exit(1);
            }
        } else {
            System.err.println("⚠️ No test result file found, assuming failure");
            System.exit(1);
        }
    }

    private static void writeTestResult(String result) {
        try {
            Files.writeString(Path.of(TEST_RESULT_FILE), result);
            System.out.println("📝 Test result written: " + result);
        } catch (IOException e) {
            System.err.println("Error writing test result: " + e.getMessage());
        }
    }

    private static void cleanupPreviousResults() {
        try {
            Files.deleteIfExists(Path.of(TEST_RESULT_FILE));
            System.out.println("🧹 Cleaned up previous test results");
        } catch (IOException e) {
            System.err.println("Warning: Could not clean up previous results: " + e.getMessage());
        }
    }

    private static void closeMinecraft(Process process, Robot robot) {
        // Kill the Gradle runClient process directly (best-effort)
        killGradleRunClient();

        // Terminate the gradle/java process tree for runClient
        if (process != null && process.isAlive()) {
            System.out.println("🔄 Closing Minecraft (process)...");
            process.destroy();
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }

        // Stop Gradle daemon to avoid lingering background processes
        try {
            new ProcessBuilder("./gradlew", "--stop")
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
        } catch (IOException ignored) {
        }
    }

    private static void killGradleRunClient() {
        try {
            Process p = new ProcessBuilder("/bin/ps", "-A", "-o", "pid=,command=")
                    .redirectErrorStream(true)
                    .start();
            p.waitFor(2, TimeUnit.SECONDS);
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            List<String> pidsToKill = new ArrayList<>();
            for (String line : out.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty())
                    continue;
                String[] parts = trimmed.split(" ", 2);
                if (parts.length < 2)
                    continue;
                String pid = parts[0];
                String cmd = parts[1];
                boolean isGradleRunClient = (cmd.contains("gradle") || cmd.contains("gradlew"));
                if (isGradleRunClient) {
                    pidsToKill.add(pid);
                }
            }
            for (String pid : pidsToKill) {
                try {
                    new ProcessBuilder("kill", "-9", pid).start();
                } catch (IOException ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
    }

    private static void pressKey(Robot robot, int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    private static void typeString(Robot robot, String text) {
        for (char c : text.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                // Handle special characters
                switch (c) {
                    case ' ':
                        keyCode = KeyEvent.VK_SPACE;
                        break;
                    case '/':
                        keyCode = KeyEvent.VK_SLASH;
                        break;
                    case '@':
                        keyCode = KeyEvent.VK_AT;
                        break;
                    default:
                        continue; // Skip unsupported characters
                }
            }

            if (Character.isUpperCase(c)) {
                robot.keyPress(KeyEvent.VK_SHIFT);
            }

            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);

            if (Character.isUpperCase(c)) {
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
        }
    }

    private static void sendClientQuit(Robot robot) throws InterruptedException {
        pressKey(robot, KeyEvent.VK_T);
        Thread.sleep(200);
        typeString(robot, "/clientquit");
        Thread.sleep(200);
        pressKey(robot, KeyEvent.VK_ENTER);
    }
}