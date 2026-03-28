package com.detox;

/**
 * Represents an application user and their screen time preferences.
 */
public class User {
    private String name;
    private int dailySafeLimit;   // in minutes
    private int sleepStartHour;   // 24-hour format
    private int sleepEndHour;     // 24-hour format

    public User() {} // Default constructor for JSON mapping

    public User(String name, int dailySafeLimit, int sleepStartHour, int sleepEndHour) {
        this.name = name;
        this.dailySafeLimit = dailySafeLimit;
        this.sleepStartHour = sleepStartHour;
        this.sleepEndHour = sleepEndHour;
    }

    // Getters
    public String getName()       { return name; }
    public int getDailySafeLimit() { return dailySafeLimit; }
    public int getSleepStartHour() { return sleepStartHour; }
    public int getSleepEndHour()   { return sleepEndHour; }

    // Setters
    public void setName(String name)               { this.name = name; }
    public void setDailySafeLimit(int limit)       { this.dailySafeLimit = limit; }
    public void setSleepStartHour(int hour)        { this.sleepStartHour = hour; }
    public void setSleepEndHour(int hour)          { this.sleepEndHour = hour; }

    @Override
    public String toString() {
        return String.format(
            "User: %s | Safe Limit: %d min | Sleep: %02d:00 - %02d:00",
            name, dailySafeLimit, sleepStartHour, sleepEndHour
        );
    }
}
