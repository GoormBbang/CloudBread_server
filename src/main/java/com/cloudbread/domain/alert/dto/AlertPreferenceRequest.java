package com.cloudbread.domain.alert.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlertPreferenceRequest {
    private boolean mealRecoEnabled;
    private boolean nutrientAlertEnabled;
}

