package com.jinmifood.jinmi.item.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.domain.Like;
import com.jinmifood.jinmi.item.dto.request.AddLikeRequest;
import com.jinmifood.jinmi.item.repository.ItemRepository;
import com.jinmifood.jinmi.item.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void addLike(AddLikeRequest request, Long userId) {
        Long itemId = request.getItemId();

        if (likeRepository.existsByUserIdAndItemId(userId, itemId)) {
            // 이미 좋아요가 눌러져 있다면 예외 발생 또는 메시지 처리
            log.warn("User {} already liked item {}", userId, itemId);
            throw new CustomException(ErrorException.DUPLICATE_LIKE); // 사용자 정의 예외
        }

        Like like = request.toEntity();
        // 보안상 @AuthenticationPrincipal에서 받은 userId를 사용
        like.setUserId(userId);

        likeRepository.save(like);
        log.info("User {} liked item {}", userId, itemId);

        // 아이템테이블에 있는 LikeCnt 갯수 추가
        Item item = itemRepository.findItemByItemId(itemId);
        item.updateItemLikeCnt();
    }

    @Transactional
    public void removeLike(Long itemId, Long userId) {

        likeRepository.deleteByUserIdAndItemId(userId, itemId);
        log.info("User {} unliked item {}", userId, itemId);

        Item item = itemRepository.findItemByItemId(itemId);
        item.decreaseItemLikeCnt();

    }

}
