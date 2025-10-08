import React, {use, useState} from "react";
import {useNavigate} from "react-router-dom";
import apiClient from "../api/apiClient.js";

const Signup = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: '', // 비밀번호 확인
        displayName: '', // 닉네임
        phoneNumber: '',
        address: '',
    });
    const [error, setError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
        if (error) setError(null);
    };

    const validate = () => {
        const {email, password, confirmPassword, displayName, phoneNumber} = formData;

        if (!email || !password || !confirmPassword || !displayName || !phoneNumber) {
            return "모든 필수 데이터를 입력해야 합니다.";
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            return "올바른 이메일 형식이 아닙니다.";
        }

        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/;
        if (!passwordRegex.test(password)) {
            return "비밀번호는 영문 대소문자, 숫자를 포함한 8~20자리여야 합니다.";
        }
        if (password !== confirmPassword) {
            return "비밀번호 확인이 일치하지 않습니다.";
        }
        if (displayName.length < 2 || displayName.length > 15) {
            return "닉네임(표시 이름)을 2자 이상 15자 이하로 입력해 주세요.";
        }
        const phoneRegex = /^01(?:0|1|[6-9])(?:\d{3}|\d{4})\d{4}$/;
        if (!phoneRegex.test(phoneNumber)) {
            return "유효한 휴대폰 번호 형식이 아닙니다. (예: 01012345678)";
        }
        return null; // 모든검사 포함
    };
    const handleSubmit = async (e) => {
        e.preventDefault();

        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setIsSubmitting(true);
        setError(null);

        try {
            // 백엔드 DTO 필드에 맞게 데이터 전송
            const response = await apiClient.post('/users/join', {
                email: formData.email,
                password: formData.password,
                address: formData.address,
                displayName: formData.displayName,
                phoneNumber: formData.phoneNumber,
            });

            console.log('회원가입 성공:', response.data);
            alert('회원가입에 성공했습니다! 로그인 페이지로 이동합니다.');

            // 로그인 페이지로 리디렉션
            navigate('/login');

        } catch (err) {
            console.error('회원가입 오류:', err);

            // 백엔드에서 받은 상세 에러 메시지 표시 (예: 이메일 중복)
            if (err.response && err.response.data && err.response.data.message) {
                setError(err.response.data.message);
            } else {
                setError('회원가입 중 알 수 없는 오류가 발생했습니다. 서버 상태를 확인하세요.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div style={styles.container}>
            <h2>회원가입</h2>
            <form onSubmit={handleSubmit} style={styles.form}>

                <input
                    type="email"
                    name="email"
                    placeholder="* 이메일 주소"
                    value={formData.email}
                    onChange={handleChange}
                    style={styles.input}
                    disabled={isSubmitting}
                />

                <input
                    type="password"
                    name="password"
                    placeholder="* 비밀번호 (영문, 숫자 포함 8~20자)"
                    value={formData.password}
                    onChange={handleChange}
                    style={styles.input}
                    disabled={isSubmitting}
                />

                <input
                    type="password"
                    name="confirmPassword"
                    placeholder="* 비밀번호 확인"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    style={styles.input}
                    disabled={isSubmitting}
                />

                <input
                    type="text"
                    name="displayName"
                    placeholder="* 닉네임 (2자 이상 15자 이하)"
                    value={formData.displayName}
                    onChange={handleChange}
                    style={styles.input}
                    disabled={isSubmitting}
                />

                <input
                    type="tel"
                    name="phoneNumber"
                    placeholder="* 휴대폰 번호 (예: 01012345678)"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    style={styles.input}
                    disabled={isSubmitting}
                />

                <input
                    type="text"
                    name="address"
                    placeholder="주소 (선택)"
                    value={formData.address}
                    onChange={handleChange}
                    style={styles.input}
                    disabled={isSubmitting}
                />

                {/* 에러 메시지 표시 */}
                {error && <p style={styles.error}>{error}</p>}

                <button
                    type="submit"
                    style={styles.button}
                    disabled={isSubmitting}
                >
                    {isSubmitting ? '가입 처리 중...' : '회원가입 완료'}
                </button>
            </form>
            <p style={styles.loginLink}>
                이미 계정이 있으신가요? <span onClick={() => navigate('/login')} style={styles.link}>로그인</span>
            </p>
        </div>
    );
};

// 일단 임시 스타일
const styles = {
    container: {
        maxWidth: '400px',
        margin: '50px auto',
        padding: '20px',
        border: '1px solid #ccc',
        borderRadius: '8px',
        textAlign: 'center',
        boxShadow: '0 4px 8px rgba(0,0,0,0.1)',
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: '15px',
    },
    input: {
        padding: '12px',
        borderRadius: '4px',
        border: '1px solid #ddd',
        fontSize: '16px',
    },
    button: {
        padding: '12px',
        backgroundColor: '#007bff',
        color: 'white',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
        fontSize: '16px',
        fontWeight: 'bold',
        transition: 'background-color 0.3s',
    },
    error: {
        color: '#dc3545',
        fontSize: '14px',
        fontWeight: 'bold',
        backgroundColor: '#f8d7da',
        padding: '10px',
        borderRadius: '4px',
    },
    loginLink: {
        marginTop: '20px',
        fontSize: '14px',
    },
    link: {
        color: '#007bff',
        cursor: 'pointer',
        fontWeight: 'bold',
        textDecoration: 'underline',
    }
};

export default Signup;