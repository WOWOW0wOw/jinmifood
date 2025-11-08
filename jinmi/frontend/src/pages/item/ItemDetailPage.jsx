import React from 'react';
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchItem } from '../../api/itemApi';
import { addLike, removeLike } from '../../api/likeApi'; // 새로 만든 likeApi 임포트
import { useAuth } from '../../context/AuthContext'; // Header에서 사용하는 AuthContext 임포트
import styles from './css/ItemDetailPage.module.css';

// 하트 아이콘 SVG 컴포넌트
const HeartIcon = () => (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12.0001 5.24438C10.0331 2.90138 6.55113 2.63938 4.23213 4.95838C1.91313 7.27738 1.65113 10.7594 3.58513 13.3884L11.5431 22.2104C11.7911 22.4834 12.2091 22.4834 12.4571 22.2104L20.4151 13.3884C22.3491 10.7594 22.0871 7.27738 19.7681 4.95838C17.4491 2.63938 13.9671 2.90138 12.0001 5.24438Z" />
    </svg>
);

export default function ItemDetailPage() {
    const { itemId } = useParams();
    const navigate = useNavigate();
    const { isLoggedIn } = useAuth(); // AuthContext에서 로그인 상태 가져오기

    const [item, setItem] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);

    // UI 즉시 반응을 위한 '좋아요' 관련 상태
    const [isLiked, setIsLiked] = useState(false);
    const [likeCount, setLikeCount] = useState(0);

    useEffect(() => {
        const loadItemData = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await fetchItem(itemId);
                setItem(data);
                setLikeCount(data.likeCnt);

                if (data.likedByCurrentUser && isLoggedIn) {
                    setIsLiked(true);
                } else {
                    setIsLiked(false);
                }

            } catch (err) {
                console.error('Failed to load item data:', err);
                setError('상품 정보를 불러오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        if (itemId) {
            loadItemData();
        }
    }, [itemId, isLoggedIn]); // isLoggedIn이 바뀔 때도 재확인

    // '좋아요' 버튼 클릭 통합 핸들러
    const handleLikeClick = async () => {
        // 1. 로그인 상태 확인
        if (!isLoggedIn) {
            alert('로그인이 필요한 기능입니다.');
            navigate('/login'); // 로그인 페이지로 이동
            return;
        }

        // 2. UI 즉시 업데이트 (Optimistic Update)
        const originalIsLiked = isLiked;
        const originalLikeCount = likeCount;

        setIsLiked(!originalIsLiked);
        setLikeCount(originalIsLiked ? originalLikeCount - 1 : originalLikeCount + 1);

        try {
            // 3. API 호출
            if (originalIsLiked) {
                // 이미 좋아한 상태였으므로, '좋아요 취소' API 호출
                await removeLike(itemId);
            } else {
                // 좋아하지 않은 상태였으므로, '좋아요 추가' API 호출
                await addLike(itemId);
            }
        } catch (err) {
            // 4. API 호출 실패 시, UI 상태를 원래대로 롤백
            console.error("'좋아요' 처리 실패:", err);
            alert("요청 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
            setIsLiked(originalIsLiked);
            setLikeCount(originalLikeCount);
        }
    };


    const handleIncreaseQuantity = () => setQuantity(prev => prev + 1);
    const handleDecreaseQuantity = () => setQuantity(prev => (prev > 1 ? prev - 1 : 1));

    if (loading) return <div className={styles.container}><h2 className={styles.loadingText}>상품 정보를 불러오는 중...</h2></div>;
    if (error) return <div className={styles.container}><h2 className={styles.errorText}>{error}</h2></div>;
    if (!item) return <div className={styles.container}><h2 className={styles.errorText}>상품 정보가 없습니다.</h2></div>;

    const mainImages = item.images ? item.images.filter(img => img.imageType === 'MAIN') : [];
    const detailImages = item.images ? item.images.filter(img => img.imageType === 'INFO') : [];

    const handlePrevImage = () => setCurrentImageIndex(prev => (prev === 0 ? mainImages.length - 1 : prev - 1));
    const handleNextImage = () => setCurrentImageIndex(prev => (prev === mainImages.length - 1 ? 0 : prev + 1));

    return (
        <div className={styles.container}>
            <section className={styles.heroSection}>
                <div className={styles.carousel}>
                    {mainImages.length > 0 ? (
                        <img
                            src={mainImages[currentImageIndex].imageUrl}
                            alt={`${item.itemName} - ${currentImageIndex + 1}`}
                            className={styles.mainImage}
                        />
                    ) : (
                        <div className={styles.noImage}>이미지 없음</div>
                    )}
                </div>

                <div className={styles.infoOverlay}>
                    <div className={styles.infoContent}>
                        <h1 className={styles.itemName}>{item.itemName}</h1>
                        <p className={styles.itemPrice}>{item.itemPrice.toLocaleString()}원</p>

                        <div className={styles.itemStats}>
                            {/* '좋아요' 수를 state(likeCount)에서 가져오도록 수정 */}
                            <span>좋아요 {likeCount.toLocaleString()}</span>
                            <span className={styles.separator}>|</span>
                            <span>리뷰 {item.reviewCnt.toLocaleString()}</span>
                        </div>

                        <div className={styles.quantitySelector}>
                            <span className={styles.quantityLabel}>수량</span>
                            <div className={styles.quantityControls}>
                                <button className={styles.quantityButton} onClick={handleDecreaseQuantity}>-</button>
                                <span className={styles.quantityDisplay}>{quantity}</span>
                                <button className={styles.quantityButton} onClick={handleIncreaseQuantity}>+</button>
                            </div>
                        </div>

                        <div className={styles.actionsContainer}>
                            <button className={styles.cartButton}>장바구니 담기</button>
                            <button className={styles.buyButton}>바로 구매</button>
                        </div>
                    </div>
                </div>

                <button
                    className={`${styles.likeButton} ${isLiked ? styles.liked : ''}`}
                    onClick={handleLikeClick} // 통합 핸들러 연결
                    aria-label="좋아요"
                >
                    <HeartIcon />
                </button>

                {mainImages.length > 1 && (
                    <>
                        <button onClick={handlePrevImage} className={`${styles.carouselButton} ${styles.prev}`}>&#10094;</button>
                        <button onClick={handleNextImage} className={`${styles.carouselButton} ${styles.next}`}>&#10095;</button>
                        <div className={styles.carouselIndicator}>
                            {currentImageIndex + 1} / {mainImages.length}
                        </div>
                    </>
                )}
            </section>

            <section className={styles.itemContent}>
                <h2 className={styles.contentTitle}>상품 상세 정보</h2>
                {detailImages.length > 0 ? (
                    detailImages.map(image => (
                        <img
                            key={image.imageId}
                            src={image.imageUrl}
                            alt={`${item.itemName} 상세 정보`}
                            className={styles.detailImage}
                        />
                    ))
                ) : (
                    <p className={styles.noDetails}>상세 설명 이미지가 없습니다.</p>
                )}
            </section>
        </div>
    );
}