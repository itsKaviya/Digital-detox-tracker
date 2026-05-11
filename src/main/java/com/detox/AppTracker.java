package com.detox;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Automatically tracks the active application in the foreground.
 * Categorises time into Study, Social, and Entertainment based on window titles.
 */
public class AppTracker {

    private static AppTracker instance;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    // Seconds spent in each category today (reset on app start or date change)
    private int studySeconds = 0;
    private int socialSeconds = 0;
    private int entSeconds = 0;

    private String lastTitle = "";
    private long lastCheckTime = System.currentTimeMillis();

    private AppTracker() {}

    public static AppTracker getInstance() {
        if (instance == null) instance = new AppTracker();
        return instance;
    }

    /** Starts the tracking loop (checks every 5 seconds). */
    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 0, 5, TimeUnit.SECONDS);
    }

    private void tick() {
        char[] buffer = new char[1024];
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
        String title = Native.toString(buffer).toLowerCase();

        long now = System.currentTimeMillis();
        int elapsedSeconds = (int) ((now - lastCheckTime) / 1000);
        lastCheckTime = now;

        if (title.isEmpty()) return;

        Category cat = classify(title);
        switch (cat) {
            case STUDY -> studySeconds += elapsedSeconds;
            case SOCIAL -> socialSeconds += elapsedSeconds;
            case ENTERTAINMENT -> entSeconds += elapsedSeconds;
            case OTHER -> {} // Track total screen time if needed
        }

        lastTitle = title;
    }

    private Category classify(String title) {
        // Study / Productivity
        if (containsAny(title, "intellij", "eclipse", "code", "studio", "word", "excel", "powerpoint", "stack overflow", "github", "docs", "zoom", "teams", "pdf", "java", "python", "math", "quiz", "classroom", "notion", "overleaf")) {
            return Category.STUDY;
        }
        // Social Media
        if (containsAny(title, "facebook", "instagram", "twitter", "reddit", "whatsapp", "telegram", "discord", "linkedin", "tiktok", "messenger", "snapchat", "pinterest")) {
            return Category.SOCIAL;
        }
        // Entertainment
        if (containsAny(title, "youtube", "netflix", "prime video", "disney+", "spotify", "steam", "vlc", "twitch", "game", "player", "hulu", "hbo", "gaming", "epic games", "valorant", "minecraft", "roblox")) {
            return Category.ENTERTAINMENT;
        }
        return Category.OTHER;
    }

    private boolean containsAny(String source, String... keywords) {
        for (String k : keywords) {
            if (source.contains(k)) return true;
        }
        return false;
    }

    // Getters for minutes (rounded)
    public int getStudyMinutes() { return studySeconds / 60; }
    public int getSocialMinutes() { return socialSeconds / 60; }
    public int getEntertainmentMinutes() { return entSeconds / 60; }

    public void stop() {
        scheduler.shutdown();
    }

    public enum Category { STUDY, SOCIAL, ENTERTAINMENT, OTHER }
}
