package com.cloudbread.domain.photo_analyses.application;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.entity.FoodNutrient;
import com.cloudbread.domain.food.domain.enums.Unit;
import com.cloudbread.domain.food.domain.repository.FoodNutrientRepository;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import com.cloudbread.domain.photo_analyses.application.candidate.CandidateFinder;
import com.cloudbread.domain.photo_analyses.application.event.PhotoAnalysisSseManager;
import com.cloudbread.domain.photo_analyses.application.image.StorageClient;
import com.cloudbread.domain.photo_analyses.domain.entity.PhotoAnalysis;
import com.cloudbread.domain.photo_analyses.domain.enums.PhotoAnalysisStatus;
import com.cloudbread.domain.photo_analyses.domain.repository.PhotoAnalysisRepository;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisRequest;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.cloudbread.domain.user.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PhotoAnalysisServiceImpl implements PhotoAnalysisService {
    private final PhotoAnalysisRepository photoAnalysisRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final FoodNutrientRepository foodNutrientRepository;
    private final StorageClient storageClient;
    private final WebClient aiFoodClient;

    private final PhotoAnalysisSseManager sse;
    private final CandidateFinder candidateFinder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.food.enabled:false}")
    private boolean aiEnabled; // ai 호출 여부

    @Override
    public PhotoAnalysisResponse.UploadResponse upload(Long userId, MultipartFile file) throws Exception{
        // 1. photoanalysis 저장 (uploaded, user 매핑)
        PhotoAnalysis pa = PhotoAnalysis.createPhotoAnalysisSetStatus(null, null, null, null,
                PhotoAnalysisStatus.UPLOADED);
        pa.setPhotoUser(userRepository.findById(userId).orElseThrow(UserNotFoundException::new));
        pa = photoAnalysisRepository.save(pa);

        // 2. 파일 저장 -> URL 세팅
        String objectKey = "u/" + userId + "/photos/" + pa.getId() + guessExt(file.getOriginalFilename());
        String imageUrl  = storageClient.upload(objectKey, file.getContentType(), file.getInputStream(), file.getSize());
        pa.setImageUrl(imageUrl); // imageUrl 세팅

        // 3) 커밋 후 AI 트리거 (비동기)
        // JPA 트랜잭션 정상 커밋된 직후에, 콜백 실행
        final Long idForTrigger = pa.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                if (!aiEnabled) {
                    log.info("[AI-TRIGGER] disabled. skip. id={}, imageUrl={}", idForTrigger, imageUrl);
                    return;
                }

                Map<String,Object> body = Map.of("photoAnalysisId", idForTrigger, "imageUrl", imageUrl);

                // WebClient.. .subscribe()로 비동기 호출, 업로드 api는 바로 201 응답을 보내고, ai 호출은 별도의 스레드에서 진행
                aiFoodClient.post().uri("/v1/photo-label") // WebClient로 post 요청
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body)// body 변수에 지정된 photoAnalysisId, 배포된 imageUrl을 보낸다

                        .retrieve()
                        .bodyToMono(String.class)
                        // 요청을 실행하고 응답 받기, 아직 실제 요청 x

                        .doOnError(e -> log.error("[AI-TRIGGER] id={} failed: {}", idForTrigger, e.toString()))
                        // 응답 실패 시, 오류 로그 남기기

                        .subscribe(res -> log.info("[AI-TRIGGER] id={} ok: {}", idForTrigger, res));
                        // 실제 요청 발생, subscribe는 논블로킹 방식으로, 비동기로 작동한다.
                        // 즉, 요청을 보낸 스레드는 AI API 응답을 기다리지 않고 즉시 다음 작업 실행
                        // 응답이 오면, 응답결과를 log로 res로 출력
            }
        });

        return new PhotoAnalysisResponse.UploadResponse(pa.getId(), imageUrl, pa.getPhotoAnalysisStatus());

    }

    /*
        AI 라벨 수신 -> ES 후보 3개 생성 -> DB 저장 -> SSE 푸시
     */
    @Override
    public void handleAiLabel(Long photoAnalysisId, PhotoAnalysisRequest.AiLabelRequest request) throws Exception {
        var pa = photoAnalysisRepository.findById(photoAnalysisId)
                .orElseThrow(() -> new IllegalArgumentException("photoAnalysis not found: " + photoAnalysisId));

        // 1) 상태 : labeled
        pa.updateLabeled(request.getLabel(), request.getConfidence());
        pa.updatePhotoAnalysisStatus(PhotoAnalysisStatus.LABELED);
        sse.sendStatus(photoAnalysisId, PhotoAnalysisStatus.LABELED); // 구독한 프론트에게 LABELDED를 보내주세요!

        // 2) 후보 생성 (ES -> 후에 교체 할 예정, 지금은 JPA Fallback)
        var items = candidateFinder.find(request.getLabel(), 3);

        var nextStatus = items.isEmpty() ? PhotoAnalysisStatus.NO_CANDIDATES
                : PhotoAnalysisStatus.CANDIDATES_READY;

        // 3) payload 구성 & JSON 저장
        var payload = PhotoAnalysisResponse.CandidatesPayload.builder()
                .photoAnalysisId(photoAnalysisId)
                .query(request.getLabel())
                .status(nextStatus.name())
                .candidates(items) // 빈 리스트여도 그대로 넣음
                .build();

        // 4) DB 반영 + SSE 발행
        pa.updateCandidates(objectMapper.writeValueAsString(payload), nextStatus);
        sse.sendStatus(photoAnalysisId, nextStatus);
        sse.sendCandidates(photoAnalysisId, payload);


    }

    @Override
    public PhotoAnalysisResponse.ConfirmResponse confirm(Long userId, Long photoAnalysisId, Long selectedFoodId) {
        var pa = photoAnalysisRepository.findById(photoAnalysisId)
                .orElseThrow(() -> new IllegalArgumentException("photoAnalysis not found: " + photoAnalysisId));

        // 1. 음식 확정 반영
        var food = foodRepository.findById(selectedFoodId)
                .orElseThrow(() -> new IllegalArgumentException("food not found: " + selectedFoodId));
        pa.setSelectedFood(food);
        pa.updatePhotoAnalysisStatus(PhotoAnalysisStatus.USER_CONFIRMED);

        // 2. 영양 상세 구성 (Calories 분리)
        Map<String, PhotoAnalysisResponse.NutrientValue> nutrients = nutrientsWithoutCalories(food);
        BigDecimal calories = food.getCalories();

        var selected = PhotoAnalysisResponse.ConfirmSelected.builder()
                .foodId(food.getId())
                .name(food.getName())
                .category(food.getCategory())
                .serving(food.getSourceName())
                .calories(calories)      // 별도 필드
                .nutrients(nutrients)    // 칼로리는 제외
                .build();

        return PhotoAnalysisResponse.ConfirmResponse.builder()
                .photoAnalysisId(photoAnalysisId)
                .status(PhotoAnalysisStatus.USER_CONFIRMED.name())
                .selected(selected)
                .build();
    }

    private String guessExt(String filename) {
        if (filename == null) return ".jpg";
        String f = filename.toLowerCase();
        if (f.endsWith(".png")) return ".png";
        if (f.endsWith(".webp")) return ".webp";
        if (f.endsWith(".jpeg")) return ".jpeg";
        return ".jpg";
    }

    private Map<String, PhotoAnalysisResponse.NutrientValue> nutrientsWithoutCalories(Food food) {
        List<FoodNutrient> list = foodNutrientRepository.findByFoodId(food.getId());

        Map<String, PhotoAnalysisResponse.NutrientValue> map = new LinkedHashMap<>();

        for (FoodNutrient fn : list) {
            String key  = normalizeKey(fn.getNutrient().getName()); // 예: "carbs"

            if ("calories".equals(key)) continue; // 칼로리는 제외 (별도 필드)

            String unit = unitSymbol(fn.getNutrient().getUnit());   // "g" | "mg" | "μg"

            map.put(key, PhotoAnalysisResponse.NutrientValue.builder()
                    .value(fn.getValue())
                    .unit(unit)
                    .build());
        }
        return map;
    }

    private String normalizeKey(String s) { return s == null ? null : s.toLowerCase(); }
    private String unitSymbol(Unit u) { return (u == null) ? null : u.name().toLowerCase(); }
}
