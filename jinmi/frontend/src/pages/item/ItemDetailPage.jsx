import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { fetchItem } from '../../api/itemApi';
import styles from './css/ItemDetailPage.module.css'; // CSS 모듈 import

export default function ItemDetailPage() {
    const { itemId } = useParams();
    const [item, setItem] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [quantity, setQuantity] = useState(1); // 수량 상태 추가, 기본값 1
    const [currentImageIndex, setCurrentImageIndex] = useState(0);
    useEffect(() => {
        const loadItemData = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await fetchItem(itemId);
                setItem(data);
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
    }, [itemId]);

    // 수량 증가 핸들러
    const handleIncreaseQuantity = () => {
        setQuantity(prevQuantity => prevQuantity + 1);
    };

    // 수량 감소 핸들러
    const handleDecreaseQuantity = () => {
        // 수량이 1보다 클 때만 감소
        setQuantity(prevQuantity => (prevQuantity > 1 ? prevQuantity - 1 : 1));
    };

    if (loading) {
        return <div className={styles.container}><h2 className={styles.loadingText}>상품 정보를 불러오는 중...</h2></div>;
    }

    if (error) {
        return <div className={styles.container}><h2 className={styles.errorText}>{error}</h2></div>;
    }

    if (!item) {
        return <div className={styles.container}><h2 className={styles.errorText}>상품 정보가 없습니다.</h2></div>;
    }
    // 이미지 타입에 따라 분리
    const mainImages = item.images ? item.images.filter(img => img.imageType === 'MAIN') : [];
    const detailImages = item.images ? item.images.filter(img => img.imageType === 'INFO') : [];

    const handlePrevImage = () => {
        setCurrentImageIndex(prevIndex => (prevIndex === 0 ? mainImages.length - 1 : prevIndex - 1));
    };

    const handleNextImage = () => {
        setCurrentImageIndex(prevIndex => (prevIndex === mainImages.length - 1 ? 0 : prevIndex + 1));
    };

    return (
        <div className={styles.container}>
            <h1 className={styles.pageTitle}>{item.itemName}</h1>

            <div className={styles.itemDetail}>
                <div className={styles.itemImageContainer}>
                    {mainImages.length > 0 ? (
                        <div className={styles.carousel}>
                            <img
                                src={mainImages[currentImageIndex].imageUrl}
                                alt={`${item.itemName} - ${currentImageIndex + 1}`}
                                className={styles.mainImage}
                            />
                            {mainImages.length > 1 && (
                                <>
                                    <button onClick={handlePrevImage} className={`${styles.carouselButton} ${styles.prev}`}>&#10094;</button>
                                    <button onClick={handleNextImage} className={`${styles.carouselButton} ${styles.next}`}>&#10095;</button>
                                </>
                            )}
                            <div className={styles.carouselIndicator}>
                                {currentImageIndex + 1} / {mainImages.length}
                            </div>
                        </div>
                    ) : (
                        <div className={styles.noImage}>이미지 없음</div>
                    )}
                </div>
                <div className={styles.itemInfoContainer}>
                    <h2 className={styles.itemName}>{item.itemName}</h2>
                    <p className={styles.itemPrice}>{item.itemPrice.toLocaleString()}</p>

                    <hr className={styles.divider} />

                    <div className={styles.itemStats}>
                        <span>좋아요 {item.likeCnt.toLocaleString()}</span>
                        <span className={styles.separator}>|</span>
                        <span>리뷰 {item.reviewCnt.toLocaleString()}</span>
                    </div>


                    <div className={styles.quantitySelector}>
                        <span className={styles.quantityLabel}>수량</span>
                        <div className={styles.quantityControls}>
                            <button
                                className={styles.quantityButton}
                                onClick={handleDecreaseQuantity}
                                aria-label="수량 감소"
                            >
                                -
                            </button>
                            <span className={styles.quantityDisplay} aria-live="polite">
                                {quantity}
                            </span>
                            <button
                                className={styles.quantityButton}
                                onClick={handleIncreaseQuantity}
                                aria-label="수량 증가"
                            >
                                +
                            </button>
                        </div>
                    </div>

                    <div className={styles.actionsContainer}>
                        <button className={styles.cartButton}>장바구니 담기</button>
                        <button className={styles.buyButton}>바로 구매</button>
                    </div>
                </div>
            </div>

            <div className={styles.itemContent}>
                <h3 className={styles.contentTitle}>상품 상세 설명</h3>
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
                    <p>상세 설명 이미지가 없습니다.</p>
                )}
            </div>
        </div>
    );
}