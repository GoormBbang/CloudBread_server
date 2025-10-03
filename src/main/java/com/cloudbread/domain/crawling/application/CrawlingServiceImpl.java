package com.cloudbread.domain.crawling.application;

import com.cloudbread.global.util.ExcelWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlingServiceImpl implements CrawlingService {

    @Override
    public void fetchContent() {
        WebDriver driver = new ChromeDriver();
        List<String[]> rows = new ArrayList<>(); // ✅ Excel에 넣을 데이터 누적
        List<String[]> firstWeekRows = new ArrayList<>(); // ✅ 1주차 데이터를 저장해뒀다가 2~4주차에 복제

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Set<String> seenNormalizedTexts = new HashSet<>(); // 중복 제거용 Set

            for (int week = 1; week <= 40; week++) {
                try {
                    if (week <= 4) {
                        if (week == 1) {
                            // 👉 1주차는 실제 크롤링
                            driver.get("https://nutriciastore.co.kr/content/pregnancy_week.php?contentNo=2");
                            Thread.sleep(1500);
                            log.info("{}주차", week);

                            // === 크롤링 처리 ===
                            List<WebElement> sectionBlocks = driver.findElements(By.cssSelector("div.component_group"));
                            for (WebElement block : sectionBlocks) {
                                String titleText = "";
                                List<WebElement> titles = block.findElements(By.cssSelector("h3.title"));
                                for (WebElement title : titles) {
                                    titleText = cleanText(title.getText());
                                    String key = normalizeForDedup(titleText);
                                    if (!isExcluded(title, titleText) && !titleText.isEmpty() && seenNormalizedTexts.add(key)) {
                                        log.info("제목: {}", titleText);
                                    }
                                }

                                StringBuilder descriptionBuilder = new StringBuilder();

                                // 본문
                                List<WebElement> contents = block.findElements(By.cssSelector("div.contents div.content p"));
                                for (WebElement p : contents) {
                                    String pText = cleanText(p.getText());
                                    if (!isExcluded(p, pText) && !pText.isEmpty()) {
                                        if (descriptionBuilder.length() > 0) descriptionBuilder.append("\n");
                                        descriptionBuilder.append(pText);
                                    }
                                }

                                // 토글
                                List<WebElement> toggles = block.findElements(By.cssSelector("div.accordion div.item"));
                                for (WebElement toggle : toggles) {
                                    try {
                                        WebElement toggleTitle = toggle.findElement(By.cssSelector("h6.title"));
                                        String toggleTitleText = cleanText(toggleTitle.getText());

                                        if (toggleTitleText.contains("참고자료 보기")) continue;

                                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggleTitle);
                                        Thread.sleep(500);

                                        if (descriptionBuilder.length() > 0) descriptionBuilder.append("\n\n");
                                        descriptionBuilder.append(toggleTitleText).append("\n");

                                        List<WebElement> toggleContents = toggle.findElements(By.cssSelector("div.content"));
                                        for (WebElement toggleContent : toggleContents) {
                                            String text = cleanText(toggleContent.getText());
                                            if (!isExcluded(toggleContent, text) && !text.isEmpty()) {
                                                descriptionBuilder.append(text).append("\n");
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                }

                                if (!titleText.isEmpty() || descriptionBuilder.length() > 0) {
                                    log.info("본문+토글: {}", descriptionBuilder.toString()); // ✅ 로그로 확인
                                    String[] row = new String[]{
                                            String.valueOf(week),
                                            "", // category
                                            titleText,
                                            descriptionBuilder.toString().trim()
                                    };
                                    rows.add(row);
                                    firstWeekRows.add(row); // ✅ 1주차 데이터 백업
                                }
                            }
                        } else {
                            // 👉 2~4주차는 1주차 데이터 복제
                            for (String[] row : firstWeekRows) {
                                rows.add(new String[]{
                                        String.valueOf(week), // 주차만 변경
                                        row[1],
                                        row[2],
                                        row[3]
                                });
                            }
                            log.info("{}주차 데이터는 1주차 데이터 복제 완료", week);
                        }
                        continue;
                    }

                    // 👉 5주차 이후부터는 정상 크롤링
                    WebElement nextArrow = wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button.slick-next")));
                    nextArrow.click();
                    Thread.sleep(800);

                    WebElement activeWeek = wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("a.item.slick-current.slick-active")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", activeWeek);

                    Thread.sleep(1500);
                    log.info("{}주차", week);

                    // === 5주차 이후도 동일 처리 ===
                    List<WebElement> sectionBlocks = driver.findElements(By.cssSelector("div.component_group"));
                    for (WebElement block : sectionBlocks) {
                        String titleText = "";
                        List<WebElement> titles = block.findElements(By.cssSelector("h3.title"));
                        for (WebElement title : titles) {
                            titleText = cleanText(title.getText());
                            String key = normalizeForDedup(titleText);
                            if (!isExcluded(title, titleText) && !titleText.isEmpty() && seenNormalizedTexts.add(key)) {
                                log.info("제목: {}", titleText);
                            }
                        }

                        StringBuilder descriptionBuilder = new StringBuilder();

                        // 본문
                        List<WebElement> contents = block.findElements(By.cssSelector("div.contents div.content p"));
                        for (WebElement p : contents) {
                            String pText = cleanText(p.getText());
                            if (!isExcluded(p, pText) && !pText.isEmpty()) {
                                if (descriptionBuilder.length() > 0) descriptionBuilder.append("\n");
                                descriptionBuilder.append(pText);
                            }
                        }

                        // 토글
                        List<WebElement> toggles = block.findElements(By.cssSelector("div.accordion div.item"));
                        for (WebElement toggle : toggles) {
                            try {
                                WebElement toggleTitle = toggle.findElement(By.cssSelector("h6.title"));
                                String toggleTitleText = cleanText(toggleTitle.getText());

                                if (toggleTitleText.contains("참고자료 보기")) continue;

                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggleTitle);
                                Thread.sleep(500);

                                if (descriptionBuilder.length() > 0) descriptionBuilder.append("\n\n");
                                descriptionBuilder.append(toggleTitleText).append("\n");

                                List<WebElement> toggleContents = toggle.findElements(By.cssSelector("div.content"));
                                for (WebElement toggleContent : toggleContents) {
                                    String text = cleanText(toggleContent.getText());
                                    if (!isExcluded(toggleContent, text) && !text.isEmpty()) {
                                        descriptionBuilder.append(text).append("\n");
                                    }
                                }
                            } catch (Exception ignored) {}
                        }

                        if (!titleText.isEmpty() || descriptionBuilder.length() > 0) {
                            log.info("본문+토글: {}", descriptionBuilder.toString()); // ✅ 로그로 확인
                            rows.add(new String[]{
                                    String.valueOf(week),
                                    "",
                                    titleText,
                                    descriptionBuilder.toString().trim()
                            });
                        }
                    }

                } catch (Exception e) {
                    log.error("{}주차 크롤링 실패", week, e);
                }
            }

            // Excel 파일로 저장
            String filePath = "src/main/resources/export/tips.xlsx";
            ExcelWriter.writeTipsToExcel(filePath, rows);
            log.info("크롤링 데이터 Excel 저장 완료: {}", filePath);

        } catch (Exception e) {
            log.error("크롤링 전체 실패", e);
        } finally {
            driver.quit();
        }
    }

    /** 텍스트 정리 */
    private String cleanText(String text) {
        if (text == null) return "";
        return text
                .replaceAll("[\\u00B2\\u00B3\\u00B9\\u2070-\\u209F]", "")
                .replaceAll("\\d+(?=\\))", "")
                .replaceAll("(?<=\\D)\\d+(?=\\s*$)", "")
                .replaceAll("(?<=\\.)\\d+", "")
                .replace("자세히 알아보기", "")
                .replace("APTACLUB 가입하기", "")
                .replace("Aptaclub", "")
                .replaceAll("[\\t\\n\\r]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /** 중복 방지용 정규화 */
    private String normalizeForDedup(String text) {
        return text == null ? "" : text.toLowerCase().replaceAll("[^가-힣a-z0-9]", "");
    }

    /** 특정 문장 차단 */
    private boolean isExcluded(WebElement element, String text) {
        if (text == null || text.isEmpty()) return true;

        String normalized = text.trim();
        if (normalized.contains("출산 후에 새로운 것을 접하는 경험이 아기의 성장 발달에 있어 회복력을 기르는데 도움이 된다고 믿습니다")) {
            return true;
        }
        if (normalized.contains("뉴트리시아 전문영양사 케어라인은 영유아 및 임산부, 수유부의 영양에 대해 여러분의 고민을 함께함으로써 부모님과 아기가 내일을 준비할 수 있도록 도와드리겠습니다")) {
            return true;
        }
        if (normalized.contains("임신 및 영유아 영양에 대해 궁금한 점이 있으신가요?")) {
            return true;
        }
        return false;
    }
}