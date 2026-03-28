package com.detox;
/**
 * Displays console alerts when the user nears or exceeds their safe screen-time limit.
 */
public class AlertSystem {

    private static final double NEAR_LIMIT_RATIO = 0.85; // 85 % of safe limit

    /**
     * Checks usage and prints appropriate alerts to the console.
     *
     * @param record   today's screen-time data
     * @param user     user profile containing the safe limit
     */
    public void checkAndAlert(ScreenTimeRecord record, User user) {
        int total     = record.getTotalTime();
        int safeLimit = user.getDailySafeLimit();
        int nearLimit = (int)(safeLimit * NEAR_LIMIT_RATIO);

        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║              ⚠  ALERT SYSTEM  ⚠          ║");
        System.out.println("╚══════════════════════════════════════════╝");

        if (total > safeLimit) {
            int over = total - safeLimit;
            System.out.println("🔴 EXCEEDED SAFE LIMIT!");
            System.out.printf("   You are %d minute(s) over your %d-minute daily limit.%n",
                              over, safeLimit);
            System.out.println("   Immediate action recommended: put the device down now.");
        } else if (total >= nearLimit) {
            int remaining = safeLimit - total;
            System.out.println("🟡 APPROACHING SAFE LIMIT!");
            System.out.printf("   You have only %d minute(s) left before reaching your %d-minute limit.%n",
                              remaining, safeLimit);
            System.out.println("   Consider wrapping up and taking a break.");
        } else {
            System.out.println("🟢 Usage is within safe limits. Keep it up!");
        }

        System.out.println("══════════════════════════════════════════");
    }
}
