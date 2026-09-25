package com.studio.core.publicdata.holiday.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_holiday", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"locdate", "date_name"})
})
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HolidayEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String locdate;

    @Column(name = "date_name", nullable = false)
    private String dateName;

    @Column(name = "is_holiday",length = 1)
    private String isHoliday;

    @Builder
    public HolidayEntity(String locdate, String dateName, String isHoliday) {
        this.locdate = locdate;
        this.dateName = dateName;
        this.isHoliday = isHoliday;
    }

}


