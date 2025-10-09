// 모든 장바구니 API 호출이 여기로 모입니다.
// ↓↓↓ 이 한 줄만 너희 백엔드 엔드포인트에 맞게 바꿔주세요.
export const CART_API = '/api/itemCart'; // 예) '/api/v1/cart' , '/api/item-cart' 등

function authHeaders() {
    const token = localStorage.getItem('accessToken'); // 로그인 시 저장한 액세스 토큰 키와 맞추세요
    return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function fetchCart() {
    const res = await fetch(`${CART_API}`, { headers: { ...authHeaders() } });
    if (!res.ok) throw new Error('장바구니 불러오기 실패');
    return res.json();
}

export async function updateQuantity(itemCartId, quantity) {
    // 백엔드 시그니처에 맞게 변경: PUT /cart/{id} 바디 {quantity}
    const res = await fetch(`${CART_API}/${itemCartId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify({ quantity }),
    });
    if (!res.ok) throw new Error('수량 변경 실패');
    return res.json();
}

export async function removeItem(itemCartId) {
    const res = await fetch(`${CART_API}/${itemCartId}`, {
        method: 'DELETE',
        headers: { ...authHeaders() },
    });
    if (!res.ok) throw new Error('삭제 실패');
    return true;
}

export async function clearCart() {
    // 백엔드에 전체삭제가 있으면 DELETE /cart (또는 /cart/clear) 로 맞춰주세요.
    const res = await fetch(`${CART_API}`, {
        method: 'DELETE',
        headers: { ...authHeaders() },
    });
    if (!res.ok) throw new Error('장바구니 비우기 실패');
    return true;
}
