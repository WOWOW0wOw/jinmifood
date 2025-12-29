// ItemSearchPage.jsx
import React, { useEffect, useState, useMemo } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { fetchItemsBySearch } from "../../api/itemApi";
import HeroCarousel from "../item/RecommendHero.js";
import s from "../item/css/ItemPage.module.css";

export default function ItemSearchPage() {
    const [searchParams] = useSearchParams();
    const query = searchParams.get("query") || "";

    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            setError(null);
            try {
                if (!query) throw new Error("검색어를 입력해주세요.");
                const data = await fetchItemsBySearch(query);
                setItems(data || []);
            } catch (e) {
                setError(e.message || "검색 결과가 없습니다.");
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [query]);

    // 캐러셀: 상위 5개
    const slides = useMemo(() => {
        return (items || []).slice(0, 5).map(item => ({
            bg: item.mainImageUrl,
            title: item.itemName,
            desc: `${item.itemPrice.toLocaleString()}원`,
            badge: item.status === "SALE" ? "인기" : "품절",
            ctaPrimary: { label: "상세보기", type: "navigate", payload: `/item/${item.itemId}` }
        }));
    }, [items]);

    const onAction = (action) => {
        if (action.type === "navigate") navigate(action.payload);
    };

    const handleCartClick = (e, name) => {
        e.stopPropagation();
        alert(`${name}을 장바구니에 담았습니다.`);
    };

    if (loading) {
        return (
            <div className={s.container}>
                <div className={s.skeletonHero} />
                <div className={s.skeletonGrid}>
                    {Array.from({ length: 8 }).map((_, i) => <div key={i} className={s.skeletonCard} />)}
                </div>
            </div>
        );
    }

    if (error) {
        return <div className={s.container}><p className={s.errorText}>{error}</p></div>;
    }

    return (
        <div className={s.container}>
            <h2 className={s.searchTitle}>"{query}" 검색 결과 ({items.length}개)</h2>

            {slides.length > 0 && <HeroCarousel slides={slides} onAction={onAction} />}

            <div className={s.itemList}>
                {items.map(item => {
                    const priceText = item.status === "SALE"
                        ? `${item.itemPrice.toLocaleString()}원`
                        : "품절";

                    return (
                        <div key={item.itemId} className={s.itemCard} onClick={() => navigate(`/item/${item.itemId}`)}>
                            {item.mainImageUrl ? (
                                <img src={item.mainImageUrl} alt={item.itemName} className={s.itemImage} />
                            ) : (
                                <div className={s.noImagePlaceholder}>이미지 없음</div>
                            )}
                            <div className={s.itemInfo}>
                                <h3 className={s.itemName}>{item.itemName}</h3>
                                <p className={s.itemPrice}>{priceText}</p>
                                <div className={s.itemActions}>
                                    <button
                                        className={s.cartButton}
                                        onClick={(e) => handleCartClick(e, item.itemName)}
                                        disabled={item.status !== "SALE"}
                                    >
                                        장바구니 담기
                                    </button>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}