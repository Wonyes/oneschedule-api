package com.studio.core.publicdata.weather.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
        name = "tb_weather",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"nx","ny","fcstDate"})
        }
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int nx;
    private int ny;

    private String baseDate;
    private String baseTime;

    private String fcstDate;
    private String fcstTime;

    private String tmp;
    private String reh;
    private String sky;
    private String pty;


    @Builder
    public WeatherEntity(int nx, int ny, String baseDate, String baseTime, String fcstDate, String fcstTime, String tmp, String reh, String sky, String pty){
        this.nx = nx;
        this.ny = ny;

        this.baseDate = baseDate;
        this.baseTime = baseTime;

        this.fcstDate = fcstDate;
        this.fcstTime = fcstTime;

        this.tmp = tmp;
        this.reh = reh;
        this.sky = sky;
        this.pty = pty;
    }

    public void updateWeather(
            String tmp,
            String reh,
            String sky,
            String pty,
            String baseDate,
            String baseTime,
            String fcstTime
    ) {
        this.baseDate = baseDate;
        this.baseTime = baseTime;
        this.fcstTime = fcstTime;

        this.tmp = tmp;
        this.reh = reh;
        this.sky = sky;
        this.pty = pty;
    }
}
