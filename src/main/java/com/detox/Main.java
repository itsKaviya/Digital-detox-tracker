package com.detox;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Entry point – console menu for the Digital Detox Tracker.
 *
 * Menu options:
 *  1. Log today's screen time
 *  2. View saved history
 *  3. Update preferences
 *  4. Exit
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        printBanner();

        // --- User setup ---
        User user = setupUser();

        Tracker     tracker     = new Tracker();
        FileManager fileManager = new FileManager();

        boolean running = true;
        while (running) {
            printMenu(user.getName());
            int choice = readInt("Enter your choice: ", 1, 4);

            switch (choice) {
                case 1 -> logScreenTime(tracker, user);
                case 2 -> fileManager.printHistory(user.getName());
                case 3 -> user = updatePreferences(user);
                case 4 -> {
                    System.out.println("\n👋 Stay mindful. See you tomorrow, " + user.getName() + "!");
                    running = false;
                }
            }
        }
        scanner.close();
    }

    // ──────────────────────────────────────────────────────────────
    // Banner
    // ──────────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║     🌿  DIGITAL DETOX TRACKER — Wellness Assistant  🌿   ║");
        System.out.println("║         Behavior-Aware · Personalized · Insightful        ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    // ──────────────────────────────────────────────────────────────
    // User setup
    // ──────────────────────────────────────────────────────────────

    private static User setupUser() {
        System.out.println("═══  WELCOME — Let's set up your profile  ═══");
        System.out.print("  Enter your name: ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) name = "User";

        int safeLimit   = readInt("  Daily safe screen-time limit (minutes, e.g. 120): ", 10, 1440);
        int sleepStart  = readInt("  Sleep start hour in 24h format (e.g. 22 for 10 PM): ", 18, 23);
        int sleepEnd    = readInt("  Wake-up hour in 24h format (e.g. 6 for 6 AM): ", 3, 12);

        User user = new User(name, safeLimit, sleepStart, sleepEnd);
        System.out.println("\n✅ Profile saved: " + user);
        return user;
    }

    private static User updatePreferences(User user) {
        System.out.println("\n═══  UPDATE PREFERENCES  ═══");
        System.out.printf("  Current: %s%n", user);
        System.out.print("  New name (Enter to keep \"" + user.getName() + "\"): ");
        String name = scanner.nextLine().trim();
        if (!name.isBlank()) user.setName(name);

        int safeLimit  = readInt("  New safe limit (min, Enter 0 to keep " + user.getDailySafeLimit() + "): ", 0, 1440);
        if (safeLimit > 0) user.setDailySafeLimit(safeLimit);

        int sleepStart = readInt("  New sleep start hour (0 to keep " + user.getSleepStartHour() + "): ", 0, 23);
        if (sleepStart > 0) user.setSleepStartHour(sleepStart);

        System.out.println("✅ Preferences updated: " + user);
        return user;
    }

    // ──────────────────────────────────────────────────────────────
    // Screen time logging
    // ──────────────────────────────────────────────────────────────

    private static void logScreenTime(Tracker tracker, User user) {
        System.out.println("\n═══  LOG TODAY'S SCREEN TIME  ═══");
        System.out.println("  Enter time in minutes for each category:");

        int study         = readInt("  📚 Study / Work          : ", 0, 1440);
        int social        = readInt("  📱 Social Media          : ", 0, 1440);
        int entertainment = readInt("  🎮 Entertainment / Video : ", 0, 1440);
        int peakHour      = readInt("  🕐 Peak usage hour (0-23): ", 0, 23);

        tracker.processDay(user, study, social, entertainment, peakHour);
    }

    // ──────────────────────────────────────────────────────────────
    // Menu
    // ──────────────────────────────────────────────────────────────

    private static void printMenu(String name) {
        System.out.println();
        System.out.printf("═══  MAIN MENU  (%s)  ═══%n", name);
        System.out.println("  [1] Log today's screen time");
        System.out.println("  [2] View saved history");
        System.out.println("  [3] Update preferences");
        System.out.println("  [4] Exit");
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────

    /**
     * Reads an integer in [min, max] with retry on invalid input.
     */
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.printf("  ⚠ Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  ⚠ Invalid input. Please enter a whole number.");
            }
        }
    }
}
