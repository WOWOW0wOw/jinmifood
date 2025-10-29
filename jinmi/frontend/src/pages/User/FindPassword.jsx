import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from "../../api/apiClient.js";
import { useAuth } from "../../context/AuthContext.jsx";
import styles from './css/FindPassword.module.css';

const FindPassword = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [isCodeVerified, setIsCodeVerified] = useState(false);
    const [email, setEmail] = useState('');
    const [code, setCode] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const [foundUserProvider, setFoundUserProvider] = useState('');

    const isSocialAccount = foundUserProvider !== 'local' && foundUserProvider !== '';

    const getSocialMessage = (provider) => {
        switch (provider) {
            case 'kakao':
                return '카카오톡으로 가입한 계정입니다.';
            case 'google':
                return 'Google로 가입한 계정입니다.';
            default:
                return '소셜 로그인으로 가입된 계정입니다.';
        }
    };

    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/;

    const handleSendCode = async () => {
        setError('');
        setIsCodeVerified(false);
        setFoundUserProvider('');
        setCode('');

        if (!email || !emailPattern.test(email)) {
            setError('유효한 이메일 형식으로 입력해 주세요.');
            return;
        }

        setIsLoading(true);
        try {
            await apiClient.post('/users/findPassword/sendCode', { email });
            setStep(2);
            setError('✅ 인증 코드가 발송되었습니다. (유효시간 5분)');
        } catch (err) {
            if (err.response?.data?.message && err.response.data.message.includes("USER_NOT_FOUND")) {
                setError('해당 이메일로 가입된 계정이 없습니다.');
            } else {
                setError(err.response?.data?.message || '인증 코드 발송에 실패했습니다.');
            }
            setStep(1);
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
            const response = await apiClient.post('/users/findPassword/verifyCode', { email, code });

            const provider = response.data.data.provider || 'local';
            setFoundUserProvider(provider);

            setIsCodeVerified(true);
            if (provider !== 'local' && provider !== '') {
                setStep(3);
                setError('✅ 인증에 성공했습니다. 계정 정보를 확인해주세요.');
            } else {
                setError('✅ 인증에 성공했습니다. 새 비밀번호를 설정해주세요.');
            }
        } catch (err) {
            setError(err.response?.data?.message || '인증 코드 확인에 실패했습니다. 코드를 다시 확인해주세요.');
            setIsCodeVerified(false);
            setFoundUserProvider('');
            setStep(2);
        } finally {
            setIsLoading(false);
        }
    };

    const handleResetPassword = async () => {
        setError('');
        if (!newPassword || !confirmPassword) {
            setError('새 비밀번호를 입력하고 확인해 주세요.');
            return;
        }
        if (newPassword !== confirmPassword) {
            setError('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.');
            return;
        }
        if (!passwordPattern.test(newPassword)) {
            setError('비밀번호는 영문, 숫자를 포함하여 8~20자여야 합니다.');
            return;
        }

        setIsLoading(true);
        try {
            await apiClient.post('/users/findPassword/reset', {
                email,
                code,
                newPassword
            });

            setStep(3);
            setError('');
        } catch (err) {
            setError(err.response?.data?.message || '비밀번호 재설정에 실패했습니다. 인증 코드를 다시 확인해주세요.');
            setIsCodeVerified(false);
            setStep(2);
        } finally {
            setIsLoading(false);
        }
    };

    const handleGoToLogin = () => {
        navigate('/login');
    };

    const isSuccessMessage = error && error.includes('✅');

    return (
        <div className={styles.findPasswordContainer}>
            <h2 className={styles.greetingHeader}>비밀번호 찾기</h2>

            {step === 1 && (
                <div className={styles.formGroup}>
                    <p className={styles.description}>가입 시 등록한 이메일로 본인 인증을 진행합니다.</p>
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

            {step === 2 && !isCodeVerified && (
                <div className={styles.formGroup}>
                    <p className={styles.description}>
                        {email}로 인증 코드가 발송되었습니다. (유효시간 5분)
                    </p>

                    <input
                        type="text"
                        placeholder="인증 코드를 입력하세요 (6자리)"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        className={styles.inputField}
                    />

                    <button
                        onClick={handleVerifyCode}
                        disabled={isLoading || !code}
                        className={styles.submitButton}>
                        {isLoading ? '확인 중...' : '인증 코드 확인'}
                    </button>

                    <button onClick={handleSendCode} disabled={isLoading} className={styles.linkButton}>
                        코드 재전송
                    </button>
                </div>
            )}

            {step === 2 && isCodeVerified && !isSocialAccount &&(
                <div className={styles.formGroup}>

                    <input
                        type="password"
                        placeholder="새 비밀번호 (영문, 숫자 포함 8~20자)"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        className={styles.inputField}
                    />
                    <input
                        type="password"
                        placeholder="새 비밀번호 확인"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        className={styles.inputField}
                    />

                    <button onClick={handleResetPassword} disabled={isLoading} className={styles.submitButton}>
                        {isLoading ? '재설정 중...' : '비밀번호 재설정'}
                    </button>
                </div>
            )}

            {step === 2 && isCodeVerified && isSocialAccount && (
                <div className={`${styles.formGroup} ${styles.successBox}`}>
                    <p className={styles.successText}>
                        {getSocialMessage(foundUserProvider)}
                    </p>
                    <h2 className={styles.foundId}>
                        {email}
                    </h2>
                    <button onClick={handleGoToLogin} className={styles.submitButton}>
                        로그인으로 이동
                    </button>
                </div>
            )}

            {step === 3 && (
                <div className={`${styles.formGroup} ${styles.successBox}`}>
                    <p className={styles.successText}>
                        {isSocialAccount
                            ? getSocialMessage(foundUserProvider)
                            : '비밀번호가 성공적으로 변경되었습니다.'
                        }
                    </p>
                    <h2 className={styles.foundId}>
                        {isSocialAccount ? email : "변경 완료"}
                    </h2>
                    <button onClick={handleGoToLogin} className={styles.submitButton}>
                        로그인으로 이동
                    </button>
                    {!isSocialAccount && (
                        <p className={styles.utilLink}>
                            <span onClick={() => navigate('/findId')} className={styles.link}>아이디 찾기</span>
                        </p>
                    )}
                </div>
            )}

            {error && (
                <p className={isSuccessMessage ? styles.successMessage : styles.errorMessage}>
                    {error.replace('✅ ', '')}
                </p>
            )}
        </div>
    );
};

export default FindPassword;