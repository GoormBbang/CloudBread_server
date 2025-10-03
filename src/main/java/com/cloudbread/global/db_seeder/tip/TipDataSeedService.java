package com.cloudbread.global.db_seeder.tip;

import com.cloudbread.domain.crawling.domain.entity.*;
import com.cloudbread.domain.crawling.domain.enums.TipCategoryName;
import com.cloudbread.domain.crawling.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipDataSeedService {

    private final TipsRepository tipsRepository;
    private final TipCategoryRepository tipCategoryRepository;
    private final TipContentRepository tipContentRepository;

    /**
     * Excel 파일을 읽어 DB에 저장
     */
    public void importFromExcel(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            rows.next(); // ✅ 헤더 스킵

            while (rows.hasNext()) {
                Row row = rows.next();

                if (row == null) continue;
                if (row.getCell(0) == null) continue;

                int weekNumber = (int) row.getCell(0).getNumericCellValue();
                String category = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : "";
                String title = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : "";
                String description = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : "";

                // DTO 생성
                TipSeedDto dto = TipSeedDto.builder()
                        .weekNumber(weekNumber)
                        .category(category == null || category.isBlank() ? "UNKNOWN" : category)
                        .title(title)
                        .description(description)
                        .build();

                saveTip(dto);
            }

        } catch (Exception e) {
            log.error("엑셀 읽기 실패", e);
        }
    }

    /**
     * Excel에서 읽은 데이터를 DB에 Insert
     */
    private void saveTip(TipSeedDto dto) {
        // 1. 주차 엔티티 조회/생성
        Tips tip = tipsRepository.findByWeekNumber(dto.getWeekNumber())
                .orElseGet(() -> tipsRepository.save(
                        Tips.builder().weekNumber(dto.getWeekNumber()).build()
                ));

        // 2. 카테고리 매핑 (한글 → Enum)
        TipCategoryName categoryEnum = mapToEnum(dto.getCategory());

        TipCategory category = tipCategoryRepository.findByName(categoryEnum)
                .orElseGet(() -> tipCategoryRepository.save(
                        TipCategory.builder().name(categoryEnum).build()
                ));

        // 3. 컨텐츠 저장
        TipContent content = TipContent.builder()
                .tip(tip)
                .category(category)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .build();

        tipContentRepository.save(content);

        log.info("[DB Insert] {}주차 - {} (카테고리: {})",
                dto.getWeekNumber(), dto.getTitle(), categoryEnum);
    }

    /**
     * 한글 카테고리를 Enum으로 매핑
     */
    private TipCategoryName mapToEnum(String category) {
        if (category == null) return TipCategoryName.NUTRITION;

        switch (category.trim()) {
            case "임산부":
                return TipCategoryName.MOM;
            case "태아":
                return TipCategoryName.BABY;
            case "영양":
                return TipCategoryName.NUTRITION;
            default:
                return TipCategoryName.NUTRITION; // fallback
        }
    }
}
