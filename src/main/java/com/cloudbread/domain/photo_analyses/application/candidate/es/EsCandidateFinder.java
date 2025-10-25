package com.cloudbread.domain.photo_analyses.application.candidate.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.cloudbread.domain.food.domain.entity.FoodNutrient;
import com.cloudbread.domain.food.domain.enums.Unit;
import com.cloudbread.domain.food.domain.repository.FoodNutrientRepository;
import com.cloudbread.domain.photo_analyses.application.candidate.CandidateFinder;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name="search.engine", havingValue="es")
@RequiredArgsConstructor
@Slf4j
public class EsCandidateFinder implements CandidateFinder {

    private final ElasticsearchClient es;
    private final FoodNutrientRepository foodNutrientRepository;

    @Value("${search.es.index}")
    private String index;

    @PostConstruct
    void init() {
        log.info("[CandidateFinder] Elasticsearch로 후보를 찾는다 (nutrients는 DB에서 보강)");
    }
    @Override
    public List<PhotoAnalysisResponse.CandidateItem> find(String query, int limit) {
        final String label = safe(query);
        final String norm  = normalize(label);        // 공백/언더바 제거
        final String rev   = reverseTokens(label);    // “새우 볶음밥” -> “볶음밥 새우” (토큰 2개 이상일 때만)

        try {
            // should절 + boost 가중치
            List<Query> shoulds = new ArrayList<>();


            // 완전일치: name.raw (가장 강함)
            shoulds.add(QueryBuilders.term(t -> t
                    .field("name.raw").value(label).boost(1000f)));

            // 정규화 동등성: name.norm (공백/언더바 제거)
            shoulds.add(QueryBuilders.term(t -> t
                    .field("name.norm").value(norm).boost(800f)));

            // 문구 일치(정순): “새우 볶음밥”
            shoulds.add(QueryBuilders.matchPhrase(mp -> mp
                    .field("name.analyzed").query(label).boost(400f)));

            // 문구 일치(역순): “볶음밥 새우”
            if (!rev.isBlank() && !rev.equals(label)) {
                shoulds.add(QueryBuilders.matchPhrase(mp -> mp
                        .field("name.analyzed").query(rev).boost(300f)));
            }

            // 토큰 AND 포함 : 둘 다 포함 (그래야 정확도 높일 수 있음)
            var tokens = splitTokens(label);
            if (tokens.length >= 2) {
                shoulds.add(QueryBuilders.multiMatch(mm -> mm
                        .query(label)
                        .type(TextQueryType.BestFields)
                        .fields(Collections.singletonList("name.analyzed^2"))
                        .operator(Operator.And)                  // 두 토큰 모두 포함
                     //   .minimumShouldMatch("100%")
                        .boost(200f)
                ));
            }

            // 부분/변형 보조: edge/ngram (낮은 가중치)
            shoulds.add(QueryBuilders.multiMatch(mm -> mm
                    .query(label)
                    .type(TextQueryType.BestFields)
                    // ★ 반드시 배열로 전달!
                    .fields(Arrays.asList("name.edge^0.5", "name.ngram^0.3"))
            ));

            // 최종 bool 쿼리 (하나라도 맞으면 점수 계산)
            Query finalQuery = QueryBuilders.bool(b -> b
                    .should(shoulds)
                    .minimumShouldMatch("1")
            );

            SearchResponse<Map> res = es.search(s -> s
                            .index(index)
                            .size(limit)
                            .query(finalQuery)
                            .source(src -> src.filter(f -> f.includes("id","name","calories"))),
                    Map.class);

            // nutrients는 DB에서 보강
            var hits = res.hits().hits();

            List<Long> ids = hits.stream()
                    .map(h -> {
                        Map<?,?> src = (Map<?,?>) h.source();
                        return src == null ? null : ((Number) src.get("id")).longValue();
                    })
                    .filter(Objects::nonNull)
                    .toList();

            // foodId -> nutrient list 맵
            Map<Long, List<FoodNutrient>> nutrientByFoodId = ids.isEmpty()
                    ? Map.of()
                    : foodNutrientRepository.findByFoodIdIn(ids).stream()
                    .collect(Collectors.groupingBy(fn -> fn.getFood().getId()));

            return hits.stream().map(h -> {
                Map<?,?> src = (Map<?,?>) h.source();

                Long id = src == null || src.get("id") == null ? null : ((Number) src.get("id")).longValue();
                String name = src == null ? null : (String) src.get("name");

                BigDecimal calories = null;
                if (src != null && src.get("calories") != null) {
                    calories = new BigDecimal(String.valueOf(src.get("calories")));
                }

                Map<String, PhotoAnalysisResponse.NutrientValue> nutrients = toNutrientsMap(nutrientByFoodId.getOrDefault(id, List.of()));

                return PhotoAnalysisResponse.CandidateItem.builder()
                        .foodId(id)
                        .name(name)
                        .calories(calories) // DTO 타입(BigDecimal)에 맞춤
                        .score(h.score() == null ? 0.0 : h.score())
                        .nutrients(nutrients)
                        .build();
            }).toList();

        } catch (Exception e) {
            log.error("[ES] search failed label='{}' : {}", label, e.toString());
            return List.of();
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /** 공백/언더바 제거 → name.norm 과 동일한 정규화 */
    private String normalize(String s) {
        return safe(s).replaceAll("[_\\s]+", "").toLowerCase();
    }

    /** 라벨을 공백/언더바 기준으로 분리 */
    private String[] splitTokens(String s) {
        String[] arr = safe(s).toLowerCase().split("[_\\s]+");
        return Arrays.stream(arr).filter(t -> !t.isBlank()).toArray(String[]::new);
    }

    /** 두 토큰 이상일 때 역순 phrase 생성 (“새우 볶음밥” -> “볶음밥 새우”) */
    private String reverseTokens(String s) {
        String[] arr = splitTokens(s);
        if (arr.length < 2) return "";
        return arr[1] + " " + arr[0];
    }

    private Map<String, PhotoAnalysisResponse.NutrientValue> toNutrientsMap(List<FoodNutrient> list) {
        Map<String, PhotoAnalysisResponse.NutrientValue> map = new LinkedHashMap<>();
        for (FoodNutrient fn : list) {
            String key  = normalizeKey(fn.getNutrient().getName()); // 예: "carbs"
            if ("calories".equals(key)) continue; // calories는 별도 필드로 분리
            String unit = unitSymbol(fn.getNutrient().getUnit());   // "g","mg","ug"...

            map.put(key, PhotoAnalysisResponse.NutrientValue.builder()
                    .value(fn.getValue())  // 이미 BigDecimal일 것
                    .unit(unit)
                    .build());
        }
        return map;
    }
    private String normalizeKey(String s) { return s == null ? null : s.toLowerCase(); }
    private String unitSymbol(Unit u) { return (u == null) ? null : u.name().toLowerCase(); }

}
