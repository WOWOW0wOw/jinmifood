export const CART_API = '/api/itemCart';

function authHeaders() {
    const token = localStorage.getItem('accessToken');
    return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function fetchCart() {
    const res = await fetch(`${CART_API}`, { headers: { ...authHeaders() } });
    if (!res.ok) throw new Error('장바구니 불러오기 실패');

    const body = await res.json();

    const raw = Array.isArray(body)
        ? body
        : body.data ?? body.result ?? body.content ?? body.items ?? [];

    // 프론트 표준 형태로 매핑
    const list = raw.map((x) => ({
        id: x.itemCartId ?? x.id ?? x.cartId,
        name: x.itemName ?? x.name,
        optionName: x.itemOption ?? x.optionName ?? x.option,
        qty: x.totalCnt ?? x.qty ?? x.totalCnt ?? 1,
        price: x.price ?? 0,
        // imageUrl: x.imageUrl ?? x.thumbnailUrl ?? null,
    }));

    return list;
}

export async function updateQuantity(cartId, qty) {
    if (qty > 100){
        qty = 100;
        throw new Error('최대 100개 주문가능');
    }
    console.log("cartid = " + cartId);
    console.log("quantity = " + qty);
    const res = await fetch(`/api/itemCart/update/${cartId}/${qty}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify({ qty }),
    });
    if (!res.ok) throw new Error('수량 변경 실패');
    return res.json();
}

export async function removeItem(cartId) {
    const res = await fetch(`${CART_API}/remove/${cartId}`, {
        method: 'POST',
        headers: { ...authHeaders() },
    });
    if (!res.ok) throw new Error('삭제 실패');
    return true;
}

export async function clearCart() {
    const res = await fetch(`${CART_API}/removeAll`, {
        method: 'POST',
        headers: { ...authHeaders() },
    });
    if (!res.ok) throw new Error('장바구니 비우기 실패');
    return true;
}
