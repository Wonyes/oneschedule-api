package com.studio.api.publicdata.weather.controller;

import com.studio.core.global.response.SuccessResponse;
import com.studio.api.publicdata.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Operation(summary = "날씨 정보 최대 3일")
    @GetMapping("/info")
    public SuccessResponse<?> getWeather (
            @RequestParam(value = "nx", defaultValue = "60") int nx,
            @RequestParam(value = "ny", defaultValue = "127") int ny
    ) {
        return SuccessResponse.ok(weatherService.getWeather(nx,ny));
    }
}
