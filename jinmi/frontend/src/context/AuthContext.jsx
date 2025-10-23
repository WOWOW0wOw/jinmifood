// frontend/src/context/AuthContext.jsx

import React, { createContext, useState, useEffect, useContext} from "react";
// axios는 더 이상 필요 없으니 제거했습니다.
import {useNavigate} from "react-router-dom";
import apiClient from "../api/apiClient.js";

// Context 객체 생성
export const AuthContext = createContext(null);

// provider 컴포넌트 생성
export function AuthProvider({ children }){
    const navigate = useNavigate();
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [user, setUser] = useState(null); // 사용자 정보
    const [isLoading, setIsLoading] = useState(true);

    // 페이지 로드 시, 토큰을 확인하고 사용자 정보를 가져오는 함수
    const loadUserFromStorage = async () => {
        const accessToken = localStorage.getItem('accessToken');

        if(accessToken){
            try {

                const response = await apiClient.get('/users/myInfo');

                setUser(response.data.data); // 백에서 받은 사용자 정보 전체 저장
                setIsLoggedIn(true);
            } catch (error){
                // 401 Unauthorized 등의 에러 시 토큰 무효화
                console.warn("Access Token 로드 실패. Refresh Token 확인 필요 또는 토큰 재발급실패:");
                handleLogout(); // 강제 로그아웃
            }
        }
        setIsLoading(false);
    };

    const handleLogin = (accessToken, refreshToken) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);

        //  토큰 저장 후, 즉시 사용자 정보를 로드하여 Context를 업데이트합니다.
        loadUserFromStorage();
    }

    const handleLogout = async () => {
        const accessToken = localStorage.getItem('accessToken');

        if (accessToken) {
            try {
                await apiClient.post('/users/logout');
                console.log("백엔드에서 로그아웃 요청 성공: 토큰 블랙리스트 등록 완료");
            } catch (error) {
                console.error("백엔드 로그아웃 요청 실패 ", error);
            }
        }
        clearAuthDataAndRedirect();
    };
        const clearAuthDataAndRedirect = () => {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            setIsLoggedIn(false);
            setUser(null);
            navigate('/'); // 로그아웃 후 메인 페이지로 이동
        };

        const handleLocalLogout = () => {
            clearAuthDataAndRedirect();
        };

    useEffect(() => {
        loadUserFromStorage();
    }, []);

    const contextValue = {
        isLoggedIn,
        user,
        isLoading,
        handleLogin,
        handleLogout,
        handleLocalLogout,
    };

    return (
        <AuthContext.Provider value={contextValue}>
            {isLoading ? <div>인증 정보 로딩 중...</div> : children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => {
    return useContext(AuthContext);
};