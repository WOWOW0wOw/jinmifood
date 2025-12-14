import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom'; //
import { useAuth } from '../../context/AuthContext';
import apiClient from '../../api/apiClient.js';
import './css/OrderPage.css';

const mapOrderStatus = (status) => {
    switch (status) {
        case 'PENDING': return '결제 대기';
        case 'CONFIRMED': return '주문 확인';
        case 'CANCELLED': return '취소 완료';
        case 'FINISHED': return '거래 완료';
        case 'REJECTED': return '주문 거부';
        case 'DELIVERED': return '배송 완료';
        case 'RETURNED': return '반품/교환';
        case 'REFUNDED': return '환불 완료';
        case 'PAYMENT_FAILED': return '결제 실패';
        case 'IN_TRANSIT': return '배송 중';
        default: return status;
    }
};

const OrderPage = () => {
    const { isLoggedIn, user } = useAuth();
    const navigate = useNavigate();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [modalContent, setModalContent] = useState({ message: '', type: 'info', callback: null });
    const [authReady, setAuthReady] = useState(false);

    const currentUserId = user?.userId;

    const loadOrders = async () => {
        if (!currentUserId || !authReady) {
            setLoading(false);
            return;
        }
        try {
            setLoading(true);
            const response = await apiClient.get('/order/list', { params: { userId: currentUserId } });
            const { status, data } = response.data;
            if (status === 'OK' || status === 200) {
                const sortedData = Array.isArray(data) ? [...data].sort((a, b) => new Date(b.orderTime) - new Date(a.orderTime)) : [];
                setOrders(sortedData);
            }
        } catch (err) {
            setError('주문 내역을 불러오는 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { if (isLoggedIn !== null) setAuthReady(true); }, [isLoggedIn]);
    useEffect(() => { if (authReady && isLoggedIn && currentUserId) loadOrders(); else if (authReady) setLoading(false); }, [isLoggedIn, currentUserId, authReady]);

    const handleConfirmCancel = (orderId) => {
        setModalContent({
            message: '정말 주문을 취소하시겠습니까?',
            type: 'confirm',
            callback: async () => {
                try {
                    const response = await apiClient.post('/order/remove', null, { params: { userId: currentUserId, orderId } });
                    if (response.data.status === 'OK') {
                        setModalContent({ message: '취소되었습니다.', type: 'success', callback: () => { setShowModal(false); loadOrders(); } });
                    }
                } catch (e) {
                    setModalContent({ message: '취소에 실패했습니다.', type: 'error', callback: () => setShowModal(false) });
                }
            }
        });
        setShowModal(true);
    };

    const formatCurrency = (amount) => new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(amount);

    const getStatusStyle = (status) => {
        if (status === 'DELIVERED') return 'bg-green-100 text-green-800';
        if (status === 'IN_TRANSIT') return 'bg-blue-100 text-blue-800';
        if (['CANCELLED', 'REFUNDED'].includes(status)) return 'bg-red-50 text-red-500';
        return 'bg-gray-100 text-gray-700';
    };

    if (!authReady || loading) return <div className="p-10 text-center">주문 내역 로딩 중...</div>;
    if (!isLoggedIn) return <div className="p-10 text-center">로그인이 필요합니다.</div>;

    return (
        <div className="order-container">
            <h1 className="order-title">주문 내역</h1>

            {orders.length === 0 ? (
                <div className="text-center py-20 border rounded-xl bg-gray-50 text-gray-400">주문 내역이 없습니다.</div>
            ) : (
                <div className="order-list">
                    {orders.map((order) => (
                        <div key={order.orderId} className="order-card">
                            <div className="order-header">
                                <span className="order-date">{order.orderTime?.split('T')[0] || order.orderTime}</span>
                                <span className={`order-status-badge ${getStatusStyle(order.orderStatus)}`}>
                                    {mapOrderStatus(order.orderStatus)}
                                </span>
                            </div>

                            <div
                                className="order-content clickable"
                                onClick={() => navigate(`/item/${order.itemId}`)}
                            >
                                <img
                                    src={order.itemImg || "https://placehold.co/100"}
                                    className="order-image"
                                    alt={order.itemName}
                                />
                                <div className="order-info">
                                    <h3 className="order-item-name">{order.itemName}</h3>
                                    <p className="order-item-option">{order.itemOption}</p>
                                    <p className="order-price-qty">
                                        {formatCurrency(order.totalPrice)} <span className="font-normal text-gray-400">/ {order.qty}개</span>
                                    </p>
                                </div>
                            </div>

                            <div className="order-actions">
                                {(order.orderStatus === 'PENDING' || order.orderStatus === 'CONFIRMED') && (
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            handleConfirmCancel(order.orderId);
                                        }}
                                        className="btn-cancel"
                                    >
                                        주문 취소
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <p className="mb-6 font-semibold text-gray-800">{modalContent.message}</p>
                        <div className="flex justify-center gap-2">
                            {modalContent.type === 'confirm' ? (
                                <>
                                    <button onClick={() => setShowModal(false)} className="px-5 py-2 bg-gray-100 rounded-lg font-bold">닫기</button>
                                    <button onClick={modalContent.callback} className="px-5 py-2 bg-red-500 text-white rounded-lg font-bold">확인</button>
                                </>
                            ) : (
                                <button onClick={modalContent.callback} className="px-8 py-2 bg-indigo-600 text-white rounded-lg font-bold">확인</button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default OrderPage;