import React from 'react'
import { Routes, Route } from 'react-router-dom'
import Header from './components/Header.jsx'
import LoginPage from "./pages/User/LoginPage.jsx";
import Signup from "./pages/User/Signup.jsx";
import ItemPage from "./pages/item/ItemPage.jsx";
import { AuthProvider } from "./context/AuthContext.jsx";
import CartPage from "./pages/itemCart/CartPage.jsx";
import PaymentSuccessPage from "./pages/Payments/PaymentSuccessPage.jsx";
import PaymentFailPage from "./pages/Payments/PaymentFailPage.jsx";
import MyPage from "./pages/User/MyPage.jsx";
import { fetchCartCountFast } from "./api/itemCart.js";
import ItemDetailPage from "./pages/item/ItemDetailPage.jsx";
import DeleteAccount from "./pages/User/DeleteAccount.jsx";
import FindId from "./pages/User/FindId.jsx";
import FindPassword from "./pages/User/FindPassword.jsx";
import OAuth2RedirectHandler from "./pages/User/OAuth2RedirectHandler.jsx";
import Home from "./components/Home.jsx";

const About    = () => <div className="page">회사소개</div>


export default function App() {
    return (
        <AuthProvider>
            <Header cartCnt={fetchCartCountFast()} />
            <Routes>
                <Route path="/" element={<Home/>} />
                <Route path="/about" element={<About/>} />
                <Route path="/items" element={<ItemPage/>} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/cart" element={<CartPage />} />
                <Route path="/payments/success" element={<PaymentSuccessPage />} />
                <Route path="/payments/fail" element={<PaymentFailPage />} />
                <Route path="/mypage" element={<MyPage />} />
                <Route path="/item/:itemId" element={<ItemDetailPage />} />
                <Route path="/deleteAccount" element={<DeleteAccount />} />
                <Route path="/findId" element={<FindId />} />
                <Route path="/findPassword" element={<FindPassword />} />
                <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />

            </Routes>
        </AuthProvider>
    )
}
