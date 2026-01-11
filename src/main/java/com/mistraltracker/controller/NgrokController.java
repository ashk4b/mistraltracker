package com.mistraltracker.controller;

import com.mistraltracker.dto.LiveObjectsMessage;
import com.mistraltracker.service.DataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@Slf4j
@RequiredArgsConstructor
public class NgrokController {

    private final DataProcessingService dataProcessingService;

    @PostMapping
    public ResponseEntity<String> receiveData(@RequestBody LiveObjectsMessage message) {
        log.info("📡 Data received from device: {}", message.getMetadata().getNetwork().getLora().getDevEUI());
        log.info("📦 Payload Hex: {}", message.getValue().getPayload());

        dataProcessingService.processMessage(message);
        return ResponseEntity.ok("Received");
    }
}