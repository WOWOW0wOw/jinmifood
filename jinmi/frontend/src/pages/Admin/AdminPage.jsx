import React, { useState } from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import "./AdminPage.css";

export default function AdminLayout() {
    const location = useLocation();
    const [openCategory, setOpenCategory] = useState("member");

    const toggleCategory = (category) => {
        setOpenCategory(openCategory === category ? "" : category);
    };

    // 현재 활성화된 메뉴인지 확인하는 함수
    const isActive = (path) => location.pathname === path;

    return (
        <div className="admin-container">
            <aside className="admin-sidebar">
                <div className="sidebar-title">ADMINISTRATOR</div>
                <nav className="admin-nav">
                    <div className={`nav-group ${openCategory === "member" ? "open" : ""}`}>
                        <div className="group-header" onClick={() => toggleCategory("member")}>
                            <span>👤 회원</span>
                            <span className="arrow">{openCategory === "member" ? "▲" : "▼"}</span>
                        </div>
                        <ul className="sub-menu">
                            <li className={isActive("/admin") ? "active" : ""}>
                                <Link to="/admin">회원관리</Link>
                            </li>
                            <li className={isActive("/admin/stats") ? "active" : ""}>
                                <Link to="/admin/stats">접속자집계</Link>
                            </li>
                            <li className={isActive("/admin/search") ? "active" : ""}>
                                <Link to="/admin/search">접속자검색</Link>
                            </li>

                        </ul>
                    </div>

                    {/* 상품 관리 카테고리 예시 시현이?*/}
                    <div className={`nav-group ${openCategory === "product" ? "open" : ""}`}>
                        <div className="group-header" onClick={() => toggleCategory("product")}>
                            <span>📦 상품</span>
                            <span className="arrow">{openCategory === "product" ? "▲" : "▼"}</span>
                        </div>
                        <ul className="sub-menu">
                            <li className={isActive("/admin/items") ? "active" : ""}>
                                <Link to="/admin/items">상품관리</Link>
                            </li>
                            <li className={isActive("/admin/category") ? "active" : ""}>
                                <Link to="/admin/category">카테고리관리</Link>
                            </li>
                        </ul>
                    </div>

                    {/* 주문 관리 카테고리 예시 석형?*/}
                    <div className={`nav-group ${openCategory === "order" ? "open" : ""}`}>
                        <div className="group-header" onClick={() => toggleCategory("order")}>
                            <span>🛒 주문</span>
                            <span className="arrow">{openCategory === "order" ? "▲" : "▼"}</span>
                        </div>
                        <ul className="sub-menu">
                            <li className={isActive("/admin/orders") ? "active" : ""}>
                                <Link to="/admin/orders">주문내역조회</Link>
                            </li>
                            <li className={isActive("/admin/delivery") ? "active" : ""}>
                                <Link to="/admin/delivery">배송관리</Link>
                            </li>
                        </ul>
                    </div>

                </nav>
            </aside>

            {/* 우측 */}
            <main className="admin-content">
                <Outlet /> {/* 여기가  MemberList나 AccessStats로 바뀜 */}
            </main>
        </div>
    );
}