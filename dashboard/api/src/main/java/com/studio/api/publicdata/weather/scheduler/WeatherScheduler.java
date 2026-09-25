package com.studio.api.publicdata.weather.scheduler;

import com.studio.api.publicdata.weather.service.WeatherService;
import com.studio.core.publicdata.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherService weatherService;
    private final WeatherRepository weatherRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void scheduleWeatherFetch() {
        weatherService.getWeather(60, 127);
        log.info(">>> 시간별 날씨 데이터 수집 완료");
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduleWeatherClean() {
        String targetDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        weatherRepository.deleteByFcstDateBefore(targetDate);
        log.info(">>> 30일 보관 기간이 지난 날씨 데이터 정리 완료 (기준일: {} 이전)", targetDate);
    }
}