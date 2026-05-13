package com.detox;

import com.detox.gui.NotificationManager;
import java.awt.TrayIcon;
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
    private int pickups = 0;
    private int notifications = 0;

    // Limits (in minutes) - defaults
    private int socialLimit = 60;
    private int entertainmentLimit = 60;
    private boolean focusMode = false;

    private String lastTitle = "";
    private long lastCheckTime = System.currentTimeMillis();
    private boolean isLocked = false;

    private AppTracker() {}

    public static AppTracker getInstance() {
        if (instance == null) instance = new AppTracker();
        return instance;
    }

    /** Initialises the tracker with today's existing record to prevent reset on restart. */
    public void initialize(ScreenTimeRecord record) {
        if (record != null) {
            this.studySeconds = record.getStudyTime() * 60;
            this.socialSeconds = record.getSocialTime() * 60;
            this.entSeconds = record.getEntertainmentTime() * 60;
            this.pickups = record.getPickups();
            this.notifications = record.getNotifications();
        }
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
            case SOCIAL -> {
                socialSeconds += elapsedSeconds;
                checkLimit("Social Media", socialSeconds, socialLimit);
            }
            case ENTERTAINMENT -> {
                entSeconds += elapsedSeconds;
                checkLimit("Entertainment", entSeconds, entertainmentLimit);
            }
            case OTHER -> {} 
        }

        // Simulate notification count based on window switches
        if (!title.equals(lastTitle) && !lastTitle.isEmpty()) {
            notifications++;
        }

        // Simple Pickup/Wake detection
        boolean currentlyLocked = isSystemLocked();
        if (isLocked && !currentlyLocked) {
            pickups++;
        }
        isLocked = currentlyLocked;

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

    private boolean isSystemLocked() {
        // Very basic heuristic for a desktop app: if HWND is null or title is empty/Login, it might be locked.
        // For real production, we'd use WTSRegisterSessionNotification, but this works for demo.
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        return hwnd == null;
    }

    private void checkLimit(String category, int currentSeconds, int limitMinutes) {
        if (currentSeconds > limitMinutes * 60) {
            // Trigger alert via system tray notification
            NotificationManager.showNotification("Limit Exceeded", "You have exceeded the " + category + " limit.", TrayIcon.MessageType.WARNING);
        }
        if (focusMode && (category.equals("Social Media") || category.equals("Entertainment"))) {
             // In strict focus mode, we could minimize the window
             // User32.INSTANCE.ShowWindow(User32.INSTANCE.GetForegroundWindow(), User32.SW_MINIMIZE);
        }
    }

    // Getters for minutes (rounded)
    public int getStudyMinutes() { return studySeconds / 60; }
    public int getSocialMinutes() { return socialSeconds / 60; }
    public int getEntertainmentMinutes() { return entSeconds / 60; }
    public int getPickups() { return pickups; }
    public int getNotifications() { return notifications; }

    public void setSocialLimit(int mins) { this.socialLimit = mins; }
    public void setEntertainmentLimit(int mins) { this.entertainmentLimit = mins; }
    public void setFocusMode(boolean enabled) { this.focusMode = enabled; }

    public void stop() {
        scheduler.shutdown();
    }

    public enum Category { STUDY, SOCIAL, ENTERTAINMENT, OTHER }
}
