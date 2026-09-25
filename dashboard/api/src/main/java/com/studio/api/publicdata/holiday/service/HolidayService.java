package com.studio.api.publicdata.holiday.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studio.api.publicdata.holiday.client.HolidayClient;
import com.studio.core.publicdata.holiday.entity.HolidayEntity;
import com.studio.core.publicdata.holiday.repository.HolidayRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
public class HolidayService {

    private final HolidayClient holidayClient;
    private final HolidayRepository holidayRepository;
    private final ObjectMapper objectMapper;

    public HolidayService(
            HolidayClient holidayClient,
            HolidayRepository holidayRepository,
            ObjectMapper objectMapper
    ) {
        this.holidayClient = holidayClient;
        this.holidayRepository = holidayRepository;
        this.objectMapper = objectMapper;
    }

    public List<HolidayEntity> getHolidays(Integer year, Integer month) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        String yearMonth = targetYear + String.format("%02d", targetMonth);

        List<HolidayEntity> holidayEntities =
                holidayRepository.findByLocdateStartingWithOrderByLocdateAsc(yearMonth);

        if (holidayEntities.isEmpty()) {
            fetchAndSaveExternalHolidays(targetYear, targetMonth);

            holidayEntities = holidayRepository.findByLocdateStartingWithOrderByLocdateAsc(yearMonth);
        }

        return holidayEntities;
    }

    @Transactional
    public void fetchAndSaveExternalHolidays(int year, int month) {
        try {
            String rawJson = holidayClient.getHolidays(year, month);
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isMissingNode() || items.isNull()) {
                return;
            }

            List<Map<String, Object>> holidayMaps;
            if (items.isArray()) {
                holidayMaps = objectMapper.convertValue(items, new TypeReference<List<Map<String, Object>>>() {});
            } else if (items.isObject()) {
                Map<String, Object> singleItem = objectMapper.convertValue(items, new TypeReference<Map<String, Object>>() {});
                holidayMaps = Collections.singletonList(singleItem);
            } else {
                return;
            }

            List<HolidayEntity> existingHolidays =
                    holidayRepository.findByLocdateBetween(
                            year + String.format("%02d", month) + "01",
                            year + String.format("%02d", month) + "31"
                    );

            Set<String> existingKeys = existingHolidays.stream()
                    .map(h -> h.getLocdate() + "|" + h.getDateName())
                    .collect(Collectors.toSet());

            List<HolidayEntity> toSave = new ArrayList<>();

            for (Map<String, Object> map : holidayMaps) {
                String locdate = getMapValue(map, "locdate");
                String dateName = getMapValue(map, "dateName", "datename");
                String isHoliday = getMapValue(map, "isHoliday", "isholiday");

                if (locdate == null || dateName == null || dateName.isBlank()) {
                    continue;
                }
                String key = locdate + "|" + dateName;
                if (!existingKeys.contains(key)) {
                    HolidayEntity holidayEntity = HolidayEntity.builder()
                            .locdate(locdate)
                            .dateName(dateName)
                            .isHoliday(isHoliday)
                            .build();

                    toSave.add(holidayEntity);
                }
            }

            // 루프 안에서 건건이 저장하지 않고 한 번에 넘긴다
            if (!toSave.isEmpty()) holidayRepository.saveAll(toSave);


        } catch (Exception e) {
            log.error("holiday Error", e);
        }
    }

    private String getMapValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return String.valueOf(map.get(key));
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key) && entry.getValue() != null) {
                    return String.valueOf(entry.getValue());
                }
            }
        }
        return null;
    }

    @Scheduled(cron = "0 0 2 1 * *")
    public void scheduleHolidaySync() {
        int currentYear = LocalDate.now().getYear();

        for (int y : List.of(currentYear, currentYear + 1)) {
            for (int m = 1; m <= 12; m++) {
                fetchAndSaveExternalHolidays(y, m);
            }
        }
    }
}