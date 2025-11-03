import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from "../../context/AuthContext.jsx";
import apiClient from "../../api/apiClient.js";
import styles from './css/FindId.module.css';

const FindId = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [email, setEmail] = useState('');
    const [code, setCode] = useState('');
    const [foundAccount, setFoundAccount] = useState({ email: '', provider: '' });
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

    const handleSendCode = async () => {
        setError('');
        if (!email) {
            setError('이메일을 입력해 주세요.');
            return;
        }
        if (!emailPattern.test(email)) {
            setError('유효한 이메일 형식으로 입력해 주세요.');
            return;
        }

        setIsLoading(true);
        try {
            await apiClient.post('/users/findId/sendCode', { email });
            setStep(2);
        } catch (err) {
            if (err.response?.data?.message.includes("USER_NOT_FOUND")) {
                setError('해당 이메일로 가입된 계정이 없습니다.');
            } else {
                setError(err.response?.data?.message || '인증 코드 발송에 실패했습니다.');
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleVerifyCode = async () => {
        setError('');
        if (!code) {
            setError('인증 코드를 입력해 주세요.');
            return;
        }

        setIsLoading(true);
        try {
            const response = await apiClient.post('/users/findId/verifyCode', { email, code });

            setFoundAccount(response.data.data);
            setStep(3);
        } catch (err) {
            setError(err.response?.data?.message || '인증 코드 확인에 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleGoToLogin = () => {
        navigate('/login');
    };

    const getAccountMessage = (provider, email) => {
        switch (provider) {
            case 'kakao':
                return '카카오톡으로 가입한 계정입니다.';
            case 'google':
                return 'Google로 가입한 계정입니다.';
            case 'naver':
                return 'Naver로 가입한 계정입니다.';
            case 'local':
                return `회원님의 아이디는 다음과 같습니다.`;
            default:
                return `회원님의 아이디는 다음과 같습니다.`;
        }
    };
    const isSocialAccount = foundAccount.provider !== 'local' && foundAccount.provider !== '';
    return (
        <div className={styles.findIdContainer}>
            <h2 className={styles.greetingHeader}>아이디 찾기</h2>

            {step === 1 && (
                <div className={styles.formGroup}>
                    <p className={styles.description}>가입 시 등록한 이메일로 인증을 진행합니다.</p>
                    <input
                        type="email"
                        placeholder="가입 시 사용한 이메일 주소"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className={styles.inputField}
                    />
                    <button onClick={handleSendCode} disabled={isLoading} className={styles.submitButton}>
                        {isLoading ? '전송 중...' : '인증 코드 받기'}
                    </button>
                </div>
            )}

            {step === 2 && (
                <div className={styles.formGroup}>
                    <p className={styles.infoText}>{email}로 인증 코드가 발송되었습니다. (유효시간 5분)</p>
                    <input
                        type="text"
                        placeholder="인증 코드를 입력하세요 (6자리)"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        className={styles.inputField}
                    />
                    <button
                        onClick={handleVerifyCode}
                        disabled={isLoading}
                        className={styles.submitButton}> {/* verifyButton 대신 submitButton 사용 */}
                        {isLoading ? '확인 중...' : '확인'}
                    </button>
                    <button onClick={handleSendCode} disabled={isLoading} className={styles.linkButton}>
                        코드 재전송
                    </button>
                </div>
            )}

            {step === 3 && (
                <div className={`${styles.formGroup} ${styles.successBox}`}>
                    <p className={styles.successText}>
                        {getAccountMessage(foundAccount.provider, foundAccount.email)}
                    </p>

                    <h2 className={styles.foundId}>
                        {foundAccount.email}
                    </h2>
                    <button onClick={handleGoToLogin} className={styles.submitButton}>
                        로그인으로 이동
                    </button>
                    <p className={styles.utilLink}>
                        <span onClick={() => navigate('/findPassword')} className={styles.link}>비밀번호 찾기</span>
                    </p>
                </div>
            )}

            {error && <p className={styles.errorMessage}>{error}</p>}
        </div>
    );
};

export default FindId;