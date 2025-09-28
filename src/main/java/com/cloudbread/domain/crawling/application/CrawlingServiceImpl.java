package com.cloudbread.domain.crawling.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlingServiceImpl implements CrawlingService {

    // 공통적으로 제외해야 할 키워드
    private static final List<String> EXCLUDED_KEYWORDS = List.of(
            "Aptaclub",
            "뉴트리시아 전문영양사 케어라인",
            "자세히 알아보기",
            "문의사항이 있으면 언제든지 연락"
    );

    // 중복 제거용 Set
    private final Set<String> seenTexts = new HashSet<>();

    @Override
    public void fetchContent() {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://nutriciastore.co.kr/content/pregnancy_week.php?contentNo=2");
            log.info("==== 임신 주차별 맞춤정보 크롤링 시작 ====");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            for (int week = 1; week <= 40; week++) {
                try {
                    if (week <= 15) {
                        // 1~15주차는 인덱스로 직접 클릭
                        int index = (week <= 4) ? 0 : week - 4;
                        WebElement weekButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("a[data-slick-index='" + index + "']")));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", weekButton);

                    } else {
                        // 16주차 이후는 > 버튼 클릭
                        WebElement nextArrow = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("button.slick-next")));
                        nextArrow.click();
                        Thread.sleep(800);

                        WebElement activeWeek = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("a.item.slick-current.slick-active")));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", activeWeek);
                    }

                    Thread.sleep(1500);
                    log.info("===== [{}주차] =====", week);

                    // 본문 크롤링
                    List<WebElement> sectionBlocks = driver.findElements(By.cssSelector("div.component_group"));
                    for (WebElement block : sectionBlocks) {
                        // 일반 제목
                        List<WebElement> titles = block.findElements(By.cssSelector("h3.title, h4.subtitle"));
                        for (WebElement title : titles) {
                            logUnique("[제목] " + cleanText(title.getText()));
                        }

                        // 일반 본문
                        List<WebElement> contents = block.findElements(By.cssSelector("div.contents, div.content"));
                        for (WebElement content : contents) {
                            logUnique("[본문] " + cleanText(content.getText()));
                        }

                        // 토글(accordion) 처리
                        List<WebElement> toggleItems = block.findElements(By.cssSelector("div.accordion .item"));
                        for (WebElement item : toggleItems) {
                            try {
                                WebElement title = item.findElement(By.cssSelector("h6.title"));
                                String titleText = cleanText(title.getText());

                                // 참고자료 토글 제외
                                if (titleText.contains("참고자료")) {
                                    continue;
                                }

                                logUnique("[토글 제목] " + titleText);

                                // 클릭해서 열기
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", title);
                                Thread.sleep(500);

                                // 토글 본문
                                List<WebElement> toggleContents = item.findElements(By.cssSelector("div.content"));
                                for (WebElement tContent : toggleContents) {
                                    logUnique("[토글 본문] " + cleanText(tContent.getText()));
                                }

                            } catch (Exception e) {
                                log.warn("토글 크롤링 실패", e);
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("{}주차 크롤링 실패", week, e);
                }
            }

        } catch (Exception e) {
            log.error("크롤링 전체 실패", e);
        } finally {
            driver.quit();
        }
    }

    // 중복 제거 후 로깅
    private void logUnique(String text) {
        if (text == null || text.isBlank()) return;
        if (seenTexts.add(text)) { // 새 텍스트만 로깅
            log.info("{}", text);
        }
    }

    // 텍스트 클린업
    private String cleanText(String text) {
        if (text == null) return "";

        // 공통 문구 제거
        for (String keyword : EXCLUDED_KEYWORDS) {
            if (text.contains(keyword)) {
                return "";
            }
        }

        // 주석 숫자(각주만) 제거
        return text
                .replaceAll("\\.(?=\\d+\\s|\\d+$)", "")   // ".8" 같은 패턴 제거
                .replaceAll("(?<=\\S)\\d+\\)", "")       // "10)" 제거
                .replaceAll("[¹²³⁴⁵⁶⁷⁸⁹⁰]", "")          // 위첨자 제거
                .trim();
    }
}
