// src/pages/Payments/PaymentSuccessPage.jsx
import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { confirmPayment } from '../../api/payments';

export default function PaymentSuccessPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const orderId = searchParams.get('orderId');
    const paymentKey = searchParams.get('paymentKey');
    // amount는 Toss 리다이렉트에 있지만 서버 검증은 DB 기준이므로 굳이 보내지 않음

    const [statusText, setStatusText] = useState('승인 처리 중...');
    const [result, setResult] = useState(null);

    useEffect(() => {
        (async () => {
            try {
                if (!orderId || !paymentKey) {
                    throw new Error('유효하지 않은 결제 성공 콜백입니다.');
                }
                const data = await confirmPayment({ orderId, paymentKey });
                setResult(data);
                setStatusText('승인 완료');
                // 필요하면 주문완료 페이지로 이동:
                // navigate(`/orders/${orderId}`);
            } catch (e) {
                setStatusText(e.message || '승인 오류');
            }
        })();
    }, [orderId, paymentKey]);

    return (
        <div style={{ padding: 24 }}>
            <h1>결제 성공</h1>
            <p>orderId: {orderId}</p>
            <p>상태: {statusText}</p>

            {result && (
                <div style={{ marginTop: 12 }}>
                    <div>결제상태: {result.status}</div>
                    <div>결제금액: {Number(result.price ?? 0).toLocaleString()}원</div>
                    {result.method && <div>결제수단: {result.method}</div>}
                    {result.receiptUrl && (
                        <div>
                            영수증: <a href={result.receiptUrl} target="_blank" rel="noreferrer">열기</a>
                        </div>
                    )}
                    {result.approvedAt && <div>승인시각: {result.approvedAt}</div>}
                </div>
            )}

            <div style={{ marginTop: 16 }}>
                <button onClick={() => navigate('/cart')}>장바구니로</button>
            </div>
        </div>
    );
}
