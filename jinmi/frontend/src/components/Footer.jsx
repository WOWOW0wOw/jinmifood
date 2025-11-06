import React from "react";
import "./footer.css";

export default function Footer() {
    const year = new Date().getFullYear();

    return (
        <footer className="site-footer">
            <div className="footer__container">
                {/* 1행: 주소/대표/전화 */}
                <address className="footer__row footer__row--info" aria-label="회사 정보">
                    <span>서울특별시 영등포구 디지털로 53가길 2-2</span>
                    <span className="sep" aria-hidden="true">ㅣ</span>
                    <span>대표자: 이매화</span>
                    <span className="sep" aria-hidden="true">ㅣ</span>
                    <span>
            TEL: <a href="tel:0263976686" className="footer__link">02-6397-6686</a>
          </span>
                </address>

                {/* 2행: 사업자/상호/통신판매 */}
                <div className="footer__row footer__row--biz">
                    <span>사업자번호 : 807-56-00747</span>
                    <span className="sep" aria-hidden="true">ㅣ</span>
                    <span>상호명 : 진미푸드</span>
                    <span className="sep" aria-hidden="true">ㅣ</span>
                    <span>통신판매 : 제2025-서울영등포-0991호</span>
                </div>

                {/* 3행: 저작권 */}
                <small className="footer__copy">
                    Copyright © {year} 진미푸드 All rights reserved.
                </small>
            </div>
        </footer>
    );
}
