import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import apiClient from "../../api/apiClient.js";
import s from "../item/css/ItemPage.module.css";

export default function LikeListPage() {
    const [likeItems, setLikeItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const { accessToken, isLoading: isAuthLoading } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        const localAccessToken = localStorage.getItem('accessToken');
        const tokenToUse = accessToken || localAccessToken;

        console.log('--- useEffect 찜 목록 실행 (수정된 로직) ---');
        console.log('isAuthLoading:', isAuthLoading);
        console.log('tokenToUse 존재 여부 (로컬 포함):', !!tokenToUse);

        if (!tokenToUse) {
            console.error("fetchLikes: Access Token이 없어 API 호출을 건너뜁니다.");
            setError("로그인이 필요합니다.");
            setLoading(false);
            return;
        }

        const fetchLikes = async () => {
            console.log('fetchLikes 함수 실행 시도');

            console.log("Access Token 유효 확인. 찜 목록 API 호출 시작.");

            try {
                const response = await apiClient.get("/likes/myList");

                if (response.data && response.data.data) {
                    setLikeItems(response.data.data);
                    console.log(`찜 목록 로드 성공. 항목 개수: ${response.data.data.length}`);
                } else {
                    setLikeItems([]);
                    console.log("찜 목록 로드 성공했으나, 데이터가 비어있습니다.");
                }
            } catch (err) {
                console.error("찜 목록 로드 실패 (API 호출 에러):", err);

                if (err.response && err.response.status === 401) {
                    setError("세션이 만료되었습니다. 다시 로그인해 주세요.");
                } else {
                    setError("찜 목록을 불러오는 데 실패했습니다.");
                }
            } finally {
                setLoading(false);
            }
        };

        fetchLikes();
    }, [accessToken, isAuthLoading]);


    if (loading) {
        return (
            <div className={s.container}>
                <div className={s.skeletonGrid}>
                    {Array.from({ length: 8 }).map((_, i) => (
                        <div key={i} className={s.skeletonCard} />
                    ))}
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className={s.container}>
                <p className={s.error}>{error}</p>
            </div>
        );
    }

    return (
        <div className={s.container}>
            <h2>나의 찜 목록</h2>
            <hr />

            {likeItems.length === 0 ? (
                <p className={s.error}>아직 찜한 상품이 없습니다. 마음에 드는 상품을 찾아보세요!</p>
            ) : (
                <div className={s.itemList}>
                    {likeItems.map(item => {
                        const itemId = item.itemId;
                        const thumbnailUrl = item.imageUrl || item.mainImageUrl;
                        const itemName = item.name || item.itemName;
                        const itemPrice = item.price || item.itemPrice;
                        const itemStatus = item.status || 'SALE';

                        const priceText = itemStatus === "SALE"
                            ? `${(itemPrice || 0).toLocaleString()}원`
                            : "품절";


                        return (
                            <div
                                key={item.likeId || itemId}
                                className={s.itemCard}
                                onClick={() => navigate(`/item/${itemId}`)}
                            >
                                <Link to={`/item/${itemId}`}>
                                    {thumbnailUrl ? (
                                        <img
                                            src={thumbnailUrl}
                                            alt={itemName}
                                            className={s.itemImage}
                                        />
                                    ) : (
                                        <div className={s.noImagePlaceholder}>이미지 없음</div>
                                    )}

                                    <div className={s.itemInfo}>
                                        <h3 className={s.itemName}>{itemName}</h3>
                                        <p className={s.itemPrice}>{priceText}</p>
                                        <div className={s.itemActions}>
                                            <button
                                                className={s.cartButton}
                                                onClick={(e) => {
                                                    e.preventDefault();
                                                    e.stopPropagation();
                                                    alert(`장바구니 추가: ${itemName}`);
                                                }}
                                                disabled={itemStatus !== 'SALE'}
                                            >
                                                장바구니 담기
                                            </button>
                                        </div>
                                    </div>
                                </Link>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}