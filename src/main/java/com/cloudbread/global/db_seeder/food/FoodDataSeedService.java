package com.cloudbread.global.db_seeder.food;

import com.cloudbread.domain.food.domain.entity.*;
import com.cloudbread.domain.food.domain.enums.Unit;
import com.cloudbread.domain.food.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ooxml.util.SAXHelper;                     // ✅ 핵심
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FoodDataSeedService {

    private final FoodRepository foodRepository;
    private final NutrientRepository nutrientRepository;
    private final FoodNutrientRepository foodNutrientRepository;

    @PersistenceContext
    private EntityManager em;

    // ⬇️ StopParsing 던져도 롤백하지 않도록 명시
    @Transactional(noRollbackFor = StopParsing.class)
    public FoodSeedDto.ImportResultDto importFromFile(InputStream in, String filename) throws Exception {
        resetTables(ResetMode.ALL);

        final List<String> warnings = new ArrayList<>();
        final int[] totalRows = {0}, foodsUpserted = {0}, nutrientsLinked = {0};

        try (OPCPackage pkg = OPCPackage.open(in)) {
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            SharedStrings sst = reader.getSharedStringsTable();
            DataFormatter formatter = new DataFormatter();

            // ✅ 모든 시트를 순회하면서, 시트마다 매번 새 parser 생성 (중요)
            XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) reader.getSheetsData();
            int sheetIdx = 0;
            while (it.hasNext()) {
                try (InputStream sheet = it.next()) {
                    sheetIdx++;
                    log.info("[FoodSeed] Parsing sheet {} - {}", sheetIdx, it.getSheetName());

                    XMLReader parser = SAXHelper.newXMLReader(); // ✅ 핵심: SAXHelper 사용

                    XSSFSheetXMLHandler.SheetContentsHandler sheetHandler =
                            new SimpleSheetHandler(row -> {
                                totalRows[0]++;
                                if (totalRows[0] <= 3) log.info("[Row{}] {}", totalRows[0], row);

                                // ---- 값 읽기 (헤더 키는 트림/은닉문자 제거로 정규화됨) ----
                                String externalId = row.get("식품코드");
                                if (externalId == null || externalId.isBlank()) {
                                    warnings.add("행 " + totalRows[0] + ": 식품코드 누락 → 스킵");
                                    return;
                                }
                                String name       = row.get("식품명");
                                String category   = row.get("식품대분류명");
                                BigDecimal calories = parseDecimal(row.get("에너지(kcal)"));
                                String sourceName = row.get("영양성분함량기준량");

                                // ---- Food upsert ----
                                Food food = foodRepository.findByExternalId(externalId)
                                        .orElseGet(() -> new Food(name, null, sourceName, externalId, calories, category));
                                if (food.getId() == null) {
                                    foodRepository.save(food);
                                    foodsUpserted[0]++;
                                } else if (food.merge(name, sourceName, calories, category)) {
                                    foodsUpserted[0]++;
                                }

                                // ---- Nutrients ----
                                Map<String, String> nutrientHeaderToName = Map.ofEntries(
                                        Map.entry("탄수화물(g)", "CARBS"),
                                        Map.entry("단백질(g)",   "PROTEINS"),
                                        Map.entry("지방(g)",     "FATS"),
                                        Map.entry("당류(g)",     "SUGARS"),
                                        Map.entry("포화지방산(g)","SATURATED_FAT"),
                                        Map.entry("트랜스지방산(g)","TRANS_FAT"),
                                        Map.entry("콜레스테롤(mg)","CHOLESTEROL"),
                                        Map.entry("나트륨(mg)",  "SODIUM"),
                                        Map.entry("엽산(μg DFE)","FOLIC_ACID"),
                                        Map.entry("철(mg)",      "IRON"),
                                        Map.entry("칼슘(mg)",    "CALCIUM"),
                                        Map.entry("수분(g)",     "MOISTURE")
                                );

                                for (String header : nutrientHeaderToName.keySet()) {
                                    String valStr = row.get(header);
                                    if (valStr == null || valStr.isBlank()) continue;

                                    BigDecimal val = parseDecimal(valStr);
                                    String nutrientName = nutrientHeaderToName.get(header);
                                    Unit unit = unitFromHeader(header);

                                    Nutrient nutrient = nutrientRepository.findByName(nutrientName)
                                            .orElseGet(() -> nutrientRepository.save(new Nutrient(nutrientName, null, unit)));

                                    foodNutrientRepository.findByFoodIdAndNutrientId(food.getId(), nutrient.getId())
                                            .map(fn -> {
                                                if (fn.updateValueIfChanged(val)) nutrientsLinked[0]++;
                                                return fn;
                                            })
                                            .orElseGet(() -> {
                                                foodNutrientRepository.save(new FoodNutrient(food, nutrient, val));
                                                nutrientsLinked[0]++;
                                                return null;
                                            });
                                }

                                if (totalRows[0] % 500 == 0) { em.flush(); em.clear(); }

                                // ✅ 여기 추가: 10행 이상이면 중단
//                                if (totalRows[0] >= 10) {
//                                    log.info("⚠️ 테스트 모드: 10행까지만 처리 후 중단");
//                                    throw new StopParsing(); // 밑에 RuntimeException 하나 선언
//                                }
                            });

                    parser.setContentHandler(new XSSFSheetXMLHandler(
                            styles, (CommentsTable) null, sst, sheetHandler, formatter, false
                    ));

                    try {
                        parser.parse(new InputSource(sheet));
                    } catch (StopParsing e) {
                        // ✅ 여기서 잡아주면 트랜잭션이 롤백되지 않는다
                        log.info("Parsing stopped early after {} rows (테스트 모드).", totalRows[0]);
                        break; // 다른 시트도 읽지 말고 종료
                    }
                }
            }
        }

        // 마지막 잔여 영속성 컨텍스트 비우기
        em.flush(); em.clear();

        log.info("[FoodSeed] Done. rows={}, foodsUpserted={}, nutrientsLinked={}, warnings={}",
                totalRows[0], foodsUpserted[0], nutrientsLinked[0], warnings.size());

        return FoodSeedDto.ImportResultDto.builder()
                .totalRows(totalRows[0])
                .foodsUpserted(foodsUpserted[0])
                .nutrientsLinked(nutrientsLinked[0])
                .warnings(warnings)
                .build();
    }

    private void resetTables(ResetMode mode) {
        if (mode == ResetMode.NONE) return;
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE food_nutrients").executeUpdate();
        if (mode == ResetMode.ALL) em.createNativeQuery("TRUNCATE TABLE nutrients").executeUpdate();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
    }

    private BigDecimal parseDecimal(String s) {
        try { return (s == null || s.isBlank()) ? null :
                new BigDecimal(s.replace(",", "").replace("%", "").trim()); }
        catch (Exception e) { return null; }
    }

    private Unit unitFromHeader(String header) {
        String x = header.replace("µ","μ").toLowerCase();
        if (x.contains("μg") || x.contains("ug")) return Unit.μg;
        if (x.contains("mg")) return Unit.mg;
        return Unit.g;
    }

    /** ===== Sheet 핸들러: 1행(첫 비어있지 않은 행)을 헤더로 사용, 헤더/값 모두 정규화 ===== */
    private static class SimpleSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final RowCallback callback;
        private final List<String> headers = new ArrayList<>();
        private final List<String> cells   = new ArrayList<>();

        SimpleSheetHandler(RowCallback callback) { this.callback = callback; }

        @Override public void startRow(int rowNum) { cells.clear(); }

        @Override public void endRow(int rowNum) {
            // 행 로그
            log.debug("endRow rowNum={}, cells={}", rowNum, cells);

            // 첫 번째 비어있지 않은 행을 헤더로 간주
            if (headers.isEmpty()) {
                if (isAllBlank(cells)) return; // 완전 빈 줄은 스킵
                headers.clear();
                for (String h : cells) headers.add(sanitize(h));
                log.info("Header detected: {}", headers);
                return;
            }

            if (isAllBlank(cells)) return;

            Map<String,String> row = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(headers.size(), cells.size()); i++) {
                row.put(headers.get(i), sanitize(cells.get(i)));
            }
            log.debug("Row mapped: {}", row);
            if (!row.isEmpty()) callback.handle(row);
        }

        @Override public void cell(String cellRef, String value, XSSFComment comment) {
            int col = CellReference.convertColStringToIndex(cellRef.replaceAll("\\d",""));
            while (cells.size() <= col) cells.add("");
            cells.set(col, value == null ? "" : value);
        }

        private static boolean isAllBlank(List<String> list) {
            for (String s : list) if (s != null && !s.trim().isEmpty()) return false;
            return true;
        }

        private static String sanitize(String s) {
            if (s == null) return "";
            // 트림 + 제로-위드스페이스 제거
            return s.replace("\u200B","").replace("\uFEFF","").trim();
        }
    }

    @FunctionalInterface
    interface RowCallback { void handle(Map<String, String> row); }
}

// ✅ StopParsing 예외 클래스 선언
class StopParsing extends RuntimeException {}