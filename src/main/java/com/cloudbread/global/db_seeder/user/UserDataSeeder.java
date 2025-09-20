package com.cloudbread.global.db_seeder.user;

import com.cloudbread.domain.user.domain.entity.Allergy;
import com.cloudbread.domain.user.domain.entity.DietType;
import com.cloudbread.domain.user.domain.entity.HealthType;
import com.cloudbread.domain.user.domain.enums.DietTypeEnum;
import com.cloudbread.domain.user.domain.enums.HealthTypeEnum;
import com.cloudbread.domain.user.domain.repository.AllergyRepository;
import com.cloudbread.domain.user.domain.repository.DietTypeRepository;
import com.cloudbread.domain.user.domain.repository.HealthTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDataSeeder implements ApplicationRunner {
    private final DietTypeRepository dietTypeRepository;
    private final HealthTypeRepository healthTypeRepository;
    private final AllergyRepository allergyRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // DietType 저장 (enum 타입)
        for (DietTypeEnum type : DietTypeEnum.values()){
            if (!dietTypeRepository.existsByName(type)){
                dietTypeRepository.save(DietType.builder().name(type).build());
            }
        }

        // HealthType 저장 (enum 타입)
        for (HealthTypeEnum type : HealthTypeEnum.values()){
            if (!healthTypeRepository.existsByName(type)){
                healthTypeRepository.save(HealthType.builder().name(type).build());
            }
        }

        // Allergy 식단 저장 (String 타입)
        for (String name : new String[]{"알류(가금류만 포함)", "우유", "메밀", "땅콩", "대두", "밀", "고등어", "게", "새우", "돼지고기", "복숭아", "토마토", "아황산류", "호두", "닭고기", "쇠고기", "오징어", "굴", "전복", "홍합", "잣"}) {
            if (!allergyRepository.existsByName(name)) {
                allergyRepository.save(Allergy.builder().name(name).build());
            }
        }
    }


}
