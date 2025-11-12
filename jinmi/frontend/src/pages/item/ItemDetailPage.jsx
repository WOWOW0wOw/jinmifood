import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Heart } from 'lucide-react';
import { fetchItem } from '../../api/itemApi';
import { addLike, removeLike, getLikeStatus } from '../../api/likeApi';
import { addReview, getReviewsByItem, updateReview, deleteReview } from '../../api/reviewApi';
import { useAuth } from '../../context/AuthContext';
import styles from './css/ItemDetailPage.module.css';

export default function ItemDetailPage() {
    const { itemId } = useParams();
    const navigate = useNavigate();
    const { isLoggedIn, userNickname: currentUserNickname } = useAuth(); // 수정: userNickname 사용

    const [item, setItem] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);
    const [isLiked, setIsLiked] = useState(false);
    const [likeCount, setLikeCount] = useState(0);

    const [activeTab, setActiveTab] = useState('detail');
    const [reviewText, setReviewText] = useState('');
    const [reviews, setReviews] = useState([]);
    const [editingReviewId, setEditingReviewId] = useState(null);
    const [editingText, setEditingText] = useState('');

    const deliveryInfo = `배송/교환정보
배송
주문 시 2~3일 이내로 배송 출발합니다!
교환
교환 및 반품안내 

ㅁ제품에 문제가 있을 시에는 수령후 당일까지 
사진 찍어 보내주시기 바랍니다. 
ㅁ상품의 특성상 반품 및 교환이 어렵습니다. 
ㅁ상품의 하자가 아닌 주관적인 것에 의한 교환 및 환불은 어렵습니다. 
(맛,색,모양 등) 
ㅁ반품 시 반드시 택배를 이용하여 배송하여 주시기 바랍니다.
ㅁ사전 연락 없는 제품의 반품에 대하여는 반품승인이 어렵사오니 
고객센터로 연락 후 제품을 보내주시기 바랍니다. 
ㅁ제품에 문제가 있을 경우를 제외 단순변심으로 인한 
교환, 반품은 불가합니다.
ㅁ문제가 있을 경우 24시간 이내 연락부탁드립니다. 
ㅁ제품은 모두 꼼꼼하게 박스 포장하여 빠르게 보내드리고 있습니다. 
ㅁ환불 시 영업일 기준 3~5일 이내로 정상처리됩니다. 
ㅁ단순변심 교환 환불 시 7일 이내 처리가 되십니다.
ㅁ단순 변심 및 반품 교환시 왕복 택배비 6,000원이 발생되오니 참고해주시길 바랍니다. `; // 생략

    useEffect(() => {
        const loadAll = async () => {
            try {
                setLoading(true);
                const data = await fetchItem(itemId);
                setItem(data);
                setLikeCount(data.likeCnt);

                if (isLoggedIn) {
                    const liked = await getLikeStatus(itemId);
                    setIsLiked(liked);
                } else {
                    setIsLiked(false);
                }

                if (activeTab === 'review') {
                    const reviewList = await getReviewsByItem(itemId);
                    setReviews(reviewList);
                }
            } catch (err) {
                console.error(err);
                setError('상품 정보를 불러오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        if (itemId) loadAll();
    }, [itemId, isLoggedIn, activeTab]);

    const handleLikeClick = async () => {
        if (!isLoggedIn) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        const prev = isLiked;
        setIsLiked(!prev);
        setLikeCount(c => prev ? c - 1 : c + 1);

        try {
            prev ? await removeLike(itemId) : await addLike(itemId);
        } catch (e) {
            alert('좋아요 처리 실패');
            setIsLiked(prev);
            const fresh = await fetchItem(itemId);
            setLikeCount(fresh.likeCnt);
        }
    };

    const handleIncreaseQuantity = () => setQuantity(q => q + 1);
    const handleDecreaseQuantity = () => setQuantity(q => (q > 1 ? q - 1 : 1));

    const handleReviewSubmit = async (e) => {
        e.preventDefault();
        if (!isLoggedIn) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }
        if (!reviewText.trim()) {
            alert('리뷰 내용을 입력해주세요.');
            return;
        }

        try {
            await addReview(itemId, reviewText);
            setReviewText('');
            const updated = await fetchItem(itemId);
            setItem(updated);
            const newList = await getReviewsByItem(itemId);
            setReviews(newList);
            alert('리뷰가 등록되었습니다.');
        } catch (e) {
            alert('리뷰 등록 실패');
        }
    };

    const startEditing = (rev) => {
        setEditingReviewId(rev.reviewId);
        setEditingText(rev.content);
    };

    const handleEditSubmit = async (reviewId) => {
        if (!editingText.trim()) {
            alert('리뷰 내용을 입력해주세요.');
            return;
        }

        try {
            await updateReview(reviewId, editingText);
            setEditingReviewId(null);
            setEditingText('');
            const newList = await getReviewsByItem(itemId);
            setReviews(newList);
            alert('리뷰가 수정되었습니다.');
        } catch (e) {
            alert('리뷰 수정 실패');
        }
    };

    const handleDelete = async (reviewId) => {
        if (!window.confirm('리뷰를 삭제하시겠습니까?')) return;

        try {
            await deleteReview(reviewId);
            const updated = await fetchItem(itemId);
            setItem(updated);
            const newList = await getReviewsByItem(itemId);
            setReviews(newList);
            alert('리뷰가 삭제되었습니다.');
        } catch (e) {
            alert('리뷰 삭제 실패');
        }
    };

    if (loading) return <div className={styles.container}><p>로딩 중...</p></div>;
    if (error) return <div className={styles.container}><p>{error}</p></div>;
    if (!item) return <div className={styles.container}><p>상품이 없습니다.</p></div>;

    const mainImages = item?.images?.filter(i => i.imageType === 'MAIN') || [];
    const detailImages = item?.images?.filter(i => i.imageType === 'INFO') || [];
    const handlePrevImage = () => setCurrentImageIndex(i => (i === 0 ? mainImages.length - 1 : i - 1));
    const handleNextImage = () => setCurrentImageIndex(i => (i === mainImages.length - 1 ? 0 : i + 1));

    return (
        <div className={styles.container}>
            <section className={styles.itemHeader}>
                <div className={styles.imageCarousel}>
                    {mainImages.length > 0 ? (
                        <>
                            <img src={mainImages[currentImageIndex].imageUrl} alt={item.itemName} className={styles.mainImage} />
                            {mainImages.length > 1 && (
                                <>
                                    <button onClick={handlePrevImage} className={`${styles.carouselButton} ${styles.prev}`}>이전</button>
                                    <button onClick={handleNextImage} className={`${styles.carouselButton} ${styles.next}`}>다음</button>
                                    <div className={styles.carouselIndicator}>
                                        {currentImageIndex + 1} / {mainImages.length}
                                    </div>
                                </>
                            )}
                        </>
                    ) : (
                        <div className={styles.noImage}>이미지 없음</div>
                    )}
                </div>

                <div className={styles.infoSection}>
                    <h1 className={styles.itemName}>{item.itemName}</h1>
                    <div className={styles.priceSection}>
                        <span className={styles.price}>{item.itemPrice.toLocaleString()}원</span>
                        {item.itemWeight && <span className={styles.weight}> / {item.itemWeight}g</span>}
                    </div>

                    <div className={styles.stats}>
                        <span>좋아요 {likeCount.toLocaleString()}</span>
                        <span className={styles.separator}>|</span>
                        <span>리뷰 {item.reviewCnt.toLocaleString()}</span>
                    </div>

                    <div className={styles.quantitySelector}>
                        <span className={styles.quantityLabel}>수량</span>
                        <div className={styles.quantityControls}>
                            <button onClick={handleDecreaseQuantity}>-</button>
                            <span>{quantity}</span>
                            <button onClick={handleIncreaseQuantity}>+</button>
                        </div>
                    </div>

                    <div className={styles.actionsContainer}>
                        <button className={styles.cartButton}>장바구니 담기</button>
                        <button className={styles.buyButton}>바로 구매</button>
                        <button
                            className={`${styles.likeButton} ${isLiked ? styles.liked : ''}`}
                            onClick={handleLikeClick}
                            aria-label="좋아요"
                        >
                            <Heart size={24} />
                        </button>
                    </div>
                </div>
            </section>

            <section className={styles.tabNavigation}>
                <button className={activeTab === 'detail' ? styles.activeTab : ''} onClick={() => setActiveTab('detail')}>
                    상품 상세 정보
                </button>
                <button className={activeTab === 'review' ? styles.activeTab : ''} onClick={() => setActiveTab('review')}>
                    리뷰 ({item.reviewCnt})
                </button>
                <button className={activeTab === 'delivery' ? styles.activeTab : ''} onClick={() => setActiveTab('delivery')}>
                    배송·교환
                </button>
            </section>

            <section className={styles.tabContent}>
                {activeTab === 'detail' && (
                    <div>
                        {detailImages.length > 0 ? (
                            detailImages.map(img => (
                                <img key={img.imageId} src={img.imageUrl} alt="상세" className={styles.detailImage} />
                            ))
                        ) : (
                            <p className={styles.noDetails}>상세 설명 이미지가 없습니다.</p>
                        )}
                    </div>
                )}

                {activeTab === 'review' && (
                    <div className={styles.reviewSection}>
                        <form onSubmit={handleReviewSubmit} className={styles.reviewForm}>
                            <textarea
                                placeholder="리뷰를 작성해주세요..."
                                value={reviewText}
                                onChange={e => setReviewText(e.target.value)}
                                rows={4}
                                className={styles.reviewTextarea}
                            />
                            <button type="submit" className={styles.reviewSubmitBtn}>리뷰 등록</button>
                        </form>

                        <div className={styles.reviewList}>
                            {reviews.length === 0 ? (
                                <p className={styles.noReview}>아직 리뷰가 없습니다.</p>
                            ) : (
                                [...reviews]
                                    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
                                    .map((rev) => (
                                    <div key={rev.reviewId} className={styles.reviewItem}>
                                        <div className={styles.reviewMeta}>
                                            <span className={styles.reviewUser}>{rev.userNickname}</span>
                                            <span className={styles.reviewDate}>{new Date(rev.createdAt).toLocaleString()}</span>
                                        </div>
                                        {editingReviewId === rev.reviewId ? (
                                            <div>
                                                <textarea
                                                    value={editingText}
                                                    onChange={(e) => setEditingText(e.target.value)}
                                                    rows={4}
                                                    className={styles.reviewTextarea}
                                                />
                                                <div style={{ marginTop: '0.5rem' }}>
                                                    <button type="button" onClick={() => handleEditSubmit(rev.reviewId)}>저장</button>
                                                    <button type="button" onClick={() => setEditingReviewId(null)}>취소</button>
                                                </div>
                                            </div>
                                        ) : (
                                            <p className={styles.reviewContent}>{rev.content}</p>
                                        )}
                                        {rev.image && <img src={rev.image} alt="리뷰" className={styles.reviewImage} />}

                                        {/* 라인 158 ~ 165: 여기만 수정! */}
                                        {rev.userNickname === currentUserNickname && editingReviewId !== rev.reviewId && (
                                            <div className={styles.reviewActions}>
                                                <button onClick={() => startEditing(rev)}>수정</button>
                                                <button onClick={() => handleDelete(rev.reviewId)}>삭제</button>
                                            </div>
                                        )}
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                )}

                {activeTab === 'delivery' && (
                    <div className={styles.deliveryInfo}>
                        <pre style={{ whiteSpace: 'pre-wrap', fontFamily: 'inherit' }}>{deliveryInfo}</pre>
                    </div>
                )}
            </section>
        </div>
    );
}