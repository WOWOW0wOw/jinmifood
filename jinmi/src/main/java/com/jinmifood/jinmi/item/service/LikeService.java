package com.jinmifood.jinmi.item.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.item.domain.Item;
import com.jinmifood.jinmi.item.domain.ItemImage;
import com.jinmifood.jinmi.item.domain.Like;
import com.jinmifood.jinmi.item.dto.request.AddLikeRequest;
import com.jinmifood.jinmi.item.dto.response.MyLikeItemResponse;
import com.jinmifood.jinmi.item.repository.ItemRepository;
import com.jinmifood.jinmi.item.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<MyLikeItemResponse> getMyLikeItems(Long userId) {

        List<Like> likes = likeRepository.findAllByUserId(userId);

        return likes.stream()
                .map(like -> {
                    Item item = itemRepository.findItemByItemId(like.getItemId());

                    if (item == null) {
                        log.warn("찜 목록에 있는 아이템(ID: {})이 DB에 존재하지 않습니다. 스킵합니다.", like.getItemId());
                        return null;
                    }
                    Optional<String> firstMainImageUrl = item.getImages().stream()
                            .filter(image -> image.getImageType() == ItemImage.ImageType.MAIN)
                            .map(ItemImage::getImageUrl)
                            .findFirst();

                    String imageUrl = firstMainImageUrl.orElse(null);
                    if (item.getImages() != null && !item.getImages().isEmpty()) {
                        imageUrl = item.getImages().get(0).getImageUrl();
                    }

                    return MyLikeItemResponse.builder()
                            .likeId(like.getId())
                            .itemId(item.getItemId())
                            .name(item.getItemName())
                            .price(item.getItemPrice())
                            .imageUrl(imageUrl)
                            .build();
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
}
