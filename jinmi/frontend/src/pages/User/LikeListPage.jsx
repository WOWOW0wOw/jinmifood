import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import apiClient from "../../api/apiClient.js";

export default function LikeListPage() {
    const [likeItems, setLikeItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const { accessToken, isLoading: isAuthLoading } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {

        const localAccessToken = localStorage.getItem('accessToken');
        const tokenToUse = accessToken || localAccessToken;

        console.log('--- useEffect 찜 목록 실행 (수정된 로직) ---');
        console.log('isAuthLoading:', isAuthLoading);
        console.log('tokenToUse 존재 여부 (로컬 포함):', !!tokenToUse);

        if (!tokenToUse) {
            console.error("fetchLikes: Access Token이 없어 API 호출을 건너뜁니다.");
            setError("로그인이 필요합니다.");
            setLoading(false);
            return;
        }

        const fetchLikes = async () => {
            console.log('fetchLikes 함수 실행 시도');

            console.log("Access Token 유효 확인. 찜 목록 API 호출 시작.");

            try {
                const response = await apiClient.get("/likes/myList");

                if (response.data && response.data.data) {
                    setLikeItems(response.data.data);
                    console.log(`찜 목록 로드 성공. 항목 개수: ${response.data.data.length}`);
                } else {
                    setLikeItems([]);
                    console.log("찜 목록 로드 성공했으나, 데이터가 비어있습니다.");
                }
            } catch (err) {
                console.error("찜 목록 로드 실패 (API 호출 에러):", err);

                if (err.response && err.response.status === 401) {
                    setError("세션이 만료되었습니다. 다시 로그인해 주세요.");
                } else {
                    setError("찜 목록을 불러오는 데 실패했습니다.");
                }
            } finally {
                setLoading(false);
            }
        };

        fetchLikes();

    }, [accessToken, isAuthLoading]);

    if (loading) return <div>찜 목록 로딩 중...</div>;
    if (error) return <div>에러: {error}</div>;

    return (
        <div className="like-list-page">
            <h2>나의 찜 목록 ({likeItems.length}개)</h2>
            <hr />
            {likeItems.length === 0 ? (
                <p>아직 찜한 상품이 없습니다. 마음에 드는 상품을 찾아보세요!</p>
            ) : (
                <ul className="like-items-grid">
                    {likeItems.map(item => (
                        <li key={item.likeId || item.itemId} className="like-item-card">

                            <Link to={`/item/${item.itemId}`}>
                                <img src={item.imageUrl} alt={item.name} className="item-image" />

                                <div className="item-details">
                                    <p className="item-name">{item.name}</p>
                                    <p className="item-price">{item.price ? item.price.toLocaleString() : '가격 정보 없음'}원</p>
                                </div>
                            </Link>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}