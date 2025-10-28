import React from "react";
import HeroCarousel from "../components/HeroCarousel.jsx";
import "./home.css";

export default function HomePage() {
    // 나중에 백엔드에서 불러올 상품 예시 (베스트상품 등)
    const bestItems = [
        {
            id: 1,
            name: "먹태",
            desc: "도톰하고 촉촉한 프리미엄 안주",
            price: 12900,
            img: "/images/products/meoktae.jpg", // <- 실제 이미지 경로로 바꿔
        },
        {
            id: 2,
            name: "짝태",
            desc: "국물용/안주용 만능",
            price: 8900,
            img: "/images/products/hwangtae.jpg",
        },
        {
            id: 3,
            name: "미역줄기",
            desc: "소/중/대 골고루 구성",
            price: 15900,
            img: "/images/products/jwipo.jpg",
        },
        {
            id: 4,
            name: "오징어채",
            desc: "업장용 대용량 특가",
            price: 25900,
            img: "/images/products/ojingeo.jpg",
        },
    ];

    return (
        <main className="home">
            <HeroCarousel />
            {/* 2) 카테고리 바로가기 */}
            <section className="cats">
                <div className="cats__inner">
                    <a className="cats__card" href="/products?cat=dried">
                        <span className="cats__title">건어물</span>
                        <span className="cats__desc">쥐포 / 황태 / 먹태</span>
                    </a>
                    <a className="cats__card" href="/products?cat=semi-dried">
                        <span className="cats__title">장아찌</span>
                        <span className="cats__desc">바로 굽기만 하면 끝</span>
                    </a>
                    <a className="cats__card" href="/products?cat=set">
                        <span className="cats__title">미역</span>
                        <span className="cats__desc">선물 / 회식 인기</span>
                    </a>
                    <a className="cats__card" href="/b2b">
                        <span className="cats__title">대량/납품</span>
                        <span className="cats__desc">식당/술집/마트 공급</span>
                    </a>
                </div>
            </section>
            {/* 4) 도매/납품 CTA */}
            <section className="b2b">
                <div className="b2b__box">
                    <div className="b2b__text">
                        <div className="b2b__label">도매 문의</div>
                        <div className="b2b__title">
                            업장용 대량 구매, 단가 협의 가능합니다
                        </div>
                        <div className="b2b__desc">
                            업종 / 수량 알려주시면 견적 바로 드려요.
                            <br />
                            꾸준한 공급 계약도 가능합니다.
                        </div>
                    </div>
                    <div className="b2b__actions">
                        <a className="b2b__btn" href="/b2b">
                            도매 문의하기
                        </a>
                        <a className="b2b__sub" href="tel:010-0000-0000">
                            전화 상담 010-0000-0000
                        </a>
                    </div>
                </div>
            </section>

            <section className="best">
                <div className="best__head">
                    <h3 className="best__title">요즘 많이 나가요 🔥</h3>
                    <a className="best__more" href="/products">
                        전체보기 &gt;
                    </a>
                </div>

                <div className="best__grid">
                    {bestItems.map(item => (
                        <a className="prodCard" key={item.id} href={`/products/${item.id}`}>
                            <div className="prodCard__thumb">
                                {/* 상품 이미지 */}
                                <img src={item.img} alt={item.name} />
                            </div>
                            <div className="prodCard__body">
                                <div className="prodCard__name">{item.name}</div>
                                <div className="prodCard__desc">{item.desc}</div>
                                <div className="prodCard__price">
                                    {item.price.toLocaleString()}원
                                </div>
                            </div>
                        </a>
                    ))}
                </div>
            </section>
        </main>
    );
}
