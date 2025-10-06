package com.cloudbread.domain.photo_analyses.dto;

import com.cloudbread.domain.photo_analyses.application.PhotoAnalysisService;
import com.cloudbread.domain.photo_analyses.domain.enums.PhotoAnalysisStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PhotoAnalysisResponse {
    public record UploadResponse(Long photoAnalysisId, String imageUrl, PhotoAnalysisStatus status) {}
    public record Ok(boolean ok) {}

    @Builder
    @Getter
    public static class CandidateItem {
        private Long foodId;
        private String name;
        private double score; // 0~1 임시 점수(간단 유사도)
        private java.math.BigDecimal calories; // 예: 320
        private Map<String,NutrientValue> nutrients; // 영양성분 키/값(있으면만)
    }

    @Builder @Getter
    public static class CandidatesPayload {
        private Long photoAnalysisId;
        private String query;
        private String status; // "CANDIDATES_READY"
        private List<CandidateItem> candidates;
    }

    @Builder @Getter
    public static class NutrientValue {
        private java.math.BigDecimal value; // 25.0
        private String unit;                // "g" | "mg" | "μg" | "kcal"
    }

    @Builder @Getter
    public static class ConfirmSelected {
        private Long foodId;
        private String name;
        private String category;
        private String serving;          // 예: "기준량 100 g"
        private BigDecimal calories;     // 별도 필드로 분리
        private Map<String, NutrientValue> nutrients; // calories 제외 나머지
    }

    @Builder @Getter
    public static class ConfirmResponse {
        private Long photoAnalysisId;
        private String status; // "USER_CONFIRMED"
        private ConfirmSelected selected;
    }

}
