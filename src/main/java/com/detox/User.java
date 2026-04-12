package com.detox;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an application user and their screen time preferences.
 */
public class User {

    private long id;
    private String username;
    private String password; // stored as BCrypt hash
    private String name;
    private int dailySafeLimit;  // in minutes
    private int sleepStartHour;  // 24-hour format
    private int sleepEndHour;    // 24-hour format

    private List<ScreenTimeRecord> records = new ArrayList<>();

    public User() {}

    public User(String username, String password, String name,
                int dailySafeLimit, int sleepStartHour, int sleepEndHour) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.dailySafeLimit = dailySafeLimit;
        this.sleepStartHour = sleepStartHour;
        this.sleepEndHour = sleepEndHour;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDailySafeLimit() { return dailySafeLimit; }
    public void setDailySafeLimit(int limit) { this.dailySafeLimit = limit; }

    public int getSleepStartHour() { return sleepStartHour; }
    public void setSleepStartHour(int hour) { this.sleepStartHour = hour; }

    public int getSleepEndHour() { return sleepEndHour; }
    public void setSleepEndHour(int hour) { this.sleepEndHour = hour; }

    public List<ScreenTimeRecord> getRecords() { return records; }
    public void setRecords(List<ScreenTimeRecord> records) { this.records = records; }

    @Override
    public String toString() {
        return String.format(
            "User: %s (%s) | Safe Limit: %d min | Sleep: %02d:00-%02d:00",
            name, username, dailySafeLimit, sleepStartHour, sleepEndHour
        );
    }
}
