package com.cloudbread.domain.photo_analyses.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

// 업로드된 파일이 외부에서 접근 가능하도록
// 예) http://localhost:8080/uploads/my-file.jpg 같은 요청이 들어오면,
// 어플리케이션은 서버의 ./uploads/my-file.jpg 파일을 찾아, 응답으로 보냄
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Value("${storage.local.base-dir:./uploads}")
    private String baseDir;
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String loc = Path.of(baseDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(loc)
                .setCachePeriod(31536000);
    }
}
