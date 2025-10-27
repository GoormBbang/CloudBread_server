package com.cloudbread.domain.photo_analyses.application.image;

import java.io.InputStream;

public interface StorageClient {
    /** 업로드 한후, 외부에서 접근 가능한 URL로 반환 */
    String upload(String objectKey, String contentType, InputStream in, long size) throws Exception;
}
