import React from 'react'
import { Routes, Route } from 'react-router-dom'
import Header from './components/Header.jsx'
import LoginPage from "./pages/User/LoginPage.jsx";
import Signup from "./pages/User/Signup.jsx";
import { AuthProvider } from "./context/AuthContext.jsx";
import CartPage from "./pages/itemCart/CartPage.jsx";
import MyPage from "./pages/User/MyPage.jsx";

const Home     = () => <div className="page">메인</div>
const About    = () => <div className="page">회사소개</div>
const Products = () => <div className="page">전체상품</div>


export default function App() {
    return (
        <AuthProvider>
            <Header cartCnt={0} />
            <Routes>
                <Route path="/" element={<Home/>} />
                <Route path="/about" element={<About/>} />
                <Route path="/products" element={<Products/>} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/cart" element={<CartPage />} />
                <Route path="/mypage" element={<MyPage />} />
            </Routes>
        </AuthProvider>
    )
}
