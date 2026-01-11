package com.mistraltracker.service;

import com.mistraltracker.dto.LiveObjectsMessage;
import com.mistraltracker.model.WeatherData;
import com.mistraltracker.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataProcessingService {

    private final WeatherDataRepository repository;
    private final DecoderService decoderService;

    public void processMessage(LiveObjectsMessage message) {
        String hexPayload = message.getValue().getPayload();

        WeatherData data = decoderService.decoder(message);

        log.info("🛠️ Payload treatment: {}", hexPayload);

        repository.save(data);
        log.info("✅ Data saved on database with ID: {}", data.getId());
    }
}