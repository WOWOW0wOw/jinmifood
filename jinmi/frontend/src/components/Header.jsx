import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { Search, ShoppingCart, MessageCircle } from "lucide-react";
import "./header.css";
import logo from "../assets/진미푸드 로고.png";

export default function Header({ cartCnt = 0 }) {
    const { isLoggedIn, user, handleLogout } = useAuth();
    const displayName = user?.displayName || '회원';

    const [isSearchOpen, setIsSearchOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");

    const toggleSearch = () => setIsSearchOpen(prev => !prev);
    const closeSearch = () => setIsSearchOpen(false);

    const handleSearch = (e) => {
        e.preventDefault();
        if (searchQuery.trim()) {
            window.location.href = `/search?query=${encodeURIComponent(searchQuery)}`;
        }
    };

    const handleKeywordClick = (keyword) => {
        window.location.href = `/search?query=${encodeURIComponent(keyword)}`;
    };

    // 카카오 SDK 초기화 (플로팅 버튼용)
    useEffect(() => {
        const script = document.createElement("script");
        script.src = "https://developers.kakao.com/sdk/js/kakao.js";
        script.async = true;
        document.body.appendChild(script);

        script.onload = () => {
            if (!window.Kakao.isInitialized()) {
                window.Kakao.init("58ce2600ddb571064fc225857ad5fd0e"); // ← 본인 JS 키 입력
            }
        };

        return () => {
            if (document.body.contains(script)) {
                document.body.removeChild(script);
            }
        };
    }, []);

    // 카카오톡 채널 챗봇 열기
    const openKakaoChat = () => {
        // 로컬 환경 fallback (ngrok 사용 권장)
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            alert('로컬 환경에서는 카카오톡 문의가 제한됩니다.\nngrok 등으로 https URL을 만들어 테스트해주세요.');
            return;
        }

        if (window.Kakao && window.Kakao.Channel) {
            window.Kakao.Channel.chat({
                channelPublicId: "1360674" // ← 본인 채널 ID 입력 (예: "_ZeUTxl")
            });
        } else {
            alert("카카오톡 채널을 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
        }
    };

    return (
        <>
            <header className="hd hd--fixed">
                <div className="hd__container">
                    <div className="hd__right">
                        {isLoggedIn ? (
                            <div className="hd__right__dropdown has-sub">
                                <button className="pill-btn user-name-btn">{displayName}님</button>
                                <ul className="dropdown user-dropdown" role="menu">
                                    <li role="none">
                                        <Link role="menuitem" to="/mypage">마이페이지</Link>
                                    </li>
                                    <li role="none">
                                        <button
                                            role="menuitem"
                                            onClick={handleLogout}
                                            className="dropdown-logout-btn"
                                        >
                                            로그아웃
                                        </button>
                                    </li>
                                </ul>
                            </div>
                        ) : (
                            <Link to="/login" className="pill-btn">로그인</Link>
                        )}
                        <a href="/orders" className="pill-btn">주문조회</a>

                        <a href="/cart" className="cart-btn" aria-label="장바구니">
                            <ShoppingCart size={18} />
                            {cartCnt > 0 && <span className="badge">{cartCnt}</span>}
                        </a>
                    </div>
                </div>

                {/* 하단 네비게이션 */}
                <div className="hd__sub">
                    <div className="hd__sub__container">
                        <div className="hd__left">
                            <a href="/" className="logo">
                                <img src={logo} alt="진미푸드 로고" className="logo-img" />
                            </a>
                        </div>

                        <nav className="hd__center" aria-label="주요 메뉴">
                            <ul className="gnb">
                                <li className="gnb__item has-sub">
                                    <a className="gnb__link" href="/about">회사소개</a>
                                    <ul className="dropdown" role="menu">
                                        <li role="none"><a role="menuitem" href="/about/greeting">인사말</a></li>
                                        <li role="none"><a role="menuitem" href="/about/map">오시는길</a></li>
                                    </ul>
                                </li>

                                <li className="gnb__item">
                                    <a className="gnb__link" href="/items">전체상품</a>
                                </li>

                                <li className="gnb__item">
                                    <a className="gnb__link" href="/cart">장바구니</a>
                                </li>

                                <li className="gnb__item">
                                    <a className="gnb__link" href="/b2b">도매문의</a>
                                </li>

                                <li className="gnb__item">
                                    <button
                                        className="gnb__link search-trigger"
                                        onClick={toggleSearch}
                                        aria-label="상품 검색 열기"
                                    >
                                        상품검색
                                    </button>
                                </li>
                            </ul>
                        </nav>
                    </div>
                </div>
            </header>

            <div className="hd__spacer"></div>

            {/* ==================== 검색 사이드바 ==================== */}
            <div className={`search-sidebar ${isSearchOpen ? 'open' : ''}`}>
                <div className="search-sidebar-overlay" onClick={closeSearch}></div>
                <aside className="search-sidebar-panel">
                    <div className="search-sidebar-inner">
                        <form onSubmit={handleSearch} className="search-sidebar-form">
                            <div className="search-input-wrapper">
                                <input
                                    type="text"
                                    placeholder="상품을 검색해보세요..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="search-sidebar-input"
                                    autoFocus
                                />
                                <button type="submit" className="search-submit-icon">
                                    <Search size={22} />
                                </button>
                            </div>
                        </form>

                        {/* 추천 키워드 */}
                        <div className="search-keywords">
                            <p className="keywords-title">추천 검색어</p>
                            <div className="keyword-tags">
                                {["먹태", "오징어", "세트", "건어물", "지포", "안주"].map((kw) => (
                                    <button
                                        key={kw}
                                        className="keyword-tag"
                                        onClick={() => handleKeywordClick(kw)}
                                    >
                                        {kw}
                                    </button>
                                ))}
                            </div>
                        </div>
                    </div>
                </aside>
            </div>


            <button onClick={openKakaoChat} className="kakao-floating-btn" aria-label="카카오톡 문의">
                <MessageCircle size={28} />
            </button>
        </>
    );
}