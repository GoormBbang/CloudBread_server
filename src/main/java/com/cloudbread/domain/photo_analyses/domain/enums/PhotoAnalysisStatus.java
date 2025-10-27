package com.cloudbread.domain.photo_analyses.domain.enums;

public enum PhotoAnalysisStatus {
    UPLOADED, // 사진 업로드 완료, AI 트리거 호출함
    LABELED, // AI가 음식명 라벨 반환
    CANDIDATES_READY, // ES 후보 3개 뽑아 DB 저장 완료
    USER_CONFIRMED, // 유저가 최종 1개 선택 완료
    FAILED, // 실패
    NO_CANDIDATES,        // 후보 없음
}


