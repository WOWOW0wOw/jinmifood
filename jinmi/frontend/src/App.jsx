import React from 'react'
import { Routes, Route } from 'react-router-dom'
import Header from './components/Header.jsx'
import LoginPage from "./pages/User/LoginPage.jsx";
import Signup from "./pages/User/Signup.jsx";
import { AuthProvider } from "./context/AuthContext.jsx";

const Home     = () => <div className="page">메인</div>
const About    = () => <div className="page">회사소개</div>
const Products = () => <div className="page">전체상품</div>
const Cart     = () => <div className="page">장바구니</div>


export default function App() {
    return (
        <AuthProvider>
            <Header cartCnt={0} />
            <Routes>
                <Route path="/" element={<Home/>} />
                <Route path="/about" element={<About/>} />
                <Route path="/products" element={<Products/>} />
                <Route path="/cart" element={<Cart/>} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/login" element={<LoginPage />} />
            </Routes>
        </AuthProvider>
    )
}
