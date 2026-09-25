package com.studio.core.publicdata.weather.repository;

import com.studio.core.publicdata.weather.entity.WeatherEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface WeatherRepository extends JpaRepository<WeatherEntity, Long> {

    boolean existsByNxAndNyAndBaseDateAndBaseTime(
            int nx,
            int ny,
            String baseDate,
            String baseTime
    );

    List<WeatherEntity> findByNxAndNyAndFcstDateIn(int nx, int ny, Collection<String> fcstDates);
    List<WeatherEntity> findByNxAndNyAndFcstDateGreaterThanEqual(int nx, int ny, String fcstDate);

    @Transactional
    @Modifying
    @Query("DELETE FROM WeatherEntity w WHERE w.fcstDate < :targetDate")
    void deleteByFcstDateBefore(@Param("targetDate") String targetDate);
}
