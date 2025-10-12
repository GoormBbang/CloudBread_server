package com.cloudbread.domain.feedback.domain.repository;

import com.cloudbread.domain.feedback.domain.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}

