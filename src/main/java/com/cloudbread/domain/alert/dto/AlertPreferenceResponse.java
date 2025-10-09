package com.cloudbread.domain.alert.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AlertPreferenceResponse {
    private Long userId;
    private boolean mealRecoEnabled;
    private boolean nutrientAlertEnabled;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss", // offset(XXX) 제거
            timezone = "Asia/Seoul"
    )
    private LocalDateTime updatedAt;
}
