import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import apiClient from '../../api/apiClient.js';

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
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [modalContent, setModalContent] = useState({ message: '', type: 'info', callback: null });

    const currentUserId = user?.userId;

    // 디버깅 강화: Context에서 받아온 값들을 콘솔에 출력
    useEffect(() => {
        console.log("--- Auth Context Status ---");
        console.log("isLoggedIn:", isLoggedIn);
        console.log("User Object:", user);
        console.log("currentUserId (resolved):", currentUserId);
        console.log("---------------------------");
    }, [isLoggedIn, user, currentUserId]);
    // **********************************************

    const [authReady, setAuthReady] = useState(false);

    const loadOrders = async () => {
        if (!currentUserId || !authReady) {
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);

            const response = await apiClient.get('/order/list', {
                params: { userId: currentUserId}
            });

            console.log("--- API Response Data 200 OK ---", response.data);

            const { status, data, message } = response.data;

            if (status === 'OK' || status === 200) {
                if (Array.isArray(data)) {
                    data.sort((a, b) => new Date(b.orderTime) - new Date(a.orderTime));
                    setOrders(data);
                } else {
                    // data가 배열이 아닐 경우 (e.g., null 또는 Object), 빈 배열로 처리하고 경고
                    console.warn("API 응답의 'data' 필드가 배열이 아닙니다. 주문 내역이 표시되지 않을 수 있습니다.", data);
                    setOrders([]);
                }
            } else {
                throw new Error(`[Server Status: ${status}] ${message || '서버에서 예상치 못한 응답을 보냈습니다.'}`);
            }

        } catch (err) {
            console.error("주문 내역 로드 에러 상세:", err);

            let errMsg = '서버 통신 오류. (백엔드 서버 주소 및 실행 상태 확인 필요)';
            if (err.response) {
                errMsg = err.response.data?.message || `API 요청 실패: ${err.response.status} ${err.response.statusText}`;
            } else if (err.request) {
                errMsg = '서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지, URL이 올바른지 확인하세요.';
            }

            setError(`[USER ID: ${currentUserId}] ${errMsg}`);
        } finally {
            setLoading(false);
        }
    };

    // 인증 상태 확인
    useEffect(() => {
        if (isLoggedIn !== null) {
            setAuthReady(true);
        }
    }, [isLoggedIn]);

    // 주문 내역 로드
    useEffect(() => {
        const shouldLoad = authReady && isLoggedIn && currentUserId;

        if (shouldLoad) {
            loadOrders();
        } else if (authReady) {
            setLoading(false);
        }

    }, [isLoggedIn, currentUserId, authReady]);


    // 주문 취소 로직
    const handleConfirmCancel = (orderId) => {
        setModalContent({
            message: '정말 주문을 취소하시겠습니까?',
            type: 'confirm',
            callback: async () => {
                try {
                    if (!currentUserId) throw new Error("로그인된 사용자 정보가 없어 취소할 수 없습니다.");

                    // 주문 취소 API 호출
                    const response = await apiClient.post('/order/remove', null, {
                        params: { userId: currentUserId, orderId }
                    });

                    const { status, message } = response.data;
                    if (status !== 'OK' && status !== 200) {
                        throw new Error(message || '주문 취소 실패');
                    }

                    setModalContent({
                        message: '취소되었습니다.',
                        type: 'success',
                        callback: () => { setShowModal(false); loadOrders(); }
                    });
                } catch (e) {
                    console.error("주문 취소 에러:", e);
                    const errorMessage = e.response?.data?.message || e.message || '주문 취소에 실패했습니다.';
                    setModalContent({
                        message: errorMessage,
                        type: 'error',
                        callback: () => setShowModal(false)
                    });
                }
            }
        });
        setShowModal(true);
    };

    const formatCurrency = (amount) => new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(amount);

    const getStatusStyle = (status) => {
        if (status === 'DELIVERED') return 'bg-green-100 text-green-800';
        if (status === 'IN_TRANSIT') return 'bg-blue-100 text-blue-800';
        if (['CANCELLED', 'REFUNDED'].includes(status)) return 'bg-red-100 text-red-800 line-through';
        return 'bg-gray-100 text-gray-700';
    };

    if (!authReady || loading) return <div className="p-10 text-center">주문 내역을 확인하는 중...</div>;

    if (!isLoggedIn || !currentUserId) {
        return <div className="p-10 text-center">로그인이 필요합니다.</div>;
    }

    if (error) {
        return (
            <div className="p-10 text-center border-2 border-red-300 bg-red-50 rounded-lg max-w-lg mx-auto">
                <p className="text-red-500 font-bold mb-2">오류가 발생했습니다</p>
                <p className="text-gray-600 break-words text-sm">{error}</p>
                <button
                    onClick={loadOrders}
                    className="mt-4 px-4 py-2 bg-indigo-500 text-white rounded hover:bg-indigo-600 transition"
                >
                    다시 시도
                </button>
            </div>
        );
    }

    const displayUserId = currentUserId;

    return (
        <div className="container mx-auto p-4 max-w-4xl">
            <h1 className="text-2xl font-bold mb-6 border-b pb-4">주문 내역 (User ID: {displayUserId})</h1>

            {orders.length === 0 ? (
                <div className="text-center py-10 text-gray-500 border rounded-lg bg-gray-50">
                    주문 내역이 없습니다.
                </div>
            ) : (
                <div className="space-y-4">
                    {orders.map((order) => (
                        <div key={order.orderId + (order.orderCode || '')} className="border rounded-lg p-4 shadow-sm bg-white hover:shadow-md transition">
                            <div className="flex justify-between border-b pb-2 mb-2">
                                <span className="font-bold text-gray-700 text-sm">{order.orderTime}</span>
                                <div className="flex items-center gap-2">
                                    <span className="text-sm text-gray-500">{order.orderCode}</span>
                                    <span className={`px-2 py-1 rounded text-xs font-bold ${getStatusStyle(order.orderStatus)}`}>
                                        {mapOrderStatus(order.orderStatus)}
                                    </span>
                                </div>
                            </div>
                            <div className="flex gap-4 items-center">
                                <img
                                    src={order.itemImg || "https://placehold.co/100"}
                                    alt={order.itemName}
                                    className="w-16 h-16 object-cover rounded bg-gray-100 flex-shrink-0"
                                    onError={(e) => { e.target.onerror = null; e.target.src="https://placehold.co/100" }}
                                />
                                <div>
                                    <h3 className="font-bold text-lg">{order.itemName}</h3>
                                    <p className="text-sm text-gray-600">{order.itemOption}</p>
                                    <p className="font-bold text-indigo-600 mt-1">{formatCurrency(order.totalPrice)} ({order.qty}개)</p>
                                </div>
                            </div>
                            <div className="flex justify-end mt-3 gap-2 border-t pt-2">
                                {(order.orderStatus === 'PENDING' || order.orderStatus === 'CONFIRMED') && (
                                    <button onClick={() => handleConfirmCancel(order.orderId)} className="px-3 py-1 bg-red-100 text-red-600 rounded text-sm hover:bg-red-200 transition">주문 취소</button>
                                )}
                                <button className="px-3 py-1 bg-gray-100 text-gray-600 rounded text-sm hover:bg-gray-200 transition">상세 보기</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* 모달 */}
            {showModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white p-6 rounded shadow-lg max-w-sm w-full">
                        <p className="mb-4 font-medium">{modalContent.message}</p>
                        <div className="flex justify-end gap-2">
                            {modalContent.type === 'confirm' ? (
                                <>
                                    <button onClick={() => setShowModal(false)} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300">닫기</button>
                                    <button onClick={modalContent.callback} className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600">확인</button>
                                </>
                            ) : (
                                <button onClick={modalContent.callback} className="px-4 py-2 bg-indigo-500 text-white rounded hover:bg-indigo-600">확인</button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default OrderPage;