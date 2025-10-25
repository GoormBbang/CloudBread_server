package com.cloudbread.domain.photo_analyses.application.candidate.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.food.domain.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DB에 있는 Food 데이터를 ElasticSearch로 한번에 밀어넣는(bulk index) 파이프라인
 */
@Component
@ConditionalOnProperty(name = "search.engine", havingValue = "es")
@RequiredArgsConstructor
@Slf4j
public class EsBootstrapper implements CommandLineRunner {
    private final ElasticsearchClient es;
    private final FoodRepository foodRepository;

    @Value("${search.es.index}")
    private String index;

    @Value("${search.bootstrap.enabled:true}")
    private boolean enabled;

    @Override
    public void run(String... args) throws Exception {
        if (!enabled){
            log.info("[ES-BOOT] disabled -> EsBootstrapper skip");
            return;
        }

        long esCount = es.count(CountRequest.of(c -> c.index(index))).count();
        if (esCount > 0){
            log.info("[ES-BOOT] index '{}' already has {} docs -> skip", index, esCount);
            return;
        }

        long dbCount = foodRepository.count();
        log.info("[ES-BOOT] start -> DB foods={}, index='{}'", dbCount, index);

        final int pageSize = 1000;
        int pageNo = 0;
        while (true) {
            // 1000건씩 조회
            Page<Food> page = foodRepository.findAll(PageRequest.of(pageNo, pageSize));
            if (page.isEmpty()) break;

            List<BulkOperation> ops = page.getContent().stream()
                    .map(this::toBulk)
                    .toList();

            // Elasticsearch Java Client가 Rest API를 호출해서 인덱스에 insert
            if (!ops.isEmpty()){
                es.bulk(b -> b.index(index).operations(ops).refresh(Refresh.WaitFor));
            }

            // 진행상황 로그 - 하나의 배치 색인이 끝날 때마다 호출 (현재완료 / 전체DB)
            long done = Math.min((long) (pageNo + 1) * pageSize, dbCount);
            log.info("[ES-BOOT] indexed {}/{}", done, dbCount);

            if (!page.hasNext()) break;
            pageNo++;
        }

        long after = es.count(c -> c.index(index)).count();
        log.info("[ES-BOOT] finished -> ES docs={}", after); // 색인 후 ES에 실제로 몇건이 저장되었는지
    }

    // Food -> BulkOperation (JSON 문서로 변환)
    private BulkOperation toBulk(Food f){
        Map<String, Object> doc = new HashMap<>();

        doc.put("id", f.getId());
        if (f.getName() != null){
            doc.put("name", f.getName().trim());
        }

        BigDecimal cal = f.getCalories();
        if (cal != null){
            doc.put("calories", cal);
        }

        return BulkOperation.of(b -> b.index(i -> i
                .index(index)
                .id(String.valueOf(f.getId()))
                .document(doc)
        ));
    }
}
