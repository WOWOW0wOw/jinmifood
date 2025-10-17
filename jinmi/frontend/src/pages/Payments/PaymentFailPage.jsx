// src/pages/Payments/PaymentFailPage.jsx
import { useSearchParams, useNavigate } from 'react-router-dom';

export default function PaymentFailPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const code = searchParams.get('code');
    const message = searchParams.get('message');

    return (
        <div style={{ padding: 24 }}>
            <h1>결제 실패</h1>
            {code && <p>코드: {code}</p>}
            {message && <p>사유: {decodeURIComponent(message)}</p>}
            <button onClick={() => navigate('/cart')}>장바구니로</button>
        </div>
    );
}
