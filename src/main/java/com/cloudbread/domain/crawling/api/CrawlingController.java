package com.cloudbread.domain.crawling.api;

import com.cloudbread.domain.crawling.application.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlingService crawlingService;

    @GetMapping("/crawl")
    public String crawl() {
        crawlingService.fetchContent();
        return "크롤링 완료!";
    }
}
