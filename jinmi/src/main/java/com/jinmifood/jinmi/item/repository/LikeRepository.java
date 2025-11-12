package com.jinmifood.jinmi.item.repository;

import com.jinmifood.jinmi.item.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndItemId(Long userId, Long itemId);

    // 특정 userId와 itemId 조합의 Like 엔티티가 존재하는지 확인하는 메서드
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    // 특정 userId와 itemId 조합의 Like 엔티티를 삭제하는 메서드 (반환값은 삭제된 레코드 수)
    void deleteByUserIdAndItemId(Long userId, Long itemId);

    List<Like> findAllByUserId(Long userId);

}
