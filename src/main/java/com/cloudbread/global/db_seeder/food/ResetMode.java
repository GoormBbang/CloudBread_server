package com.cloudbread.global.db_seeder.food;

public enum ResetMode {
    NONE,           // 아무 것도 비우지 않음
    FOOD_LINKS,     // food_nutrients만 TRUNCATE
    ALL             // food_nutrients + nutrients 둘 다 TRUNCATE
}