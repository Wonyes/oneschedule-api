package com.studio.api.publicdata.holiday.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class HolidayClient {

    private final String serviceKey;
    private final RestTemplate restTemplate;

    public HolidayClient(
            RestTemplate restTemplate,
            @Value("${weather.service-key}") String serviceKey
    ) {
        this.restTemplate = restTemplate;
        this.serviceKey = serviceKey;
    }

    public String getHolidays(int year, int month) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
                .queryParam("ServiceKey", serviceKey)
                .queryParam("solYear", year)
                .queryParam("solMonth", String.format("%02d", month))
                .queryParam("dataType", "JSON")
                .build(true)
                .toUri();

        return restTemplate.getForObject(uri, String.class) ;
    }


}