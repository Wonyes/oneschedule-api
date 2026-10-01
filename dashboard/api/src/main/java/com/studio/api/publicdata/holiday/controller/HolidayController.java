package com.studio.api.publicdata.holiday.controller;

import com.studio.core.global.response.SuccessResponse;
import com.studio.api.publicdata.holiday.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/holiday")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @Operation(summary = "이번 달 공휴일")
    @GetMapping("/info")
    public SuccessResponse<?> getHolidays (
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month
    ) {
        return SuccessResponse.ok(holidayService.getHolidays(year,month));
    }
}
