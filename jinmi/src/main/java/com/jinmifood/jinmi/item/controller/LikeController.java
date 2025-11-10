package com.jinmifood.jinmi.item.controller;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.common.security.CustomUserDetails;
import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.item.dto.request.AddLikeRequest;
import com.jinmifood.jinmi.item.repository.LikeRepository;
import com.jinmifood.jinmi.item.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;
    private final LikeRepository likeRepository;

    @PostMapping("/add")
    public StatusResponseDTO addLike(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AddLikeRequest request) {

        if (userDetails == null) {
            log.error("@AuthenticationPrincipal userDetails is NULL. 인증 정보가 SecurityContext에 없습니다.");
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN);
        }
        Long userId = userDetails.getId();
        log.info("Request to add like for item {} by user {}", request.getItemId(), userId);

        likeService.addLike(request,userId);

        return StatusResponseDTO.ok("좋아요가 성공적으로 추가되었습니다.");
    }

    @PostMapping("/remove")
    public StatusResponseDTO removeLike(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AddLikeRequest request) {

        if (userDetails == null) {
            log.error("@AuthenticationPrincipal userDetails is NULL. 인증 정보가 SecurityContext에 없습니다.");
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN);
        }
        Long userId = userDetails.getId();
        Long itemId = request.getItemId();

        log.info("Request to remove like for item {} by user {}", itemId, userId);

        likeService.removeLike(itemId,userId);


        return StatusResponseDTO.ok("좋아요가 성공적으로 취소되었습니다.");
    }

    @GetMapping("/status/{itemId}")
    public StatusResponseDTO getLikeStatus(@PathVariable Long itemId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.error("@AuthenticationPrincipal userDetails is NULL.");
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN);
        }
        Long userId = userDetails.getId();
        boolean isLiked = likeRepository.existsByUserIdAndItemId(userId, itemId);
        return StatusResponseDTO.ok(isLiked); 
    }


}
