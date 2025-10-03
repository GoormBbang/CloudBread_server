package com.cloudbread.global.db_seeder.tip;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seed/tip")
@RequiredArgsConstructor
public class TipDataSeedController {

    private final com.cloudbread.global.db_seeder.tip.TipDataSeedService tipDataSeedService;

    @PostMapping("/import")
    public String importTips() {
        String filePath = "src/main/resources/export/tips.xlsx";
        tipDataSeedService.importFromExcel(filePath);
        return "Tips 데이터 Excel에서 DB로 저장 완료!";
    }
}
