package com.detox;
import java.time.LocalDate;

/**
 * Holds one day's screen-time data for a user.
 */
public class ScreenTimeRecord {
    private LocalDate date;
    private String userName;
    private int studyTime;         // minutes
    private int socialTime;        // minutes
    private int entertainmentTime; // minutes
    private int totalTime;         // computed
    private int peakUsageHour;     // 0-23, optional

    public ScreenTimeRecord() {} // Default constructor for JSON

    public ScreenTimeRecord(LocalDate date, String userName, int studyTime, int socialTime,
                            int entertainmentTime, int totalTime, int peakUsageHour) {
        this.date = date;
        this.userName = userName;
        this.studyTime = studyTime;
        this.socialTime = socialTime;
        this.entertainmentTime = entertainmentTime;
        this.totalTime = totalTime;
        this.peakUsageHour = peakUsageHour;
    }

    public ScreenTimeRecord(String userName, int studyTime, int socialTime,
                            int entertainmentTime, int peakUsageHour) {
        this(LocalDate.now(), userName, studyTime, socialTime, entertainmentTime, 
             studyTime + socialTime + entertainmentTime, peakUsageHour);
    }

    // Getters
    public LocalDate getDate()           { return date; }
    public String getUserName()          { return userName; }
    public int getStudyTime()            { return studyTime; }
    public int getSocialTime()           { return socialTime; }
    public int getEntertainmentTime()    { return entertainmentTime; }
    public int getTotalTime()            { return totalTime; }
    public int getPeakUsageHour()        { return peakUsageHour; }

    /** Comma-separated line for CSV persistence. */
    public String toCsvLine() {
        return String.join(",",
            date.toString(),
            userName,
            String.valueOf(studyTime),
            String.valueOf(socialTime),
            String.valueOf(entertainmentTime),
            String.valueOf(totalTime),
            String.valueOf(peakUsageHour)
        );
    }

    /** CSV header */
    public static String csvHeader() {
        return "Date,UserName,StudyTime,SocialTime,EntertainmentTime,TotalTime,PeakUsageHour";
    }

    @Override
    public String toString() {
        return String.format(
            "[%s] %s | Study: %d | Social: %d | Entertainment: %d | Total: %d min | Peak Hour: %02d:00",
            date, userName, studyTime, socialTime, entertainmentTime, totalTime, peakUsageHour
        );
    }
}
