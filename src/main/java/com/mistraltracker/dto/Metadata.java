package com.mistraltracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {
    private Network network;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Network {
        private Lora lora;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Lora {
        private String devEUI;
        private Integer signalQuality;
        private Integer signalNoiseRatio;
    }
}