package com.detox;
import java.io.*;
import java.util.*;

/**
 * Handles persistence: saving records to CSV and loading them back.
 * File location: records_<username>.csv in the working directory.
 */
public class FileManager {

    private static final String DIR = "data";

    public FileManager() {
        // Ensure the data directory exists
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private String filePath(String userName) {
        return DIR + File.separator + "records_" + sanitize(userName) + ".csv";
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    /**
     * Appends a single record to the user's CSV file.
     * Creates the file with a header row if it doesn't exist yet.
     */
    public void saveRecord(ScreenTimeRecord record) {
        String path = filePath(record.getUserName());
        File file = new File(path);
        boolean newFile = !file.exists();

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (newFile) {
                pw.println(ScreenTimeRecord.csvHeader());
            }
            pw.println(record.toCsvLine());
            System.out.println("\n✅ Record saved to: " + path);
        } catch (IOException e) {
            System.err.println("⚠ Could not save record: " + e.getMessage());
        }
    }

    /**
     * Loads all records for a given user from the CSV file.
     * Returns an empty list if the file doesn't exist or is unreadable.
     */
    public List<String> loadRecords(String userName) {
        List<String> lines = new ArrayList<>();
        String path = filePath(userName);
        File file = new File(path);

        if (!file.exists()) {
            System.out.println("ℹ No saved records found for: " + userName);
            return lines;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean headerSkipped = false;
            while ((line = br.readLine()) != null) {
                if (!headerSkipped) { headerSkipped = true; continue; } // skip CSV header
                if (!line.isBlank()) lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("⚠ Could not load records: " + e.getMessage());
        }
        return lines;
    }

    /**
     * Prints all previously saved records for a user to the console.
     */
    public void printHistory(String userName) {
        List<String> records = loadRecords(userName);
        System.out.println("\n📂 Saved History for: " + userName);
        System.out.println("──────────────────────────────────────────────────────────────────");
        System.out.println(ScreenTimeRecord.csvHeader());
        if (records.isEmpty()) {
            System.out.println("  (No records yet)");
        } else {
            records.forEach(System.out::println);
        }
        System.out.println("──────────────────────────────────────────────────────────────────");
    }

    /**
     * Counts the number of "streak" days where the user stayed within their safe limit.
     * Reads from saved CSV data — simple heuristic on TotalTime vs SafeLimit.
     */
    public int calculateStreak(String userName, int safeLimit) {
        List<String> records = loadRecords(userName);
        int streak = 0;
        // Records are in chronological order; count from the most recent backward
        for (int i = records.size() - 1; i >= 0; i--) {
            String[] parts = records.get(i).split(",");
            if (parts.length >= 6) {
                try {
                    int total = Integer.parseInt(parts[5].trim());
                    if (total <= safeLimit) {
                        streak++;
                    } else {
                        break; // streak broken
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return streak;
    }

    /**
     * Loads all records for a user and returns them as ScreenTimeRecord objects.
     */
    public List<ScreenTimeRecord> loadHistory(String userName) {
        List<String> rawRecords = loadRecords(userName);
        List<ScreenTimeRecord> history = new ArrayList<>();
        for (String line : rawRecords) {
            String[] p = line.split(",");
            if (p.length >= 7) {
                try {
                    history.add(new ScreenTimeRecord(
                        java.time.LocalDate.parse(p[0].trim()),
                        p[1].trim(),
                        Integer.parseInt(p[2].trim()),
                        Integer.parseInt(p[3].trim()),
                        Integer.parseInt(p[4].trim()),
                        Integer.parseInt(p[5].trim()),
                        Integer.parseInt(p[6].trim())
                    ));
                } catch (Exception ignored) {}
            }
        }
        return history;
    }
}
