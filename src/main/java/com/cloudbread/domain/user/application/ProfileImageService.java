package com.cloudbread.domain.user.application;

import com.cloudbread.domain.photo_analyses.application.image.StorageClient;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.cloudbread.domain.user.exception.FileInvalidException;
import com.cloudbread.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileImageService {

    private final UserRepository userRepository;
    private final StorageClient storageClient;

    @Value("${storage.local.public-base-url}")
    private String publicBaseUrl; // http://cloudbread.133.186.213.185.nip.io/uploads

    private static final Set<String> ALLOWED_EXT = Set.of("jpg","jpeg","png","webp","gif");

    public String updateProfileImage(Long userId, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new FileInvalidException();
        String ct = (file.getContentType() == null) ? "" : file.getContentType().toLowerCase();
        if (!ct.startsWith("image/")) throw new FileInvalidException();

        String ext = extOrDefault(file.getOriginalFilename());
        String filename = Instant.now().toEpochMilli() + "_" + UUID.randomUUID() + "." + ext;

        String objectKey = "u/" + userId + "/profile/" + filename; // 저장 경로(상대)
        try (InputStream in = file.getInputStream()) {
            // StorageClient는 내부적으로 base-dir(/data/uploads)에 파일 저장 후 공개 URL을 반환하거나,
            // 여기서는 키 기준으로 URL을 직접 조합해도 됨.
            storageClient.upload(objectKey, file.getContentType(), in, file.getSize());
        }

        // 공개 URL: public-base-url + "/" + objectKey
        String publicUrl = stripTrailingSlash(publicBaseUrl) + "/" + objectKey;

        // 유저에 반영
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.updateProfileImageUrl(publicUrl);


        return publicUrl;
    }

    private String extOrDefault(String filename) {
        String e = FilenameUtils.getExtension(filename == null ? "" : filename).toLowerCase();
        if (!ALLOWED_EXT.contains(e)) return "jpg";
        return e;
    }
    private String stripTrailingSlash(String s) { return s == null ? "" : s.replaceAll("/+$", ""); }
}
