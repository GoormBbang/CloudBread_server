package com.cloudbread.global.db_seeder.food;

import com.cloudbread.domain.food.domain.Food;
import com.cloudbread.domain.food.domain.FoodNutrient;
import com.cloudbread.domain.food.domain.Nutrient;
import com.cloudbread.domain.food.enums.NutrientType;
import com.cloudbread.domain.food.enums.Unit;
import com.cloudbread.domain.food.repository.FoodNutrientRepository;
import com.cloudbread.domain.food.repository.FoodRepository;
import com.cloudbread.domain.food.repository.NutrientRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

// 실제 DB 저장 로직, bulk insert (성능을 위해)
/*
   - 엑셀표의 헤더는 한글 컬럼명 그대로 사용
   - 단위는 헤더 괄호에서 자동 추출 (g, mg, μg). μ(마이크로) 문자는 µ/μ/ug 다양한 표기가 있어 전부 정규화.
   - Nutrient가 없으면 자동 생성

 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FoodDataSeedService {
    private final FoodRepository foodRepository;
    private final NutrientRepository nutrientRepository;
    private final FoodNutrientRepository foodNutrientRepository;

    /**
     * MySQL 전용: FK 체크 끄고 필요한 테이블을 TRUNCATE 한 뒤 다시 켠다.
     * TRUNCATE는 DDL이라 autocommit이 일어나니 예외가 나든 말든 FK는 반드시 다시 켜지도록 try/finally 처리.
     */
    private void resetTables(ResetMode mode) {
        if (mode == ResetMode.NONE) return;

        log.warn("[FoodSeed] Reset tables start. Mode = {}", mode);
        try {
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();

            // 참조 테이블부터
            em.createNativeQuery("TRUNCATE TABLE food_nutrients").executeUpdate();

            if (mode == ResetMode.ALL) {
                em.createNativeQuery("TRUNCATE TABLE nutrients").executeUpdate();
            }
        } finally {
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
            log.warn("[FoodSeed] Reset tables done.");
        }
    }

    /**
     * resetMode 파라미터로 리셋 범위를 결정.
     *  - 개발 중 재시드: ResetMode.ALL
     *  - 운영(마스터 고정): ResetMode.FOOD_LINKS
     */
    public FoodSeedDto.ImportResultDto importFromFile(InputStream in, String filename) throws Exception {
        // 0) 시작 전에 선택적으로 리셋
        resetTables(ResetMode.ALL);

        List<String> warnings = new ArrayList<>();
        int totalRows = 0, foodsUpserted = 0, nutrientsLinked = 0;

        DataFormatter fmt = new DataFormatter();


        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sh = wb.getSheetAt(0);
            if (sh.getPhysicalNumberOfRows() < 2) {
                return FoodSeedDto.ImportResultDto.builder()
                        .totalRows(0).foodsUpserted(0).nutrientsLinked(0)
                        .warnings(List.of("엑셀에 데이터 행이 없습니다.")).build();
            }

            // === 1) 헤더 인덱스 맵 ===
            // 첫번째 행(헤더)의 모든 셀을 순회하며, 헤더 이름(식품코드, 대표식품명 등)과 해당 셀의 인덱스 번호(열번호)를 col 맵에 저장
            // 예) : col.put("식품코드", 0)
            Row header = sh.getRow(0);
            Map<String, Integer> col = new HashMap<>();
            for (Cell c : header) {
                String h = fmt.formatCellValue(c).trim();
                col.put(h, c.getColumnIndex());
                log.info("헤더 : {}, trim 제거한 헤더 : {}, 저장된 col :: {} {}", c, col, h, col.get(h));
            }

            // 필수 컬럼 확인
            Integer cFoodCode   = col.get("식품코드");         // external_id
            Integer cRepName    = col.get("대표식품명");       // name
            Integer cKcal       = col.get("에너지(kcal)");     // calories
            // 사용자가 원한 매핑: sourceName <- "영양성분함량기준량"
            Integer cSourceName = col.get("영양성분함량기준량");

            if (cFoodCode == null || cRepName == null || cKcal == null) {
                throw new IllegalArgumentException("필수 컬럼(식품코드/대표식품명/에너지(kcal))을 찾을 수 없습니다.");
            }

            // 영양소 헤더 → 시스템 Nutrient 이름 과 매핑 (NutrientType.CARBS과 같이 nutrients.name 칼럼에 들어갈 예정)
            Map<String, String> nutrientHeaderToName = Map.ofEntries(
                    Map.entry("탄수화물(g)",    "CARBS"),
                    Map.entry("단백질(g)",      "PROTEINS"),
                    Map.entry("지방(g)",        "FATS"),
                    Map.entry("당류(g)",        "SUGARS"),
                    Map.entry("포화지방산(g)",  "SATURATED_FAT"),
                    Map.entry("트랜스지방산(g)","TRANS_FAT"),
                    Map.entry("콜레스테롤(mg)","CHOLESTEROL"),
                    Map.entry("나트륨(mg)",     "SODIUM"),
                    Map.entry("엽산(μg DFE)",   "FOLIC_ACID"),
                    Map.entry("철(mg)",         "IRON"),
                    Map.entry("칼슘(mg)",       "CALCIUM"),
                    Map.entry("수분(g)",        "MOISTURE")
            );

            // 헤더 인덱스 + 단위 추출
            record ColInfo(Integer idx, Unit unit) {}
            Map<String, ColInfo> nutrientCols = new HashMap<>();
            // nutrientCols 맵은 실제 엑셀 속 영양소 컬럼의 인덱스와 단위 정보(g, mg) 등을 저장
            for (String excelHeader : nutrientHeaderToName.keySet()) {
                Integer idx = col.get(excelHeader);
                if (idx != null) {
                    Unit unit = normalizeUnit(unitFromHeader(excelHeader));
                    nutrientCols.put(excelHeader, new ColInfo(idx, unit));
                }
            }

            // === 2) 행 루프 ===
            final int BATCH = 500; int batchCount = 0;

            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                // 테스트 -> 30개만 먼저 처리
//                if (r > 30){
//                    break;
//                }

                // 루프로 두번째 행 ~ 마지막 행까지 한줄씩 순회하며 데이터 읽기
                Row row = sh.getRow(r);
                if (row == null) continue;
                totalRows++;

                String externalId = getString(row, cFoodCode, fmt);
                if (isBlank(externalId)) {
                    warnings.add("행 " + (r+1) + ": 식품코드 누락 → 스킵");
                    continue;
                }
                String name = getString(row, cRepName, fmt);
                BigDecimal calories = getDecimal(row, cKcal, fmt);
                String sourceName = (cSourceName == null) ? null : getString(row, cSourceName, fmt);

                // 2-1) Food upsert
                Food food = foodRepository.findByExternalId(externalId)
                        .orElseGet(() -> new Food(name, null, sourceName, externalId, calories));

                boolean changed;
                if (food.getId() == null) {
                    foodRepository.save(food);
                    foodsUpserted++;
                    changed = true;
                } else {
                    changed = food.merge(name, sourceName, calories);
                    if (changed) {
                        foodsUpserted++;
                        // JPA dirty checking
                    }
                }

                // 2-2) Nutrient & FoodNutrient upsert
                for (Map.Entry<String, ColInfo> e : nutrientCols.entrySet()) {
                    String headerName = e.getKey();
                    ColInfo ci = e.getValue();

                    BigDecimal val = getDecimal(row, ci.idx(), fmt);
                    if (val == null) continue;

                    String nutrientName = nutrientHeaderToName.get(headerName);

                    Nutrient nutrient = nutrientRepository.findByName(nutrientName)
                            .orElseGet(() -> nutrientRepository.save(
                                    new Nutrient(nutrientName, null, ci.unit())
                            ));

                    Optional<FoodNutrient> existing =
                            foodNutrientRepository.findByFoodIdAndNutrientId(food.getId(), nutrient.getId());

                    if (existing.isPresent()) {
                        if (existing.get().updateValueIfChanged(val)) {
                            nutrientsLinked++;
                        }
                    } else {
                        foodNutrientRepository.save(new FoodNutrient(food, nutrient, val));
                        nutrientsLinked++;
                    }
                }

                if (++batchCount >= BATCH) {
                    // 메모리/성능 위해 주기적 flush/clear
                    flushAndClear();
                    batchCount = 0;
                }
            }

            if (batchCount > 0) flushAndClear();
        }

        return FoodSeedDto.ImportResultDto.builder()
                .totalRows(totalRows)
                .foodsUpserted(foodsUpserted)
                .nutrientsLinked(nutrientsLinked)
                .warnings(warnings)
                .build();
    }

    // ===== util =====
    @PersistenceContext
    private EntityManager em;
    private void flushAndClear() { em.flush(); em.clear(); }

    private String getString(Row row, Integer idx, DataFormatter fmt) {
        if (idx == null) return null;
        Cell c = row.getCell(idx);
        return c == null ? null : fmt.formatCellValue(c).trim();
    }
    private BigDecimal getDecimal(Row row, Integer idx, DataFormatter fmt) {
        String s = getString(row, idx, fmt);
        if (isBlank(s)) return null;
        try {
            s = s.replace(",", "");              // 1,234.56 → 1234.56
            if (s.endsWith("%")) s = s.substring(0, s.length()-1);
            return new BigDecimal(s);
        } catch (Exception ex) {
            return null;
        }
    }
    private boolean isBlank(String s){ return s==null || s.isBlank(); }

    // "단백질(g)" → "g", "엽산(μg DFE)" → "μg DFE"
    private String unitFromHeader(String header) {
        int l = header.indexOf('('), r = header.indexOf(')');
        if (l > -1 && r > l) return header.substring(l + 1, r).trim();
        return "g";
    }

    // µg/μg/ug, 공백/접미어 포함 케이스를 전부 허용
    private Unit normalizeUnit(String u) {
        if (u == null) return Unit.g;
        // µ → μ 통일, 소문자
        String x = u.replace("µ", "μ").toLowerCase();

        // 공백/문자 혼용해도 포함 여부로 판단
        if (x.contains("μg") || x.contains("ug")) return Unit.μg;
        if (x.contains("mg")) return Unit.mg;
        if (x.contains("g"))  return Unit.g;

        // 모르면 g로 폴백
        return Unit.g;
    }

}
