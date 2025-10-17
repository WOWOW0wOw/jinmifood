// src/api/payments.js
export function authHeaders({ requireAuth = false } = {}) {
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

function unwrap(json) {
    if (!json) return null;
    return json.data ?? json.result ?? json.content ?? json.items ?? json;
}

/** 결제 준비 (서버가 orderId/amount 확정) */
export async function preparePayment({ cartIds, orderName }) {
    const res = await fetch('/api/payments/prepare', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders({ requireAuth: true }) },
        body: JSON.stringify({ cartIds, orderName }),
    });
    const json = await res.json().catch(() => ({}));
    if (!res.ok) {
        throw new Error(json?.message || '결제 준비 실패');
    }
    return unwrap(json); // { orderId, amount, orderName }
}

/** 결제 승인(성공 콜백에서 호출) — amount는 보내지 않음 */
export async function confirmPayment({ orderId, paymentKey }) {
    const res = await fetch('/api/payments/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders({ requireAuth: true }) },
        body: JSON.stringify({ orderId, paymentKey }),
    });
    const json = await res.json().catch(() => ({}));
    if (!res.ok) {
        throw new Error(json?.message || '결제 승인 실패');
    }
    return unwrap(json); // { orderId, paymentKey, price, status, ... }
}

/** 결제 취소(옵션) */
export async function cancelPayment({ paymentKey, cancelReason }) {
    const res = await fetch('/api/payments/cancel', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders({ requireAuth: true }) },
        body: JSON.stringify({ paymentKey, cancelReason }),
    });
    const json = await res.json().catch(() => ({}));
    if (!res.ok) {
        throw new Error(json?.message || '결제 취소 실패');
    }
    return unwrap(json); // "CANCELED"
}
