package com.studio.api.publicdata.weather.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class WeatherClient {

    private final String serviceKey;
    private final RestTemplate restTemplate;

    public WeatherClient(
            RestTemplate restTemplate,
            @Value("${weather.service-key}") String serviceKey
    ) {
        this.restTemplate = restTemplate;
        this.serviceKey = serviceKey;
    }

    public String getWeather(int nx, int ny, String baseDate, String baseTime) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst")
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", "1")
                .queryParam("numOfRows", "1000")
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build(true)
                .toUri();

        return restTemplate.getForObject(uri, String.class);
    }
}