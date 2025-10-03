//package com.cloudbread.domain.crawling.api;
//
//import com.cloudbread.domain.crawling.application.TipDataSeedService;
//import com.cloudbread.domain.crawling.domain.dto.TipSeedDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/admin/tips")
//@RequiredArgsConstructor
//public class TipDataSeedController {
//
//    private final TipDataSeedService tipDataSeedService;
//
//    @PostMapping("/import")
//    public ResponseEntity<String> importTips(@RequestBody TipSeedDto dto) {
//        tipDataSeedService.saveTip(dto);
//        return ResponseEntity.ok("저장 완료");
//    }
//}
