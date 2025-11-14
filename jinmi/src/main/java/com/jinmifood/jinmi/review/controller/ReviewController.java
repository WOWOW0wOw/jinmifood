package com.jinmifood.jinmi.review.controller;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.common.security.CustomUserDetails;
import com.jinmifood.jinmi.common.security.JwtTokenProvider;
import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.review.domain.Review;
import com.jinmifood.jinmi.review.dto.request.AddReviewRequest;
import com.jinmifood.jinmi.review.dto.request.UpdateReviewRequest;
import com.jinmifood.jinmi.review.dto.response.ViewReviewResponse;
import com.jinmifood.jinmi.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/add")
    public StatusResponseDTO addReview(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AddReviewRequest request) {

        if (userDetails == null) {
            log.error("@AuthenticationPrincipal userDetails is NULL. 인증 정보가 SecurityContext에 없습니다.");
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN);
        }
        Long userId = userDetails.getId();
        log.info("Request to add like for item {} by user {}", request.getItemId(), userId);

        reviewService.addReview(request, userId);

        return StatusResponseDTO.ok("리뷰가 성공적으로 저장되었습니다.");
    }

    @PostMapping("/remove")
    public StatusResponseDTO deleteReview(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam Long reviewId) {
        if (userDetails == null) {
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN);
        }
        Long userId = userDetails.getId();
        // 서비스 호출 시 userId 전달
        reviewService.deleteReview(reviewId, userId);
        return StatusResponseDTO.ok("리뷰가 성공적으로 삭제되었습니다.");
    }

    @PostMapping("/update")
    public StatusResponseDTO updateReview(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UpdateReviewRequest request, @RequestParam Long reviewId) {
        if (userDetails == null) {
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN);
        }
        Long userId = userDetails.getId();
        // 서비스 호출 시 userId 전달
        reviewService.updateReview(request, reviewId, userId);
        return StatusResponseDTO.ok("리뷰가 성공적으로 수정되었습니다.");
    }

    @GetMapping("/listByItem")
    public StatusResponseDTO getReviews(@RequestParam Long itemId) {

        List<ViewReviewResponse> list = reviewService.getReviewListByItem(itemId);

        return StatusResponseDTO.ok(list);
    }

    @GetMapping("/listAll")
    public StatusResponseDTO getAllReviews() {
        List<ViewReviewResponse> list = reviewService.getAllReviewList();
        return StatusResponseDTO.ok(list);
    }


}
