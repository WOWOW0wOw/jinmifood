import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchAllItems, fetchItemsByCategoryId, fetchAllCategories } from '../../api/itemApi';
import styles from './css/ItemPage.module.css';

export default function ItemPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [categories, setCategories] = useState([]); // 카테고리 목록 상태
    const [selectedCategory, setSelectedCategory] = useState(null); // 선택된 카테고리 ID (null: 전체)
    const [isDropdownOpen, setIsDropdownOpen] = useState(false); // 드롭다운 메뉴 가시성
    const navigate = useNavigate();
    const dropdownRef = useRef(null);


    useEffect(() => {
        const handleClickOutside = (event) => {
            // 드롭다운이 열려있고, 클릭된 요소가 드롭다운 내부에 포함되지 않는 경우
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsDropdownOpen(false);
            }
        };

        // document에 이벤트 리스너 추가
        document.addEventListener('mousedown', handleClickOutside);

        // 컴포넌트 언마운트 시 이벤트 리스너 제거
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isDropdownOpen]); // isDropdownOpen이 변경될 때만 리스너를 다시 설정 (최적화)



    // 상품 목록 및 카테고리 목록을 불러오는 useEffect
    useEffect(() => {
        const loadPageData = async () => {
            setLoading(true);
            setError(null);
            try {
                // 1. 카테고리 목록 불러오기
                const fetchedCategories = await fetchAllCategories();
                setCategories([{ categoryId: null, categoryName: '전체 상품' }, ...fetchedCategories]); // '전체 상품' 옵션 추가

                // 2. 선택된 카테고리에 따라 상품 불러오기
                let data;
                if (selectedCategory === null) {
                    data = await fetchAllItems();
                } else {
                    data = await fetchItemsByCategoryId(selectedCategory);
                }
                setItems(data);
            } catch (err) {
                setError('현재 판매하는 상품이 없습니다.');
                console.error('Failed to load page data:', err);
            } finally {
                setLoading(false);
            }
        };

        loadPageData();
    }, [selectedCategory]); // selectedCategory가 변경될 때마다 재실행


    const handleCategorySelect = (categoryId) => {
        setSelectedCategory(categoryId);
        setIsDropdownOpen(false); // 카테고리 선택 후 드롭다운 닫기
    };

    const toggleDropdown = () => {
        setIsDropdownOpen(prev => !prev);
    };

    if (loading) {
        return <div className={styles.container}>상품 및 카테고리 정보를 불러오는 중...</div>;
    }

    if (error) {
        return <div className={styles.container}>
            <h2 className={styles.pageTitle}>상품 목록</h2>

            {/* 카테고리 드롭다운 박스 */}
            <div
                className={styles.categoryContainer}
                ref={dropdownRef} // ref 연결
            >
                <button
                    className={styles.categoryButton}
                    onClick={toggleDropdown}
                >
                    {categories.find(cat => cat.categoryId === selectedCategory)?.categoryName || '카테고리 선택'} ▼
                </button>
                <div className={`${styles.dropdownContent} ${isDropdownOpen ? styles.show : ''}`}>
                    {categories.map((cat) => (
                        <div
                            key={cat.categoryId === null ? 'all' : cat.categoryId}
                            className={styles.dropdownItem}
                            onClick={() => handleCategorySelect(cat.categoryId)}
                        >
                            {cat.categoryName}
                        </div>
                    ))}
                </div>
            </div>
            <div className={styles.container}><p className={styles.error}>{error}</p></div>
        </div>;

    }

    return (
        <div className={styles.container}>
            <h2 className={styles.pageTitle}>상품 목록</h2>

            {/* 카테고리 드롭다운 박스 */}
            <div
                className={styles.categoryContainer}
                ref={dropdownRef} // ref 연결
            >
                <button
                    className={styles.categoryButton}
                    onClick={toggleDropdown}
                >
                    {categories.find(cat => cat.categoryId === selectedCategory)?.categoryName || '카테고리 선택'} ▼
                </button>
                <div className={`${styles.dropdownContent} ${isDropdownOpen ? styles.show : ''}`}>
                    {categories.map((cat) => (
                        <div
                            key={cat.categoryId === null ? 'all' : cat.categoryId}
                            className={styles.dropdownItem}
                            onClick={() => handleCategorySelect(cat.categoryId)}
                        >
                            {cat.categoryName}
                        </div>
                    ))}
                </div>
            </div>

            {items.length === 0 ? (
                <p>등록된 상품이 없습니다.</p>
            ) : (
                <div className={styles.itemList}>
                    {items.map((item) => (
                        <div key={item.itemId} className={styles.itemCard}>
                            <img src={item.itemImg} alt={item.itemName} className={styles.itemImage} />
                            <div className={styles.itemInfo}>
                                <h3 className={styles.itemName}>{item.itemName}</h3>
                                <p className={styles.itemPrice}>{item.itemPrice.toLocaleString()} 원</p>
                                <p className={styles.itemStatus}>
                                    {item.status === 'SALE' ? '판매 중' : '품절'}
                                    {item.status === 'SALE' && ` (재고: ${item.count})`}
                                </p>
                                <div className={styles.itemActions}>
                                    <button
                                        className={styles.detailButton}
                                        onClick={() => navigate(`/item/${item.itemId}`)}
                                    >
                                        상세보기
                                    </button>
                                    <button
                                        className={styles.cartButton}
                                        onClick={() => alert(`장바구니 추가: ${item.itemName}`)}
                                        disabled={item.status !== 'SALE'}
                                    >
                                        장바구니 담기
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}