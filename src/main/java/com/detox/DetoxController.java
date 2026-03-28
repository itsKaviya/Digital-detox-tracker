package com.detox;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DetoxController {

    private final Tracker tracker = new Tracker();
    private final FileManager fileManager = new FileManager();

    @GetMapping("/hello")
    public String hello() {
        return "Digital Detox Tracker API is running!";
    }

    @PostMapping("/log")
    public Map<String, Object> logScreenTime(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.getOrDefault("name", "User");
        int safeLimit = (int) payload.getOrDefault("safeLimit", 120);
        int sleepStart = (int) payload.getOrDefault("sleepStart", 22);
        int sleepEnd = (int) payload.getOrDefault("sleepEnd", 6);

        int study = (int) payload.getOrDefault("study", 0);
        int social = (int) payload.getOrDefault("social", 0);
        int entertainment = (int) payload.getOrDefault("entertainment", 0);
        int peakHour = (int) payload.getOrDefault("peakHour", 0);

        User user = new User(name, safeLimit, sleepStart, sleepEnd);
        ScreenTimeRecord record = new ScreenTimeRecord(name, study, social, entertainment, peakHour);
        
        String pattern = new Analyzer().analyzePattern(record, user);
        tracker.processDay(user, study, social, entertainment, peakHour);

        int score = new DetoxScoreCalculator().calculateScore(record, user, pattern);

        return Map.of(
            "status", "success",
            "message", "Screen time logged successfully",
            "score", score,
            "user", user
        );
    }

    @GetMapping("/history")
    public List<ScreenTimeRecord> getHistory(@RequestParam(defaultValue = "User") String name) {
        return fileManager.loadHistory(name);
    }
}
