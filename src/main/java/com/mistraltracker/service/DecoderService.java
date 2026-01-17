package com.mistraltracker.service;

import com.mistraltracker.dto.LiveObjectsMessage;
import com.mistraltracker.model.WeatherData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.mistraltracker.repository.WeatherDataRepository;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DecoderService {

    private static final int ID_AIR_TEMP = 0x4097;
    private static final int ID_AIR_HUMIDITY = 0x4098;
    private static final int ID_WIND_SPEED = 0x4191;
    private static final int ID_WIND_DIRECTION = 0x4193;
    private static final int ID_RAIN_GAUGE = 0x4194;
    private static final int ID_LIGHT_INTENSITY = 0x4099;
    private static final int ID_UV_INDEX = 0x4190;

    private static final int ID_TEMP_ALT = 0x4A00;
    private static final int ID_WIND_ALT = 0x4B00;
    private static final int ID_BATTERY  = 0x4C00;
    private static final int ID_STATUS_UNK = 0xB000;

    private final WeatherDataRepository repository;

    public WeatherData decoder(LiveObjectsMessage message) {
        WeatherData data = new WeatherData();
        data.setTimestamp(LocalDateTime.now());

        if (message.getMetadata() != null
                && message.getMetadata().getNetwork() != null
                && message.getMetadata().getNetwork().getLora() != null) {
            data.setDeviceEui(message.getMetadata().getNetwork().getLora().getDevEUI());
        }

        if(message.getValue() == null || message.getValue().getPayload() == null) {
            return data;
        }

        String payload = message.getValue().getPayload();

        try {
            byte[] bytes = HexFormat.of().parseHex(payload);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            while(buffer.remaining() >= 2){
                buffer.order(ByteOrder.BIG_ENDIAN);
                int sensorId = Short.toUnsignedInt(buffer.getShort());
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                switch (sensorId) {
                    case ID_AIR_TEMP:
                    case ID_TEMP_ALT:
                        if(buffer.remaining() >= 4) {
                            double temperature = buffer.getInt() / 1000.0;
                            data.setTemperature(temperature);
                        } else if (buffer.remaining() >= 2) {
                            data.setTemperature(buffer.getShort() / 1000.0);
                        }
                        break;
                    case ID_WIND_SPEED:
                    case ID_WIND_ALT:
                        if(buffer.remaining() >= 4) {
                            double windSpeed = buffer.getInt() / 1000.0;
                            data.setWindSpeed(windSpeed);
                        } else if (buffer.remaining() >= 2) {
                            data.setWindSpeed(buffer.getShort() / 10.0);
                        }
                        break;
                    case ID_STATUS_UNK:
                        if(buffer.remaining() > 0) buffer.get();
                        break;
                    case 0x0000:
                        break;
                    case ID_AIR_HUMIDITY:
                        if(buffer.remaining() >= 2) data.setHumidity(buffer.getShort()/100.0);
                        break;
                    case ID_WIND_DIRECTION:
                        if(buffer.remaining() >= 2) data.setWindDirection((double)buffer.getShort());
                        break;
                    case ID_RAIN_GAUGE:
                        if(buffer.remaining() >= 4) data.setRainLevel(buffer.getInt()/1000.0);
                        break;
                    case ID_LIGHT_INTENSITY:
                        if(buffer.remaining() >= 4) data.setLightIntensity((double)buffer.getInt());
                        break;
                    case ID_BATTERY:
                        if(buffer.remaining() >= 2) {
                            int battery = buffer.getShort();
                            data.setBatteryLevel(battery);
                        }
                        break;
                    default:
                        log.warn("Unknown ID: {}", Integer.toHexString(sensorId));
                        if(buffer.remaining() > 0) buffer.get();
                        break;
                }
            }
            scoreFogCalculation(data);
            scoreUvCalculation(data);
        } catch (Exception e) {
            log.error("Decoding error", e);
        }
        return data;
    }

    public void scoreFogCalculation (WeatherData data) {
        Double temperature = data.getTemperature();
        Double humidity = data.getHumidity();

        double lambda = Math.log(humidity)/100 + (17.62 * temperature)/(243.12 + temperature);
        double td = (243.12 * lambda)/(17.62 - lambda);
        double delta = temperature - td;

        if (delta < 0.5) {
            data.setIsFogOk(true);
        } else if (delta > 3) {
            data.setIsFogOk(false);
        }
    }

    public void scoreRainCalculation (WeatherData data) {

    }

    public void scoreUvCalculation (WeatherData data) {
        Double lightIntensity = data.getLightIntensity();
        Double uvIntensity = data.getUvIntensity();
        List<WeatherData> previousData = repository.findTop10ByOrderByTimestampDesc();
        Double previousUvIntensity = previousData.get(0).getUvIntensity();

        double delta = 60;
        double uvRate = (uvIntensity - previousUvIntensity) / delta;
        double sunFactor = Math.log10(lightIntensity + 1.0)/5.0;
        double nextUv = uvIntensity + uvRate * delta * sunFactor;

        if (nextUv > 8) {
            data.setIsUvOk(false);
        } else {
            data.setIsUvOk(true);
        }
    }

    public void scoreWindCalculation (WeatherData data) {
        Double windSpeed = data.getWindSpeed();
        List<WeatherData> previousData = repository.findTop10ByOrderByTimestampDesc();
        Double previousWindSpeed = previousData.get(0).getWindSpeed();
    }
}
