// src/pages/Payments/PaymentSuccessPage.jsx
import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

// 필요하면 공용 authHeaders()를 가져다 쓰세요
function authHeaders() {
    const token = localStorage.getItem('accessToken');
    return token ? { Authorization: `Bearer ${token}` } : {};
}

export default function PaymentSuccessPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const orderId = searchParams.get('orderId');
    const paymentKey = searchParams.get('paymentKey');
    const amount = searchParams.get('amount');
    const [status, setStatus] = useState('승인 처리 중...');

    useEffect(() => {
        (async () => {
            try {
                // 백엔드 결제 승인 호출
                const res = await fetch('/api/payments/confirm', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', ...authHeaders() },
                    body: JSON.stringify({ orderId, paymentKey, amount }),
                });

                const data = await res.json();
                if (!res.ok) {
                    const msg = data?.message || '결제 승인 실패';
                    throw new Error(msg);
                }

                setStatus('승인 완료');
                // 필요하면 주문완료 페이지로 이동
                // navigate(`/orders/${orderId}`);
            } catch (e) {
                setStatus(e.message || '승인 오류');
            }
        })();
    }, [orderId, paymentKey, amount, navigate]);

    return (
        <div style={{ padding: 24 }}>
            <h1>결제 성공</h1>
            <p>orderId: {orderId}</p>
            <p>amount: {amount}</p>
            <p>상태: {status}</p>
            <button onClick={() => navigate('/cart')}>장바구니로</button>
        </div>
    );
}
