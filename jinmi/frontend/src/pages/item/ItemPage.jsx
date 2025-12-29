import React, { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchAllItems, fetchItemsByCategoryId, fetchAllCategories } from "../../api/itemApi";
import HeroCarousel from "../item/RecommendHero.js"; // 경로 프로젝트에 맞게 조정
import s from "../item/css/ItemPage.module.css";

export default function ItemPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [categories, setCategories] = useState([
        { categoryId: null, categoryName: "전체 상품" },
    ]);
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        const handleOut = (e) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
                setIsDropdownOpen(false);
            }
        };
        document.addEventListener("mousedown", handleOut);
        return () => document.removeEventListener("mousedown", handleOut);
    }, []);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            setError(null);
            try {
                const cats = await fetchAllCategories();
                setCategories([{ categoryId: null, categoryName: "전체 상품" }, ...cats]);

                const data =
                    selectedCategory === null
                        ? await fetchAllItems()
                        : await fetchItemsByCategoryId(selectedCategory);
                console.log("API에서 받아온 전체 상품 데이터:", data);

                setItems(data || []);
            } catch (e) {
                setError("현재 판매하는 상품이 없습니다.");
                console.error(e);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [selectedCategory]);

    // 캐러셀 슬라이드 데이터
    const slides = useMemo(() => {
        const top = (items || []).slice(0, 5);
        if (!top.length) return [];
        return top.map((it) => ({
            bg: it.mainImageUrl,
            title: it.itemName,
            desc: it.itemPrice
                ? `${Number(it.itemPrice).toLocaleString()}원 · ${
                    it.status === "SALE" ? "판매 중" : "품절"
                }`
                : "",
            badge: it.categoryName || "추천",
            ctaPrimary: { label: "자세히 보기", type: "detail", payload: it.itemId },
            ctaGhost:
                it.status === "SALE"
                    ? { label: "바로 구매", type: "buy", payload: it.itemId }
                    : null,
        }));
    }, [items]);

    // 캐러셀 CTA
    const onHeroAction = (cta) => {
        if (!cta) return;
        if (cta.type === "detail") navigate(`/item/${cta.payload}`);
        if (cta.type === "buy") navigate(`/item/${cta.payload}?buy=1`);
    };

    const handleCategorySelect = (id) => {
        setSelectedCategory(id);
        setIsDropdownOpen(false);
    };

    const handleCartClick = (e, itemName) => {
        e.stopPropagation();
        alert(`장바구니 추가: ${itemName}`);
    };

    if (loading) {
        return (
            <div className={s.container}>
                <div className={s.skeletonHero} />
                <div className={s.skeletonGrid}>
                    {Array.from({ length: 8 }).map((_, i) => (
                        <div key={i} className={s.skeletonCard} />
                    ))}
                </div>
            </div>
        );
    }

    return (
        <div className={s.container}>
            {/* 히어로 캐러셀 */}
            <HeroCarousel slides={slides} onAction={onHeroAction} />

            {/* 카테고리 바 */}
            <div className={s.categoryBarWrap}>
                <div className={s.categoryBar}>
                    {categories.map((c) => (
                        <button
                            key={c.categoryId === null ? "all" : c.categoryId}
                            className={`${s.categoryChip} ${
                                selectedCategory === c.categoryId ? s.active : ""
                            }`}
                            onClick={() => handleCategorySelect(c.categoryId)}
                        >
                            {c.categoryName}
                        </button>
                    ))}
                </div>

                <div className={s.categoryContainer} ref={dropdownRef}>
                    <button
                        className={s.categoryButton}
                        onClick={() => setIsDropdownOpen((v) => !v)}
                    >
                        {categories.find((cat) => cat.categoryId === selectedCategory)
                            ?.categoryName || "카테고리 선택"}{" "}
                        ▼
                    </button>
                    <div
                        className={`${s.dropdownContent} ${
                            isDropdownOpen ? s.show : ""
                        }`}
                    >
                        {categories.map((cat) => (
                            <div
                                key={cat.categoryId === null ? "all" : cat.categoryId}
                                className={s.dropdownItem}
                                onClick={() => handleCategorySelect(cat.categoryId)}
                            >
                                {cat.categoryName}
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* 리스트 */}
            {error ? (
                <p className={s.error}>{error}</p>
            ) : items.length === 0 ? (
                <p className={s.error}>등록된 상품이 없습니다.</p>
            ) : (
                <div className={s.itemList}>
                    {items.map((item) => {
                        const thumbnailUrl = item.mainImageUrl;
                        const priceText =
                            item.status === "SALE"
                                ? `${(item.itemPrice || 0).toLocaleString()}원`
                                : "품절";

                        return (
                            <div
                                key={item.itemId}
                                className={s.itemCard}
                                onClick={() => navigate(`/item/${item.itemId}`)}
                            >
                                {thumbnailUrl ? (
                                    <img
                                        src={thumbnailUrl}
                                        alt={item.itemName}
                                        className={s.itemImage}
                                    />
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
            )}
        </div>
    );
}
