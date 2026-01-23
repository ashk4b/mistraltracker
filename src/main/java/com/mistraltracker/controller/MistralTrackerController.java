package com.mistraltracker.controller;

import com.mistraltracker.model.WeatherData;
import com.mistraltracker.repository.WeatherDataRepository;
import com.mistraltracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mistraltracker.service.NotificationService;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RestController
@RequestMapping("/api/mistraltracker")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MistralTrackerController {

    private final WeatherDataRepository repository;

    @Autowired
    private final NotificationService notificationService;

    @Value("${app.alert.recipient}")
    private String recipientEmail;

    @GetMapping("/current")
    public ResponseEntity<WeatherData> getCurrentWeather() {
        return repository.findTopByOrderByTimestampDesc()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<WeatherData>> getHistory() {
        List<WeatherData> history = repository.findTop10ByOrderByTimestampDesc();
        return ResponseEntity.ok(history);
    }

    @PostMapping("/alert")
    public void triggerAlert() {
        notificationService.sendStormAlert(recipientEmail);
    }
}