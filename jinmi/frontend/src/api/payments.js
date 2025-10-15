// src/api/payments.js
function authHeaders({ requireAuth = false } = {}) {
    const token = localStorage.getItem('accessToken');
    if (!token) {
        if (requireAuth && typeof window !== 'undefined') {
            const next = encodeURIComponent(location.pathname + location.search);
            location.href = `/login?next=${next}`;
        }
        return {};
    }
    return { Authorization: `Bearer ${token}` };
}

// 결제 전 금액을 서버 세션에 임시 저장 (백엔드: POST /payments/saveAmount)
export async function tempSaveAmount(orderId, amount) {
    const res = await fetch('/api/payments/saveAmount', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders({ requireAuth: true }) },
        body: JSON.stringify({ orderId, amount: String(amount) }),
    });
    if (!res.ok) throw new Error('결제금액 임시 저장 실패');
    return res.json();
}
