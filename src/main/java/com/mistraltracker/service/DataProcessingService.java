package com.mistraltracker.service;

import com.mistraltracker.dto.LiveObjectsMessage;
import com.mistraltracker.model.WeatherData;
import com.mistraltracker.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataProcessingService {

    private final WeatherDataRepository repository;
    private final JsDecoderService decoderService;

    public void processMessage(LiveObjectsMessage message) {
        if (message.getValue() == null || message.getValue().getPayload() == null) {
            log.warn("Empty message received.");
            return;
        }

        String hexPayload = message.getValue().getPayload();
        log.info("Payload treatment: {}", hexPayload);

        int port = 3;
        if (message.getMetadata() != null
                && message.getMetadata().getNetwork() != null
                && message.getMetadata().getNetwork().getLora() != null) {
            port = message.getMetadata().getNetwork().getLora().getPort();
        }

        WeatherData newData = decoderService.decode(hexPayload, port);

        newData.setTimestamp(LocalDateTime.now());

        if (message.getMetadata() != null
                && message.getMetadata().getNetwork() != null
                && message.getMetadata().getNetwork().getLora() != null) {
            newData.setDeviceEui(message.getMetadata().getNetwork().getLora().getDevEUI());
        }

        Optional<WeatherData> lastDataInDb = repository.findTopByOrderByTimestampDesc();
        if (lastDataInDb.isPresent()) {
            WeatherData lastData = lastDataInDb.get();
            compareNewDataWithLast(newData, lastData);

            decoderService.analyzeWeatherConditions(newData);
        }

        repository.save(newData);
        log.info("Data saved on database with ID: {}", newData.getId());
    }

    public void compareNewDataWithLast(WeatherData newData, WeatherData oldData) {
        if (newData.getTemperature() == null || newData.getTemperature() > 45) newData.setTemperature(oldData.getTemperature());
        if (newData.getHumidity() == null) newData.setHumidity(oldData.getHumidity());
        if (newData.getWindSpeed() == null) newData.setWindSpeed(oldData.getWindSpeed());
        if (newData.getWindDirection() == null) newData.setWindDirection(oldData.getWindDirection());
        if (newData.getRainLevel() == null) newData.setRainLevel(oldData.getRainLevel());
        if (newData.getBatteryLevel() == null) newData.setBatteryLevel(oldData.getBatteryLevel());
        if (newData.getLightIntensity() == null) newData.setLightIntensity(oldData.getLightIntensity());
        if (newData.getUvIntensity() == null) newData.setUvIntensity(oldData.getUvIntensity());
    }
}