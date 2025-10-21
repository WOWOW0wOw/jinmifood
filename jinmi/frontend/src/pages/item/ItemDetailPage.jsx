// ItemDetailPage.jsx 파일 예시

import React from 'react';
import { useParams } from 'react-router-dom';

export default function ItemDetailPage() {
    const { itemId } = useParams();

    return (
        <div>
            <h1>상품 상세 정보</h1>
            <p>현재 보고 있는 상품의 ID는 {itemId} 입니다.</p>
        </div>
    );
}