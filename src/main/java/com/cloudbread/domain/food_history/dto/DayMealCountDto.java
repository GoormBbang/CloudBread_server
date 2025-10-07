package com.cloudbread.domain.food_history.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayMealCountDto {

    @JsonProperty("day")
    private Integer day;

    @JsonProperty("count")
    @Min(1) @Max(3)
    private Integer count;

    public static DayMealCountDto of(Integer day, Integer count) {
        return DayMealCountDto.builder()
                .day(day)
                .count(Math.min(count, 3))
                .build();
    }
}
