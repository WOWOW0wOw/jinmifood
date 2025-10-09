package com.jinmifood.jinmi.review.service;

import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.repository.ItemRepository;
import com.jinmifood.jinmi.review.domain.Review;
import com.jinmifood.jinmi.review.dto.request.AddReviewRequest;
import com.jinmifood.jinmi.review.dto.request.UpdateReviewRequest;
import com.jinmifood.jinmi.review.dto.response.ViewReviewResponse;
import com.jinmifood.jinmi.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void addReview(AddReviewRequest request, Long userId) {

        Long itemId = request.getItemId();

        Review review = request.toEntity();
        review.setUserId(userId);

        reviewRepository.save(review);
        log.info("Review added: " + review);

        Item item = itemRepository.findItemByItemId(itemId);
        item.updateReviewCnt();

    }

    @Transactional
    public void deleteReview(Long reviewId) {

        log.info("reviewId: " + reviewId);
        Long itemId = reviewRepository.findItemIdByReviewId(reviewId);
        log.info("itemId: " + itemId);
        reviewRepository.deleteById(reviewId);
        Item item = itemRepository.findItemByItemId(itemId);
        log.info("item: " + item);
        item.decreaseReviewCnt();

    }

    @Transactional
    public void updateReview(UpdateReviewRequest request,Long reviewId) {

        log.info("reviewId: " + reviewId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Review를 찾을 수 없습니다. reviewId: {}", reviewId);
                    return new IllegalArgumentException("존재하지 않는 리뷰입니다. reviewId: " + reviewId);
                });
        log.info("review: " + review);

        review.updateReviewDetails(request);
        log.info("updateReview: " + review);

    }

    public List<ViewReviewResponse> getReviewListByItem(Long itemId) {

        List<Review> reviews = reviewRepository.findReviewsByItemId(itemId);
        return reviews.stream()
                .map(ViewReviewResponse::new)
                .collect(Collectors.toList());

    }

    public List<ViewReviewResponse> getAllReviewList(){
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(ViewReviewResponse::new)
                .collect(Collectors.toList());
    }


}
