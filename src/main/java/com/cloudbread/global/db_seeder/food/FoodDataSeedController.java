package com.cloudbread.global.db_seeder.food;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// POST 요청을 받아 데이터 처리를 시작하는 REST 컨트롤러
@RestController
@RequestMapping("/api/admin/foods")
@RequiredArgsConstructor
public class FoodDataSeedController {
    private final FoodDataSeedService foodDataSeedService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodSeedDto.ImportResultDto> importByUpload(
            @RequestPart("file") MultipartFile file) throws Exception {
        FoodSeedDto.ImportResultDto result =
                foodDataSeedService.importFromFile(file.getInputStream(), file.getOriginalFilename());
        return ResponseEntity.ok(result);
    }
}
