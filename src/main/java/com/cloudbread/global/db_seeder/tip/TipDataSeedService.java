package com.cloudbread.global.db_seeder.tip;

import com.cloudbread.domain.crawling.domain.entity.*;
import com.cloudbread.domain.crawling.domain.enums.TipCategoryName;
import com.cloudbread.domain.crawling.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipDataSeedService {

    private final TipsRepository tipsRepository;
    private final TipCategoryRepository tipCategoryRepository;
    private final TipContentRepository tipContentRepository;

    // ✅ 버전 1 : 셀 타입 구분해서 처리
    public void importFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            rows.next(); // ✅ 헤더 스킵

            while (rows.hasNext()) {
                Row row = rows.next();
                if (row == null || row.getCell(0) == null) continue;

                int weekNumber;
                if (row.getCell(0).getCellType() == CellType.NUMERIC) {
                    weekNumber = (int) row.getCell(0).getNumericCellValue();
                } else {
                    weekNumber = Integer.parseInt(row.getCell(0).getStringCellValue());
                }

                String category = getCellValue(row.getCell(1));
                String title = getCellValue(row.getCell(2));
                String description = getCellValue(row.getCell(3));

                TipSeedDto dto = TipSeedDto.builder()
                        .weekNumber(weekNumber)
                        .category(category.isBlank() ? "UNKNOWN" : category)
                        .title(title)
                        .description(description)
                        .build();

                saveTip(dto);
            }

        } catch (Exception e) {
            log.error("엑셀 읽기 실패", e);
            throw new RuntimeException("엑셀 파싱 중 오류 발생", e);
        }
    }

    // ✅ 버전 2 : 단순 Numeric 전용 (원하신 코드 그대로)
    public void importFromExcelSimple(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            rows.next(); // 헤더 스킵

            while (rows.hasNext()) {
                Row row = rows.next();
                if (row == null || row.getCell(0) == null) continue;

                int weekNumber = (int) row.getCell(0).getNumericCellValue();
                String category = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : "";
                String title = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : "";
                String description = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : "";

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

    // 🔽 이하 기존 saveTip, getCellValue, mapToEnum 메서드 그대로 두시면 됩니다
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private void saveTip(TipSeedDto dto) {
        Tips tip = tipsRepository.findByWeekNumber(dto.getWeekNumber())
                .orElseGet(() -> tipsRepository.save(
                        Tips.builder().weekNumber(dto.getWeekNumber()).build()
                ));

        TipCategoryName categoryEnum = mapToEnum(dto.getCategory());
        TipCategory category = tipCategoryRepository.findByName(categoryEnum)
                .orElseGet(() -> tipCategoryRepository.save(
                        TipCategory.builder().name(categoryEnum).build()
                ));

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

    private TipCategoryName mapToEnum(String category) {
        if (category == null) return TipCategoryName.NUTRITION;

        switch (category.trim()) {
            case "임산부" -> { return TipCategoryName.MOM; }
            case "태아" -> { return TipCategoryName.BABY; }
            case "영양" -> { return TipCategoryName.NUTRITION; }
            default -> { return TipCategoryName.NUTRITION; }
        }
    }
}
