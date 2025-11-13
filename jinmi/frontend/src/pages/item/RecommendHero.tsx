// @ts-ignore
import React, { useEffect, useRef, useState } from "react";
// @ts-ignore
import s from "../item/css/RecommendHero.module.css";

/**
 * slides: [
 *   { bg, title, desc, badge, ctaPrimary:{label,type,payload}, ctaGhost:{...} }
 * ]
 */
export default function HeroCarousel({ slides = [], onAction, interval = 5000 }) {
    const [idx, setIdx] = useState(0);
    const timerRef = useRef(null);
    const pausedRef = useRef(false);

    const n = slides.length || 1;
    const prev = () => setIdx((i) => (i - 1 + n) % n);
    const next = () => setIdx((i) => (i + 1) % n);
    const go = (i) => setIdx(i);

    useEffect(() => {
        if (!slides.length) return;
        clearInterval(timerRef.current);
        timerRef.current = setInterval(() => {
            if (!pausedRef.current) next();
        }, interval);
        return () => clearInterval(timerRef.current);
    }, [slides.length, idx, interval]);

    if (!slides.length) return null;

    const cur = slides[idx];
    const left = slides[(idx - 1 + n) % n];
    const right = slides[(idx + 1) % n];

    return (
        <section
            className={s.hero}
            onMouseEnter={() => (pausedRef.current = true)}
            onMouseLeave={() => (pausedRef.current = false)}
        >
            {/* 좌/우 배경 패널 */}
            <div
                className={`${s.side} ${s.left}`}
                style={{ backgroundImage: `url(${left?.bg || ""})` }}
            />
            <div
                className={`${s.side} ${s.right}`}
                style={{ backgroundImage: `url(${right?.bg || ""})` }}
            />

            {/* 중앙 카드 + 화살표(카드 바깥) */}
            <div className={s.cardWrap}>
                <button className={`${s.arrow} ${s.arrowLeft}`} onClick={prev} aria-label="이전">
                    ‹
                </button>

                <article className={s.card}>
                    {cur.badge && <span className={s.badge}>{cur.badge}</span>}
                    <h2 className={s.title}>{cur.title}</h2>
                    {cur.desc && <p className={s.desc}>{cur.desc}</p>}

                    <div className={s.actions}>
                        {cur.ctaPrimary && (
                            <button
                                className={s.btnPrimary}
                                onClick={() => onAction?.(cur.ctaPrimary)}
                            >
                                {cur.ctaPrimary.label}
                            </button>
                        )}
                        {cur.ctaGhost && (
                            <button
                                className={s.btnGhost}
                                onClick={() => onAction?.(cur.ctaGhost)}
                            >
                                {cur.ctaGhost.label}
                            </button>
                        )}
                    </div>
                </article>

                <button className={`${s.arrow} ${s.arrowRight}`} onClick={next} aria-label="다음">
                    ›
                </button>
            </div>

            {/* 도트 */}
            <div className={s.dots}>
                {slides.map((_, i) => (
                    <button
                        key={i}
                        onClick={() => go(i)}
                        className={`${s.dot} ${i === idx ? s.active : ""}`}
                        aria-label={`${i + 1}번 슬라이드`}
                    />
                ))}
            </div>
        </section>
    );
}
