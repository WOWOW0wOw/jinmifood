import React from "react";
import {Link} from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import "./header.css";
import logo from "../assets/진미푸드 로고.png";

export default function Header({ cartCnt = 0 }) {

    const { isLoggedIn, user, handleLogout } = useAuth();

    const displayName = user?.displayName || '회원';
    return (
        <header className="hd">
            {/* 상단 바: 우측 정렬 */}
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
                                        <Link role="menuitem" to="/mypage/likes">찜 목록</Link>
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
                        🛒<span className="badge">{cartCnt}</span>
                    </a>
                </div>
            </div>

            {/* 하단 바: 좌 로고 / 가운데 내비 */}
            <div className="hd__sub">
                <div className="hd__sub__container">
                    <div className="hd__left">
                        <a href="/" className="logo">
                            <img src={logo} alt={"로고"} className="logo"/>
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
                        </ul>
                    </nav>
                </div>
            </div>
        </header>
    );
}
