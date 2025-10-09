import React, {useState} from 'react';
import { useNavigate } from "react-router-dom";
import apiClient from "../../api/apiClient.js";
import { useAuth} from "../../context/AuthContext.jsx";
import axios from 'axios';
import styles from './css/Login.module.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export default function LoginPage() {

    const navigate = useNavigate();
    const { handleLogin } = useAuth();

    const [error, setError] = useState(null);

    const [credentials, setCredentials] = useState({
        email: '',
        password: '',
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setCredentials({
            ...credentials,
            [name]: value,
        });
        if(error) setError(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!credentials.email || !credentials.password){
            alert('이메일 과 비밀먼호를 입력하세요.');
            return;
        }
        try {

            const response = await apiClient.post(
                `/users/login`, // apiClient의 baseURL이 이미 설정되어 있으므로 경로만 명시
                credentials
            );

            const { accessToken, refreshToken } = response.data; // 백엔드 응답 확인

            handleLogin(accessToken,refreshToken);

            alert('로그인에 성공하였습니다.');
            navigate('/');
        } catch (error) {

            let errorMessage = '로그인에 실패했습니다. 서버 오류를 확인하세요.';

            if (error.response && error.response.data && error.response.data.message) {
                errorMessage = error.response.data.message;
            } else if (error.message === 'Network Error') {
                errorMessage = '네트워크 연결 상태를 확인해주세요.';
            }

            console.error('Login Error:', error);
            alert(`로그인 실패: ${errorMessage}`);
        }
    };



    return (
        <div className={styles.container}>
            <h2>로그인</h2>
            <form className={styles.form} onSubmit={handleSubmit}>
                <input type="email"
                       name="email"
                       placeholder="이메일"
                       value={credentials.email}
                       onChange={handleChange}
                       required
                       className={styles.input}
                />

                <input type="password"
                       name="password"
                       placeholder="비밀번호"
                       value={credentials.password}
                       onChange={handleChange}
                       required
                       className={styles.input}
                />

                {error && <p className={styles.error}>{error}</p>}

                <button type="submit" className={styles.button}>로그인</button>

                <p className={styles.signupLink}>
                    계정이 없으신가요? <span onClick={() => navigate('/signup')} className={styles.link}>회원가입</span>
                </p>
            </form>
        </div>
    );
}