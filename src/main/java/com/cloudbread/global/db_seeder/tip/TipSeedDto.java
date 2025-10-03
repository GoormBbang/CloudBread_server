package com.cloudbread.global.db_seeder.tip;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipSeedDto {
    private Integer weekNumber;
    private String category;   // 지금은 null 가능
    private String title;
    private String description;
}