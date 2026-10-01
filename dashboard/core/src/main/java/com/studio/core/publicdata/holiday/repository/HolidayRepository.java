package com.studio.core.publicdata.holiday.repository;

import com.studio.core.publicdata.holiday.entity.HolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HolidayRepository extends JpaRepository<HolidayEntity, Long> {

    List<HolidayEntity> findByLocdateStartingWithOrderByLocdateAsc(String yearMonth);

    List<HolidayEntity> findByLocdateBetween(String startLocdate, String endLocdate);
}
