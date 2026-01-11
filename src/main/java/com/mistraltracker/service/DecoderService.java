package com.mistraltracker.service;

import com.mistraltracker.dto.LiveObjectsMessage;
import com.mistraltracker.model.WeatherData;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class DecoderService {

    public WeatherData decoder(LiveObjectsMessage message) {
        WeatherData data = new WeatherData();

        String payload = message.getValue().getPayload();

        // TODO: decode the payload

        data.setTimestamp(LocalDateTime.now());
        if (message.getMetadata() != null
                && message.getMetadata().getNetwork() != null
                && message.getMetadata().getNetwork().getLora() != null) {

            data.setDeviceEui(message.getMetadata().getNetwork().getLora().getDevEUI());
        }
        return data;
    }
}
