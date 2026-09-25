package com.studio.api.publicdata.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studio.api.publicdata.weather.client.WeatherClient;
import com.studio.core.publicdata.weather.entity.WeatherEntity;
import com.studio.core.publicdata.weather.repository.WeatherRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherClient weatherClient;
    private final WeatherRepository weatherRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Map<String, Object>> getWeather(int nx, int ny) {

        BaseDateTime base = getBaseTime();

        boolean hasLatestData =
                weatherRepository.existsByNxAndNyAndBaseDateAndBaseTime(
                        nx, ny,
                        base.getBaseDate(), base.getBaseTime()
                );


        if (!hasLatestData) {
            fetchAndSaveWeather(nx, ny);
        }

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<WeatherEntity> weatherList =
                weatherRepository.findByNxAndNyAndFcstDateGreaterThanEqual(nx, ny, today);

        return convertEntityToProcessedMap(weatherList);
    }

    public void fetchAndSaveWeather(int nx, int ny) {

        BaseDateTime base = getBaseTime();

        try {

            String rawJson = weatherClient.getWeather(
                    nx, ny,
                    base.getBaseDate(), base.getBaseTime()
            );

            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode items = root.path("response")
                    .path("body")
                    .path("items")
                    .path("item");

            if (items.isMissingNode() || !items.isArray()) {
                return;
            }

            String currentHourKey =
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH")) + "00";

            Map<String, Map<String, String>> groupedData = new LinkedHashMap<>();

            for (JsonNode item : items) {

                String fcstDate = item.path("fcstDate").asText();
                String fcstTime = item.path("fcstTime").asText();

                String category = item.path("category").asText();
                String value = item.path("fcstValue").asText();

                Map<String, String> weather =
                        groupedData.computeIfAbsent(fcstDate, k -> {

                            Map<String, String> map = new LinkedHashMap<>();
                            map.put("fcstDate", fcstDate);
                            map.put("fcstTime", fcstTime);
                            return map;

                        });

                String savedTime = weather.get("fcstTime");

                int currentGap =
                        Math.abs(Integer.parseInt(fcstTime) - Integer.parseInt(currentHourKey));

                int savedGap =
                        Math.abs(Integer.parseInt(savedTime) - Integer.parseInt(currentHourKey));

                if (currentGap < savedGap) {

                    weather.put("fcstTime", fcstTime);

                    weather.remove("TMP");
                    weather.remove("REH");
                    weather.remove("SKY");
                    weather.remove("PTY");
                }

                if (weather.get("fcstTime").equals(fcstTime)) {

                    weather.put(category, value);

                }
            }

            Map<String, WeatherEntity> existing =
                    weatherRepository.findByNxAndNyAndFcstDateIn(nx, ny, groupedData.keySet())
                            .stream()
                            .collect(toMap(WeatherEntity::getFcstDate, w -> w));

            List<WeatherEntity> toSave = groupedData.values().stream()
                    .map(d -> {
                        WeatherEntity found = existing.get(d.get("fcstDate"));
                        if (found != null) {
                            found.updateWeather(d.get("TMP"), d.get("REH"), d.get("SKY"), d.get("PTY"),
                                    base.getBaseDate(), base.getBaseTime(), d.get("fcstTime"));
                            return found;
                        }
                        return WeatherEntity.builder()
                                .nx(nx).ny(ny)
                                .baseDate(base.getBaseDate()).baseTime(base.getBaseTime())
                                .fcstDate(d.get("fcstDate")).fcstTime(d.get("fcstTime"))
                                .tmp(d.get("TMP")).reh(d.get("REH")).sky(d.get("SKY")).pty(d.get("PTY"))
                                .build();
                    })
                    .toList();

            weatherRepository.saveAll(toSave);

        } catch (org.springframework.web.client.HttpStatusCodeException e) {

            System.err.println(
                    "공공데이터 서버 장애 발생 : "
                            + e.getStatusCode()
                            + " - "
                            + e.getResponseBodyAsString()
            );

        } catch (Exception e) {
            log.error("error", e);
            e.printStackTrace();
        }
    }


    private Map<String, Map<String, Object>> convertEntityToProcessedMap(List<WeatherEntity> weatherList) {
        Map<String, Map<String, Object>> processed = new HashMap<>();

        for (WeatherEntity entity : weatherList) {
            String fcstDate = entity.getFcstDate();

            Map<String, Object> dateMap = processed.computeIfAbsent(fcstDate, k -> {
                Map<String, Object> map = new HashMap<>();
                map.put("date", fcstDate);
                map.put("time", entity.getFcstTime());
                return map;
            });

            if (entity.getTmp() != null) dateMap.put("TMP", entity.getTmp());
            if (entity.getReh() != null) dateMap.put("REH", entity.getReh());
            if (entity.getSky() != null) dateMap.put("SKY", entity.getSky());
            if (entity.getPty() != null) dateMap.put("PTY", entity.getPty());
        }

        return processed;
    }

    private BaseDateTime getBaseTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate date = now.toLocalDate();
        LocalTime time = now.toLocalTime();

        String baseDate;
        String baseTime;

        if (time.isBefore(LocalTime.of(2, 10))) {
            baseDate = date.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "2300";
        } else if (time.isBefore(LocalTime.of(5, 10))) {
            baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "0200";
        } else if (time.isBefore(LocalTime.of(8, 10))) {
            baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "0500";
        } else if (time.isBefore(LocalTime.of(11, 10))) {
            baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "0800";
        } else if (time.isBefore(LocalTime.of(14, 10))) {
            baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "1100";
        } else if (time.isBefore(LocalTime.of(17, 10))) {
            baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "1400";
        } else if (time.isBefore(LocalTime.of(20, 10))) {
            baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "1700";
        } else if (time.isBefore(LocalTime.of(23, 10))) {
            baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "2000";
        } else {
            baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "2300";
        }

        return new BaseDateTime(baseDate, baseTime);
    }

    @Getter
    public static class BaseDateTime {
        private final String baseDate;
        private final String baseTime;

        public BaseDateTime(String baseDate, String baseTime) {
            this.baseDate = baseDate;
            this.baseTime = baseTime;
        }
    }
}