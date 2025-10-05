package com.cloudbread.domain.photo_analyses.application;

import com.cloudbread.domain.photo_analyses.application.image.StorageClient;
import com.cloudbread.domain.photo_analyses.domain.entity.PhotoAnalysis;
import com.cloudbread.domain.photo_analyses.domain.enums.PhotoAnalysisStatus;
import com.cloudbread.domain.photo_analyses.domain.repository.PhotoAnalysisRepository;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.cloudbread.domain.user.exception.UserNotFoundException;
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

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PhotoAnalysisServiceImpl implements PhotoAnalysisService {
    private final PhotoAnalysisRepository photoAnalysisRepository;
    private final UserRepository userRepository;
    private final StorageClient storageClient;
    private final WebClient aiFoodClient;

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

    private String guessExt(String filename) {
        if (filename == null) return ".jpg";
        String f = filename.toLowerCase();
        if (f.endsWith(".png")) return ".png";
        if (f.endsWith(".webp")) return ".webp";
        if (f.endsWith(".jpeg")) return ".jpeg";
        return ".jpg";
    }
}
