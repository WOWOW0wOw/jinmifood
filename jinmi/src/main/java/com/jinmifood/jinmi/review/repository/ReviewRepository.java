package com.jinmifood.jinmi.review.repository;

import com.jinmifood.jinmi.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
