package com.cloudbread.domain.food.domain.repository;

import com.cloudbread.domain.food.domain.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {
    Optional<Food> findByExternalId(String externalId);

    @Query("select f from Food f where lower(f.name) like lower(concat('%', :q, '%'))")
    List<Food> searchByNameContains(@Param("q") String q, Pageable pageable);

    // 오버로드 (간단히 size만)
    default List<Food> searchByNameContains(String q, int size) {
        return searchByNameContains(q, org.springframework.data.domain.PageRequest.of(0, size));
    }

    @Query("""
        select f from Food f
        where lower(f.name) like lower(concat('%', :q, '%'))
        order by 
          case when lower(f.name) = lower(:q) then 0
               when lower(f.name) like lower(concat(:q, '%')) then 1
               else 2 end,
          f.name asc
    """)
    List<Food> searchByNameContainsSuggest(@Param("q") String q, Pageable pageable);
}
