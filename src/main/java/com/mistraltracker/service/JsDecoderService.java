package com.mistraltracker.service;

import com.mistraltracker.model.WeatherData;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JsDecoderService {

    private Source jsSource;

    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource("decoder.js");
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            this.jsSource = Source.newBuilder("js", reader, "decoder.js").build();
        }
    }

    public WeatherData decode(String hexPayload, int port) {
        try (Context context = Context.newBuilder("js").allowAllAccess(true).build()) {
            context.eval(jsSource);
            Value function = context.getBindings("js").getMember("decodeUplink");

            if (function == null || !function.canExecute()) {
                log.error("Fonction decodeUplink missing!");
                return new WeatherData();
            }

            List<Integer> byteList = hexStringToByteList(hexPayload);
            int[] byteArray = byteList.stream().mapToInt(i -> i).toArray();

            Map<String, Object> inputObj = new HashMap<>();
            inputObj.put("bytes", byteArray);

            Value jsResult = function.execute(inputObj, port);

            log.info("JS result: {}", jsResult.toString());

            return mapJsResultToWeatherData(jsResult);

        } catch (Exception e) {
            log.error("ERROR while JS decoding: ", e);
            return new WeatherData();
        }
    }

    private WeatherData mapJsResultToWeatherData(Value jsResult) {
        WeatherData data = new WeatherData();

        if (!jsResult.hasMember("data")) return data;
        Value internalData = jsResult.getMember("data");

        if (!internalData.hasMember("messages")) return data;
        Value messagesArray = internalData.getMember("messages");

        for (long i = 0; i < messagesArray.getArraySize(); i++) {
            Value msg = messagesArray.getArrayElement(i);
            log.info("Message decoded: {}", msg);

            if (msg.hasMember("type") && msg.hasMember("measurementValue")) {
                String type = msg.getMember("type").asString().trim();
                double value = msg.getMember("measurementValue").asDouble();

                switch (type) {
                    case "Air Temperature": data.setTemperature(value); break;
                    case "Air Humidity":    data.setHumidity(value); break;
                    case "Wind Speed":      data.setWindSpeed(value); break;
                    case "Wind Direction Sensor": data.setWindDirection(value); break;
                    case "Rain Gauge":      data.setRainLevel(value); break;
                    case "Light Intensity": data.setLightIntensity(value); break;
                    case "UV Index":        data.setUvIntensity(value); break;
                    case "Barometric Pressure": data.setPressure(value); break;
                    case "Peak Wind Gust":  break;
                    default: log.warn("Type ignoré: {}", type);
                }
            }
            if (msg.hasMember("Battery(%)")) {
                data.setBatteryLevel(msg.getMember("Battery(%)").asInt());
            }
        }

        analyzeWeatherConditions(data);
        return data;
    }

    public void analyzeWeatherConditions(WeatherData data) {
        double rain = data.getRainLevel() != null ? data.getRainLevel() : 0.0;
        double wind = data.getWindSpeed() != null ? data.getWindSpeed() : 0.0;
        double temp = data.getTemperature() != null ? data.getTemperature() : 0.0;
        double humidity = data.getHumidity() != null ? data.getHumidity() : 0.0;
        double uv = data.getUvIntensity() != null ? data.getUvIntensity() : 0.0;

        data.setRainScore(rain > 0.2 ? 100.0 : 0.0);
        data.setWindScore(Math.min(100.0, (wind / 20.0) * 100.0));
        data.setUvScore(Math.min(100.0, (uv / 12.0) * 100.0));

        if (humidity > 95.0 && wind < 2.0) data.setFogScore(100.0);
        else if (humidity > 90.0 && wind < 3.0) data.setFogScore(50.0);
        else data.setFogScore(0.0);

        data.setGoodForWaterSports(wind > 4.0 && rain < 5.0);
        data.setGoodForSwimming(wind < 5.0 && temp > 22.0 && rain == 0);
        data.setGoodForFishing(wind < 3.0 && rain < 2.0);
        data.setGoodForBoating(wind < 4.0 && rain == 0.0 && humidity < 95.0);
    }

    private List<Integer> hexStringToByteList(String s) {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < s.length(); i += 2) {
            data.add(Integer.parseInt(s.substring(i, i + 2), 16));
        }
        return data;
    }
}