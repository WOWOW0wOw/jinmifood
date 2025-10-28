import React, {useState} from 'react';
import { useNavigate } from "react-router-dom";
import apiClient from "../../api/apiClient.js";
import { useAuth} from "../../context/AuthContext.jsx";
import styles from './css/Login.module.css';
import GoogleIcon from './google-icon.png';
import KakaoIcon from './KakaoTalk_logo.png';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const GOOGLE_AUTH_URL = `${API_BASE_URL}/oauth2/authorization/google`;
const KAKAO_AUTH_URL = `${API_BASE_URL}/oauth2/authorization/kakao`;

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
                `/users/login`,
                credentials
            );

            const { accessToken, refreshToken } = response.data;

            handleLogin(accessToken,refreshToken);
            window.location.replace("/");
            alert('로그인에 성공하였습니다.');
            navigate('/');
        } catch (error) {

            let errorMessage = '로그인에 실패했습니다. 서버 오류를 확인하세요.';

            if (error.response && error.response.data && error.response.data.message) {
                errorMessage = error.response.data.message;
            } else if (error.message === 'Network Error') {
                errorMessage = '네트워크 연결 상태를 확인해주세요.';
            }
            if (errorMessage === 'Bad Request'){
                errorMessage = '아이디와 비밀번호가 틀렸습니다.';
            }

            console.error('Login Error:', error);
            alert(`${errorMessage}`);
        }
    };
    const handleGoogleLogin = () => {
        window.location.href = GOOGLE_AUTH_URL;
    };
    const handleKakaoLogin = () => {
        window.location.href = KAKAO_AUTH_URL;
    };


    return (
        <div className={styles.container}>
            <h1 className={styles.greetingHeader}>
                안녕하세요
                <br />
                <span className={styles.brandName}>진미푸드입니다.</span>
            </h1>
            <p className={styles.description}>
                진미푸드 통합회원으로 로그인이 가능합니다.
            </p>

            <form className={styles.form} onSubmit={handleSubmit}>
                <input type="email"
                       name="email"
                       placeholder="아이디 입력"
                       value={credentials.email}
                       onChange={handleChange}
                       required
                       className={styles.input}
                />

                <input type="password"
                       name="password"
                       placeholder="비밀번호 입력"
                       value={credentials.password}
                       onChange={handleChange}
                       required
                       className={styles.input}
                />

                {error && <p className={styles.error}>{error}</p>}

                <button type="submit" className={styles.loginButton}>로그인</button>
            </form>

            <div className={styles.utilityLinks}>
                <span onClick={() => navigate('/findId')} className={styles.link}>아이디 찾기</span>
                <span className={styles.separator}>|</span>
                <span onClick={() => navigate('/findPassword')} className={styles.link}>비밀번호 찾기</span>
                <span className={styles.separator}>|</span>
                <span onClick={() => navigate('/signup')} className={styles.link}>회원가입</span>
            </div>

            <div className={styles.snsSection}>
                <div className={styles.snsDivider}>
                    <span className={styles.snsText}>SNS 계정으로 로그인</span>
                </div>
                <div className={styles.snsIcons}>

                    <div className={`${styles.snsIconWrapper} ${styles.kakao}`}
                         onClick={handleKakaoLogin}>
                        <img src={KakaoIcon} alt="Kakao Logo" className={styles.snsImage} />
                    </div>

                    <div className={`${styles.snsIconWrapper} ${styles.google}`}
                         onClick={handleGoogleLogin}>
                        <img src={GoogleIcon} alt="Google Icon" className={styles.snsImage} />
                    </div>
                </div>
            </div>
        </div>
    );
}