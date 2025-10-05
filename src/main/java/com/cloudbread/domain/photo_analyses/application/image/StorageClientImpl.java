package com.cloudbread.domain.photo_analyses.application.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class StorageClientImpl implements StorageClient {
    @Value("${storage.local.base-dir:./uploads}")
    private String baseDir; // 현재 디렉터리 아래에 uploads 폴더

    @Value("${storage.local.public-base-url:http://localhost:8080/uploads}")
    private String publicBaseUrl;
    @Override
    public String upload(String objectKey, String contestType, InputStream in, long size) throws Exception {
        Path path = Paths.get(baseDir, objectKey).normalize(); // baseDir 경로와 파일명을 합쳐, 최종 저장경로
        // ./uploads/u/{userId}/photos/{id}.jpg 로 이미지 url 세팅
        Files.createDirectories(path.getParent()); // 파일이 저장될 상위 디렉터리가 없으면 자동생성

        try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            in.transferTo(out); // 입력 스트림(in)의 내용을, 지정된 경로의 파일에 write
        }

        return publicBaseUrl + "/" + objectKey.replace("\\", "/"); // 외부 접근 가능한, 로컬 파일 경로
    }
}
