package com.jinmifood.jinmi.review.repository;

import com.jinmifood.jinmi.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r.itemId FROM Review r WHERE r.reviewId = :reviewId")
    Long findItemIdByReviewId(Long reviewId);

    List<Review> findReviewsByItemId(Long itemId);

}
