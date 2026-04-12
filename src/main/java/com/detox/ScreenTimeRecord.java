package com.detox;

import java.time.LocalDate;

/**
 * Holds one day's screen-time data for a user.
 */
public class ScreenTimeRecord {

    private long id;
    private LocalDate date;
    private long userId;  // FK reference (no JPA lazy loading needed)

    private int studyTime;          // minutes
    private int socialTime;         // minutes
    private int entertainmentTime;  // minutes
    private int totalTime;          // computed
    private int peakUsageHour;      // 0-23

    public ScreenTimeRecord() {}

    public ScreenTimeRecord(LocalDate date, long userId, int studyTime, int socialTime,
                            int entertainmentTime, int totalTime, int peakUsageHour) {
        this.date = date;
        this.userId = userId;
        this.studyTime = studyTime;
        this.socialTime = socialTime;
        this.entertainmentTime = entertainmentTime;
        this.totalTime = totalTime;
        this.peakUsageHour = peakUsageHour;
    }

    public ScreenTimeRecord(long userId, int studyTime, int socialTime,
                            int entertainmentTime, int peakUsageHour) {
        this(LocalDate.now(), userId, studyTime, socialTime, entertainmentTime,
             studyTime + socialTime + entertainmentTime, peakUsageHour);
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public int getStudyTime() { return studyTime; }
    public void setStudyTime(int time) { this.studyTime = time; }

    public int getSocialTime() { return socialTime; }
    public void setSocialTime(int time) { this.socialTime = time; }

    public int getEntertainmentTime() { return entertainmentTime; }
    public void setEntertainmentTime(int time) { this.entertainmentTime = time; }

    public int getTotalTime() { return totalTime; }
    public void setTotalTime(int time) { this.totalTime = time; }

    public int getPeakUsageHour() { return peakUsageHour; }
    public void setPeakUsageHour(int hour) { this.peakUsageHour = hour; }

    @Override
    public String toString() {
        return String.format(
            "[%s] Study: %d | Social: %d | Entertainment: %d | Total: %d min | Peak: %02d:00",
            date, studyTime, socialTime, entertainmentTime, totalTime, peakUsageHour
        );
    }
}
