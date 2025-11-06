import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';

const OAuth2RedirectHandler = () => {
    const navigate = useNavigate();
    const { handleLogin } = useAuth();

    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const accessToken = urlParams.get('token');
        const refreshToken = urlParams.get('refreshToken');
        const error = urlParams.get('error'); // 혹시 모를 에러 처리

        if (error) {
            console.error("OAuth2 로그인 에러:", error);
            alert(`소셜 로그인에 실패했습니다: ${error}`);
            navigate(`/login`, { replace: true });
            return;
        }

        if (accessToken && refreshToken) {
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);

            handleLogin(accessToken, refreshToken);

            console.log("OAuth2 로그인 성공! 토큰 저장 및 상태 업데이트 완료.");

            navigate('/', { replace: true });

        } else {
            console.error("소셜 로그인 실패: 토큰이 URL에 포함되지 않았습니다.");
            alert("소셜 로그인 과정에서 오류가 발생했습니다.");
            navigate('/login', { replace: true });
        }
    }, [navigate, handleLogin]);

    return (
        <div style={{ padding: '50px', textAlign: 'center' }}>
            <h2>소셜 로그인 처리 중...</h2>
            <p>인증 정보를 확인하고 있습니다. 잠시만 기다려주세요.</p>
        </div>
    );
};

export default OAuth2RedirectHandler;