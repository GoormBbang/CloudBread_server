package com.cloudbread.domain.crawling.api;

import com.cloudbread.domain.crawling.application.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlingService crawlingService;

    @GetMapping("/crawl")
    public ResponseEntity<Map<String, String>> crawl() {
        crawlingService.fetchContent();
        return ResponseEntity.ok(Map.of("message", "크롤링 완료!"));
    }

}
