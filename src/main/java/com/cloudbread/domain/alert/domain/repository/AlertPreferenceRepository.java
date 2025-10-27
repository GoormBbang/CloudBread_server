package com.cloudbread.domain.alert.domain.repository;

import com.cloudbread.domain.alert.domain.entity.AlertPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AlertPreferenceRepository extends JpaRepository<AlertPreference, Long> {
    Optional<AlertPreference> findByUserId(Long userId);
}
