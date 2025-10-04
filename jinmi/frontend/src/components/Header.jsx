import React from "react";
import "./header.css";

export default function Header({ cartCnt = 0 }) {
    return (
        <header className="hd">
            {/* 상단 바: 우측 정렬 */}
            <div className="hd__container">
                <div className="hd__right">
                    <a href="/login" className="pill-btn">로그인</a>
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
                        <a href="/" className="logo">JINMI<span>FOOD</span></a>
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
                                <a className="gnb__link" href="/products">전체상품</a>
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
