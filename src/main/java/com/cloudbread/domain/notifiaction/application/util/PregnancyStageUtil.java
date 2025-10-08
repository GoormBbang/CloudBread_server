package com.cloudbread.domain.notifiaction.application.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 임신 주기를 계산해, 현재 시점이 임신 초/중/후기 어디인지 알려주는 역할
 *
 * early : 1주 ~ 12주
 * mid : 13주 ~ 27주
 * late : 28주 ~ 40주
 */
public class PregnancyStageUtil {
    public enum Stage { EARLY, MID, LATE }
    public static Stage stageOf(LocalDate dueDate, LocalDate today) { // 출산예정일, 오늘날짜
        if (dueDate == null) return Stage.MID; // fallback

        LocalDate start = dueDate.minusWeeks(40); // 임신 시작일

        long weeks = ChronoUnit.WEEKS.between(start, today);

        if (weeks <= 12) return Stage.EARLY;
        if (weeks <= 27) return Stage.MID;
        return Stage.LATE;
    }
}
