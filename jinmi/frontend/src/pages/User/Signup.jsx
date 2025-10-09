import React, {useState,useEffect} from "react";
import {useNavigate} from "react-router-dom";
import apiClient from "../../api/apiClient.js";
import styles from './css/Signup.module.css';

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

    const [isNicknameChecked, setIsNicknameChecked] = useState(false);
    const [nicknameCheckMessage, setNicknameCheckMessage] = useState('');

    const [isApiLoaded, setIsApiLoaded] = useState(false);

    useEffect(() => {
        let attempts = 0;
        const maxAttempts = 50;
        const checkApi = setInterval(() => {
            if (window.daum && window.daum.Postcode) {
                setIsApiLoaded(true);
                clearInterval(checkApi);
            }

            attempts++;
            if (attempts >= maxAttempts) {
                clearInterval(checkApi);
                console.error("Daum Postcode API 로드 시간 초과. 네트워크 상태를 확인하세요.");
            }
        }, 100); // 0.1초마다 확인

        return () => clearInterval(checkApi);
    }, []);

    const getAddressParts = () => {
        if (!formData.address) return { base: '', detail: '' };

        const parts = formData.address.split('상세:');
        const base = parts[0].trim();
        const detail = parts.length > 1 ? parts[1].trim() : '';

        return { base, detail };
    };

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'detailAddress'){
            if (formData.address.includes('상세:')){
                const baseAddress = formData.address.split('상세:')[0].trim();
                const fullAddress = `${baseAddress} 상세: ${value.trim()}`; // 'baseAddress' 사용

                setFormData(prev =>({
                    ...prev,
                    address: fullAddress,
                }));
            } else{
                if (value.trim() !== ''){
                    setNicknameCheckMessage('상세 주소 입력 전, 반드시 "주소 검색"을 먼저 완료해야 합니다.');
                    setIsNicknameChecked(false);
                }
            }

        }else{
            setFormData(prev =>({
                ...prev,
                [name]: value,
            }));
        }

        if (error) setError(null);

        if(name === 'displayName'){
            setIsNicknameChecked(false);
            setNicknameCheckMessage('');
        }
    };

    const handleAddressSearch = () => {
        if (!isApiLoaded) {
            console.error("Daum Postcode API가 아직 로드되지 않았습니다.");
            alert("주소 검색 시스템 로딩 중입니다. 잠시 후 다시 시도해 주세요.");
            return;
        }

        new window.daum.Postcode({
            oncomplete: (data) => {
                let baseAddress = data.address; // 기본 주소
                let extraAddress = '';

                if (data.userSelectedType === 'R') {
                    if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
                        extraAddress += data.bname;
                    }
                    if (data.buildingName !== '') {
                        extraAddress += (extraAddress !== '' ? ', ' + data.buildingName : data.buildingName);
                    }
                    baseAddress += (extraAddress !== '' ? ' (' + extraAddress + ')' : '');
                }

                // 기존 상세 주소 유지 로직
                const existingDetail = formData.address.includes('상세:') ?
                    formData.address.split('상세:')[1].trim() : '';

                const newFullAddress = `${baseAddress} 상세: ${existingDetail}`;

                setFormData(prevFormData => ({
                    ...prevFormData,
                    address: newFullAddress,
                }));

                const detailInput = document.getElementById('detailAddressInput');
                if(detailInput) {
                    detailInput.focus();
                }

                if (error) setError(null);
            }
        }).open();
    };


    const handleNicknameCheck = async () => {
        const displayName = formData.displayName.trim();

        const reservedKeywords = [
            'admin', 'administrator', 'system', 'root', 'super', '관리자', '운영자',
            'master', 'webmaster', 'support', 'cs', 'ceo','시발','병신','ㅈ병신','ㅅ발','시발롬','좆까','시발련','시발년','개새끼','개병신','새끼','십새끼'
        ];

        const isReserved = reservedKeywords.some(keyword =>
            displayName.toLowerCase().includes(keyword)
        );
        if (isReserved) {
            setNicknameCheckMessage("사용할 수 없는 키워드 가 포함되어 있습니다.");
            setIsNicknameChecked(false);
            return;
        }

        if (displayName.length < 2 || displayName.length > 15) {
            setNicknameCheckMessage("닉네임을 2자 이상 15자 이하로 입력해 주세요.");
            setIsNicknameChecked(false);
            return;
        }
        if (!displayName) {
            setNicknameCheckMessage("닉네임을 입력해 주세요.");
            setIsNicknameChecked(false);
            return;
        }

        setIsSubmitting(true);
        setNicknameCheckMessage('');
        setIsNicknameChecked(false);
        try {
            const response = await apiClient.get(`/users/checkNickname`, {
                params: { displayName: displayName }
            });

            setNicknameCheckMessage(`'${displayName}'는 사용 가능한 닉네임입니다.`);
            setIsNicknameChecked(true);

        } catch (err) {
            let displayMessage = "닉네임 중복 확인 중 알 수 없는 오류가 발생했습니다.";

            if (err.response && err.response.data) {
                const serverMessage = err.response.data.message;

                if (serverMessage === 'DUPLICATE_NICKNAME') {
                    displayMessage = "해당 닉네임은 이미 사용 중입니다. 다른 닉네임을 사용해 주세요.";

                } else if (serverMessage) {
                    displayMessage = serverMessage;
                }
            }

            setNicknameCheckMessage(displayMessage);
            setIsNicknameChecked(false);
        } finally {
            setIsSubmitting(false);
        }
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

        const addressParts = getAddressParts();
        if (addressParts.detail.trim() !== '' && addressParts.base.trim() === ''){
            return "상세 주소 입력 전, 반드시 주소 검색을 통해 기본 주소를 먼저 설정해야 합니다.";
        }

        if (!isNicknameChecked){
            return "닉네임 중복 확인을 완료해야 합니다.";
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
            const response = await apiClient.post('/users/join', {
                email: formData.email,
                password: formData.password,
                address: formData.address.trim(), // 마지막에 trim()으로 공백 제거
                displayName: formData.displayName,
                phoneNumber: formData.phoneNumber,
            });

            console.log('회원가입 성공:', response.data);
            alert('회원가입에 성공했습니다! 로그인 페이지로 이동합니다.');

            // 로그인 페이지로 리디렉션
            navigate('/login');

        } catch (err) {
            console.error('회원가입 오류:', err);

            let displayError = '회원가입 중 알 수 없는 오류 발생 서버 상태 확인';

            if (err.response && err.response.data && err.response.data.message) {
                const serverMessage = err.response.data.message;
                if (serverMessage === 'DUPLICATE_EMAIL') {
                    displayError = "이미 사용 중인 이메일입니다.";
                } else if (serverMessage === 'DUPLICATE_NICKNAME') {
                    displayError = "이미 사용 중인 닉네임 입니다. 중복 확인을 해주세요."
                } else if (serverMessage === 'DUPLICATE_PHONENUMBER') {
                    displayError = "이미 사용 중인 전화번호입니다.";
                } else {
                    displayError = serverMessage;
                }
            }
            setError(displayError);
        } finally {
            setIsSubmitting(false);
        }
    };

    const addressParts = getAddressParts();


    return (
        <div className={styles.container}>
            <h2>회원가입</h2>
            <form onSubmit={handleSubmit} className={styles.form}>

                <input
                    type="email"
                    name="email"
                    placeholder="* 이메일 주소"
                    value={formData.email}
                    onChange={handleChange}
                    className={styles.input}
                    disabled={isSubmitting}
                />
                <input
                    type="password"
                    name="password"
                    placeholder="* 비밀번호 (영문, 숫자 포함 8~20자)"
                    value={formData.password}
                    onChange={handleChange}
                    className={styles.input}
                    disabled={isSubmitting}
                />
                <input
                    type="password"
                    name="confirmPassword"
                    placeholder="* 비밀번호 확인"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className={styles.input}
                    disabled={isSubmitting}
                />

                <div className={styles.inputGroup}>
                    <input
                        type="text"
                        name="displayName"
                        placeholder="* 닉네임 (2자 이상 15자 이하)"
                        value={formData.displayName}
                        onChange={handleChange}
                        className={styles.input}
                        disabled={isSubmitting}
                    />
                    <button
                        type="button"
                        onClick={handleNicknameCheck}
                        className={styles.checkButton}
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? '확인 중...' : '중복 확인'}
                    </button>
                </div>
                {nicknameCheckMessage && (
                    <p className={isNicknameChecked ? styles.successMessage : styles.errorMessage}>
                        {nicknameCheckMessage}
                    </p>
                )}
                <input
                    type="tel"
                    name="phoneNumber"
                    placeholder="* 휴대폰 번호 (예: 01012345678)"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    className={styles.input}
                    disabled={isSubmitting}
                />

                {/* 주소 검색 필드 */}
                <div className={styles.inputGroup}>
                    <input
                        type="text"
                        name="baseAddressDisplay"
                        placeholder={isApiLoaded ? "주소 (검색 버튼을 눌러주세요)" : "주소 검색 시스템 로딩 중..."}
                        value={addressParts.base}
                        readOnly
                        onClick={handleAddressSearch}
                        className={styles.input}
                        disabled={isSubmitting || !isApiLoaded}
                    />
                    <button
                        type="button"
                        onClick={handleAddressSearch}
                        className={styles.checkButton}
                        disabled={isSubmitting || !isApiLoaded}
                    >
                        {isApiLoaded ? '주소 검색' : '로딩 중'}
                    </button>
                </div>

                {/* 상세 주소 입력 필드 */}
                <input
                    type="text"
                    name="detailAddress"
                    placeholder="상세 주소 (선택)"
                    value={addressParts.detail}
                    onChange={handleChange}
                    className={styles.input}
                    disabled={isSubmitting || !isApiLoaded || addressParts.base.length === 0}
                    id="detailAddressInput"
                />

                {/* 에러 메시지 표시 */}
                {error && <p className={styles.error}>{error}</p>}

                <button
                    type="submit"
                    className={styles.button}
                    disabled={isSubmitting}
                >
                    {isSubmitting ? '가입 처리 중...' : '회원가입 완료'}
                </button>
            </form>
            <p className={styles.loginLink}>
                이미 계정이 있으신가요? <span onClick={() => navigate('/login')} className={styles.link}>로그인</span>
            </p>
        </div>
    );
};


export default Signup;