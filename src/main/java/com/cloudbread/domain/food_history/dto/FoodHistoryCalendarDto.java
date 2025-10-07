package com.cloudbread.domain.food_history.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodHistoryCalendarDto {

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("month")
    private Integer month;

    @JsonProperty("days")
    private List<DayMealCountDto> days;

    public static FoodHistoryCalendarDto createEmpty(Integer year, Integer month) {
        return FoodHistoryCalendarDto.builder()
                .year(year)
                .month(month)
                .days(new ArrayList<>())
                .build();
    }
}
