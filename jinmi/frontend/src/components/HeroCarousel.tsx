import { useEffect, useState } from "react";

import "./heroCarousel.css";
// @ts-ignore
import main1 from "../assets/main1.jpg";
// @ts-ignore
import main2 from "../assets/main2.jpg";
// @ts-ignore
import main3 from "../assets/main3.jpg";

/**
 * slides: 배너 데이터 배열
 *  - bg: 배경 이미지
 *  - badge: 작은 상단 라벨
 *  - title: 큰 헤드라인 (줄바꿈 포함 가능)
 *  - desc: 설명 문구 (2~3줄)
 *  - primary / secondary: 버튼 정보
 */

const slidesData = [
    {
        bg: main1, // 실제 배너 이미지 경로 (예: 인기 상품 콜라보 컷)
        badge: "LIMITED",
        title: "프리미엄 먹태\n지금이 제철",
        desc:
            "당일 손질한 먹태를 도축 후 바로 건조.\n육즙 살아있는 부드러운 식감.",
        primary: { label: "자세히 보기", href: "/products/123" },
        secondary: { label: "구매하기", href: "/products/123" },
        dark: true, // 글자색을 밝게 처리할지 여부
    },
    {
        bg: main2,
        badge: "인기 상품",
        title: "술안주 준비 끝.\n안주 세트 특가",
        desc:
            "쥐포 / 먹태 / 오징어채\n다 들어있는 구성 그대로 보내드립니다.",
        primary: { label: "세트 보러가기", href: "/products?cat=set" },
        secondary: { label: "도매문의", href: "/b2b" },
        dark: true,
    },
    {
        bg: main3,
        badge: "B2B",
        title: "식당 · 포차 사장님\n대량 단가 별도 상담",
        desc:
            "꾸준히 나가는 메뉴라면 단가가 곧 수익.\n전화로 바로 견적 드립니다.",
        primary: { label: "전화상담", href: "tel:010-0000-0000" },
        secondary: { label: "온라인 문의", href: "/b2b" },
        dark: false, // 이 배너는 배경이 어두울 수도/밝을 수도라서 바꿔도 됨
    },
];

export default function HeroCarousel() {
    const [idx, setIdx] = useState(0);

    // 자동 슬라이드 (5초마다 다음으로)
    useEffect(() => {
        const timer = setInterval(() => {
            setIdx((prev) => (prev + 1) % slidesData.length);
        }, 5000);
        return () => clearInterval(timer);
    }, []);

    const goPrev = () => {
        setIdx((prev) => (prev === 0 ? slidesData.length - 1 : prev - 1));
    };

    const goNext = () => {
        setIdx((prev) => (prev + 1) % slidesData.length);
    };

    const slide = slidesData[idx];

    // desc/title에 줄바꿈 `\n` 넣었으니까 줄 단위로 <br/> 처리
    const renderMultiline = (text) =>
        text.split("\n").map((line, i) => (
            <>
                {line}
                <br />
            </>
        ));


    return (
        <section className="heroCarousel">
            {/* 슬라이드 이미지 배경 */}
            <div
                className="heroCarousel__bg"
                style={{ backgroundImage: `url(${slide.bg})` }}
            />

            {/* 좌우 화살표 */}
            <button
                className="heroCarousel__nav heroCarousel__nav--left"
                onClick={goPrev}
                aria-label="이전 배너"
            >
                ‹
            </button>
            <button
                className="heroCarousel__nav heroCarousel__nav--right"
                onClick={goNext}
                aria-label="다음 배너"
            >
                ›
            </button>

            {/* 가운데 컨텐츠 박스 */}
            <div className="heroCarousel__inner">
                <div
                    className={
                        "heroCarousel__content" +
                        (slide.dark ? " heroCarousel__content--light" : "")
                    }
                >
                    {slide.badge && (
                        <div className="heroCarousel__badge">{slide.badge}</div>
                    )}

                    <h2 className="heroCarousel__title">
                        {renderMultiline(slide.title)}
                    </h2>

                    <p className="heroCarousel__desc">
                        {renderMultiline(slide.desc)}
                    </p>

                    <div className="heroCarousel__actions">
                        {slide.primary && (
                            <a
                                className="heroCarousel__btn heroCarousel__btn--main"
                                href={slide.primary.href}
                            >
                                {slide.primary.label}
                            </a>
                        )}
                        {slide.secondary && (
                            <a
                                className="heroCarousel__btn heroCarousel__btn--ghost"
                                href={slide.secondary.href}
                            >
                                {slide.secondary.label}
                            </a>
                        )}
                    </div>

                    {/* 인디케이터 (● ● ● 느낌) */}
                    <div className="heroCarousel__dots">
                        {slidesData.map((_, i) => (
                            <button
                                key={i}
                                className={
                                    "heroCarousel__dot" +
                                    (i === idx
                                        ? " heroCarousel__dot--active"
                                        : "")
                                }
                                onClick={() => setIdx(i)}
                                aria-label={`${i + 1}번째 배너로 이동`}
                            />
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
}
