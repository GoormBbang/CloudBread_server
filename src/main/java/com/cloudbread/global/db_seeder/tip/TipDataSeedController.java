package com.cloudbread.global.db_seeder.tip;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;

import java.util.Map;

@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
public class TipDataSeedController {

    private final TipDataSeedService tipDataSeedService;

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> importTips(@RequestParam("file") MultipartFile file) {
        tipDataSeedService.importFromExcel(file);
        return ResponseEntity.ok(Map.of("message", "엑셀 데이터 업로드 성공!"));
    }

}
