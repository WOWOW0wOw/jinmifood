import React, { useEffect, useMemo, useState } from 'react';
import { fetchCart, updateQuantity, removeItem, clearCart } from '../../api/itemCart.js';
import './itemCart.css';

export default function CartPage() {
    const [items, setItems] = useState([]); // [{ id, name, optionName, qty, price, imageUrl }]
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [checked, setChecked] = useState(() => new Set()); // 선택된 id 집합

    useEffect(() => {
        (async () => {
            try {
                setLoading(true);
                setError('');
                const list = await fetchCart(); // ← 이제 항상 표준화된 배열이 옴
                setItems(list);
            } catch (e) {
                setError(e.message || '불러오기 에러');
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    const allChecked = items.length > 0 && checked.size === items.length;

    const selectedItems = useMemo(
        () => items.filter((i) => checked.has(i.id)),
        [items, checked]
    );

    const totalCount = useMemo(
        () => selectedItems.reduce((acc, cur) => acc + (cur.qty ?? 1), 0),
        [selectedItems]
    );

    const totalPrice = useMemo(
        () => selectedItems.reduce((acc, cur) => acc + (cur.price || 0) * (cur.qty ?? 1), 0),
        [selectedItems]
    );

    function toggleAll() {
        if (allChecked) {
            setChecked(new Set());
        } else {
            setChecked(new Set(items.map((i) => i.id)));
        }
    }

    function toggleOne(id) {
        setChecked((prev) => {
            const next = new Set(prev);
            next.has(id) ? next.delete(id) : next.add(id);
            return next;
        });
    }

    async function handlePlus(item) {
        const nextQty = (item.qty ?? 1) + 1;
        await handleUpdateQty(item.id, nextQty);
    }

    async function handleMinus(item) {
        const nextQty = Math.max(1, (item.qty ?? 1) - 1);
        await handleUpdateQty(item.id, nextQty);
    }

    async function handleUpdateQty(id, qty) {
        console.log("id = " + id)
        try {
            // Optimistic UI
            setItems((prev) => prev.map((i) => (i.id === id ? { ...i, qty: qty } : i)));
            await updateQuantity(id, qty);
        } catch (e) {
            setError(e.message || '수량 변경 실패');
            // 실패 시 서버 데이터로 동기화
            const list = await fetchCart();
            setItems(list);
        }
    }

    async function handleRemove(id) {
        if (!confirm('해당 상품을 삭제할까요?')) return;
        try {
            await removeItem(id);
            setItems((prev) => prev.filter((i) => i.id !== id));
            setChecked((prev) => {
                const next = new Set(prev);
                next.delete(id);
                return next;
            });
        } catch (e) {
            setError(e.message || '삭제 실패');
        }
    }

    async function handleClear() {
        if (!confirm('장바구니를 모두 비우시겠어요?')) return;
        try {
            await clearCart();
            setItems([]);
            setChecked(new Set());
        } catch (e) {
            setError(e.message || '비우기 실패');
        }
    }

    function handleOrder() {
        if (selectedItems.length === 0) {
            alert('주문할 상품을 선택해주세요.');
            return;
        }
        const ids = selectedItems.map((i) => i.id).join(',');
        window.location.href = `/order?cartIds=${ids}`;
    }

    if (loading) return <div className="cart-container">불러오는 중...</div>;

    return (
        <div className="cart-container">
            <h1 className="cart-title">장바구니</h1>

            {error && <div className="cart-error">{error}</div>}

            {items.length === 0 ? (
                <div className="cart-empty">
                    장바구니가 비었습니다.
                    <a href="/products" className="btn primary">상품 보러가기</a>
                </div>
            ) : (
                <>
                    <div className="cart-actions">
                        <label className="chk">
                            <input type="checkbox" checked={allChecked} onChange={toggleAll} />
                            <span>전체선택</span>
                        </label>
                        <button className="btn" onClick={handleClear}>장바구니 비우기</button>
                    </div>

                    <div className="cart-table">
                        <div className="cart-thead">
                            <div className="col col-chk" />
                            <div className="col col-item">상품정보</div>
                            <div className="col col-price">가격</div>
                            <div className="col col-qty">수량</div>
                            <div className="col col-sum">합계</div>
                            <div className="col col-act">관리</div>
                        </div>

                        {items.map((item) => {
                            const qty = item.qty ?? 1;
                            const sum = (item.price || 0) * qty;

                            return (
                                <div className="cart-row" key={item.id}>
                                    <div className="col col-chk">
                                        <input
                                            type="checkbox"
                                            checked={checked.has(item.id)}
                                            onChange={() => toggleOne(item.id)}
                                        />
                                    </div>

                                    <div className="col col-item">
                                        <div className="item-wrap">
                                            <div className="item-info">
                                                <div className="name">{item.name}</div>
                                                {item.optionName && <div className="opt">옵션: {item.optionName}</div>}
                                            </div>
                                        </div>
                                    </div>

                                    <div className="col col-price">{(item.price || 0).toLocaleString()}원</div>

                                    <div className="col col-qty">
                                        <div className="qty">
                                            <button onClick={() => handleMinus(item)}>-</button>
                                            <input
                                                value={qty}
                                                onChange={(e) => {
                                                    const v = Math.max(1, Number(e.target.value) || 1);
                                                    handleUpdateQty(item.id, v);
                                                }}
                                            />
                                            <button onClick={() => handlePlus(item)}>+</button>
                                        </div>
                                    </div>

                                    <div className="col col-sum">{sum.toLocaleString()}원</div>

                                    <div className="col col-act">
                                        <button className="btn danger" onClick={() => handleRemove(item.id)}>
                                            삭제
                                        </button>
                                    </div>
                                </div>
                            );
                        })}
                    </div>

                    <div className="cart-total">
                        <div className="line">
                            <span>선택 상품 수</span>
                            <b>{totalCount}개</b>
                        </div>
                        <div className="line">
                            <span>총 결제금액</span>
                            <b className="price">{totalPrice.toLocaleString()}원</b>
                        </div>
                        <div className="order-actions">
                            <a href="/products" className="btn">계속 쇼핑하기</a>
                            <button className="btn primary" onClick={handleOrder}>주문하기</button>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
}
