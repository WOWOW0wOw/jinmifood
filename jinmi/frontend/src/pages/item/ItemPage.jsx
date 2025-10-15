import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchAllItems } from '../../api/itemApi'; // 상품 API
import styles from './css/ItemPage.module.css';

export default function ItemPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const getItems = async () => {
            try {
                const data = await fetchAllItems();
                setItems(data);
            } catch (err) {
                setError('상품 목록을 불러오는 데 실패했습니다.');
                console.error('Failed to fetch items:', err);
            } finally {
                setLoading(false);
            }
        };

        getItems();
    }, []);

    if (loading) {
        return <div className={styles.container}>상품 목록을 불러오는 중...</div>;
    }

    if (error) {
        return <div className={styles.container}><p className={styles.error}>{error}</p></div>;
    }

    if (items.length === 0) {
        return <div className={styles.container}><p>등록된 상품이 없습니다.</p></div>;
    }

    return (
        <div className={styles.container}>
            <h2 className={styles.pageTitle}>전체 상품</h2>
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
                                    onClick={() => alert(`상세보기: ${item.itemName}`)} // 실제 상세 페이지로 이동 로직 필요
                                >
                                    상세보기
                                </button>
                                <button
                                    className={styles.cartButton}
                                    onClick={() => alert(`장바구니 추가: ${item.itemName}`)} // 실제 장바구니 추가 로직 필요
                                    disabled={item.status !== 'SALE'}
                                >
                                    장바구니 담기
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );

};