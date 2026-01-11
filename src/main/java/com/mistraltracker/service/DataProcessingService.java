package com.mistraltracker.service;

import com.mistraltracker.dto.LiveObjectsMessage;
import com.mistraltracker.model.WeatherData;
import com.mistraltracker.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataProcessingService {

    private final WeatherDataRepository repository;
    private final DecoderService decoderService;

    public void processMessage(LiveObjectsMessage message) {
        String hexPayload = message.getValue().getPayload();
        log.info("Payload treatment: {}", hexPayload);
        WeatherData newData = decoderService.decoder(message);

        Optional<WeatherData> lastDataInDb = repository.findTopByOrderByTimestampDesc();
        if (lastDataInDb.isPresent()) {
            WeatherData lastData = lastDataInDb.get();
            compareNewDataWithLast(newData, lastData);
            decoderService.mistralScoreCalculation(newData);
        }
        repository.save(newData);
        log.info("Data saved on database with ID: {}", newData.getId());
    }

    public void compareNewDataWithLast (WeatherData newData, WeatherData oldData) {
        if(newData.getTemperature() == null) newData.setTemperature(oldData.getTemperature());
        if(newData.getHumidity() == null) newData.setHumidity(oldData.getHumidity());
        if(newData.getWindSpeed() == null) newData.setWindSpeed(oldData.getWindSpeed());
        if(newData.getWindDirection() == null) newData.setWindDirection(oldData.getWindDirection());
        if(newData.getRainLevel() == null) newData.setRainLevel(oldData.getRainLevel());
        if(newData.getBatteryLevel() == null) newData.setBatteryLevel(oldData.getBatteryLevel());
        if(newData.getLightIntensity() == null) newData.setLightIntensity(oldData.getLightIntensity());
        if(newData.getUvIntensity() == null) newData.setUvIntensity(oldData.getUvIntensity());
    }
}