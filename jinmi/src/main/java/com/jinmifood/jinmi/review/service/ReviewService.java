package com.jinmifood.jinmi.review.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.repository.ItemRepository;
import com.jinmifood.jinmi.review.domain.Review;
import com.jinmifood.jinmi.review.dto.request.AddReviewRequest;
import com.jinmifood.jinmi.review.dto.request.UpdateReviewRequest;
import com.jinmifood.jinmi.review.dto.response.ViewReviewResponse;
import com.jinmifood.jinmi.review.repository.ReviewRepository;
import com.jinmifood.jinmi.user.domain.User;
import com.jinmifood.jinmi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

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
    public void deleteReview(Long reviewId, Long currentUserId) { // currentUserId 파라미터 추가
        log.info("reviewId: {} 삭제 요청 by userId: {}", reviewId, currentUserId);

        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다. reviewId: " + reviewId));

        // 2. 권한 확인 (중요)
        if (!review.getUserId().equals(currentUserId)) {
            throw new CustomException(ErrorException.USER_NOT_FOUND); // 권한 없음 예외 발생
        }

        // 3. 기존 삭제 로직 수행
        Long itemId = review.getItemId();
        reviewRepository.deleteById(reviewId);
        Item item = itemRepository.findItemByItemId(itemId);
        item.decreaseReviewCnt();
        log.info("리뷰 삭제 완료");
    }

    @Transactional
    public void updateReview(UpdateReviewRequest request, Long reviewId, Long currentUserId) { // currentUserId 파라미터 추가
        log.info("reviewId: {} 수정 요청 by userId: {}", reviewId, currentUserId);

        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다. reviewId: " + reviewId));

        // 2. 권한 확인 (중요)
        if (!review.getUserId().equals(currentUserId)) {
            throw new CustomException(ErrorException.USER_NOT_FOUND); // 권한 없음 예외 발생
        }

        // 3. 기존 수정 로직 수행
        review.updateReviewDetails(request);
        log.info("updateReview: " + review);
    }

    public List<ViewReviewResponse> getReviewListByItem(Long itemId) {
        List<Review> reviews = reviewRepository.findReviewsByItemId(itemId);
        if (reviews.isEmpty()) {
            return List.of(); // 리뷰가 없으면 빈 리스트 반환
        }
        List<Long> userIds = reviews.stream()
                .map(Review::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return reviews.stream()
                .map(review -> {
                    User user = userMap.get(review.getUserId());
                    return new ViewReviewResponse(review, user);
                })
                .collect(Collectors.toList());
    }

    public List<ViewReviewResponse> getAllReviewList(){
        // 1. 모든 리뷰를 데이터베이스에서 조회합니다.
        List<Review> reviews = reviewRepository.findAll();
        if (reviews.isEmpty()) {
            return List.of(); // 리뷰가 없으면 빈 리스트를 즉시 반환합니다.
        }

        // 2. 조회된 리뷰 목록에서 모든 작성자의 ID(userId)를 중복 없이 추출합니다.
        List<Long> userIds = reviews.stream()
                .map(Review::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 추출된 userId 목록을 사용하여 관련된 모든 사용자(User) 정보를 한 번의 쿼리로 가져옵니다. (N+1 문제 방지)
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 4. 리뷰 목록을 순회하면서, 각 리뷰의 userId에 해당하는 사용자 정보를 Map에서 찾아 DTO를 생성합니다.
        return reviews.stream()
                .map(review -> {
                    User user = userMap.get(review.getUserId());
                    // user가 null일 경우를 대비한 방어 코드 (예: 탈퇴한 사용자)
                    if (user == null) {
                        // 탈퇴한 사용자의 리뷰는 닉네임을 "(알 수 없음)" 등으로 표시할 수 있습니다.
                        // 여기서는 간단히 User 객체를 새로 만들어 기본값을 넣어줍니다.
                        user = User.builder().displayName("(알 수 없음)").build();
                    }
                    return new ViewReviewResponse(review, user);
                })
                .collect(Collectors.toList());
    }


}
