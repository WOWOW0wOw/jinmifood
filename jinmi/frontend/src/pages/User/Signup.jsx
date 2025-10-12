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

    const [authCode, setAuthCode] = useState('');
    const [isEmailSent, setIsEmailSent] = useState(false); // 인증 코드를 발송했는지 여부
    const [isEmailVerified, setIsEmailVerified] = useState(false); // 최종 인증이 완료되었는지 여부
    const [emailAuthMessage, setEmailAuthMessage] = useState('');
    const [emailTimer, setEmailTimer] = useState(0); // 인증 시간 타이머 (초)

    // 다음 주소 api
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

    //이메일 인증 타이머 관리
    useEffect(() => {
        let timerId;
        if (isEmailSent && !isEmailVerified && emailTimer > 0) {
            timerId = setInterval(() => {
                setEmailTimer(prev => prev - 1);
            }, 1000);
        } else if (emailTimer === 0 && isEmailSent) {
            // 타이머가 0이 되면 만료 처리
            setIsEmailSent(false);
            setEmailAuthMessage('인증 시간이 만료되었습니다. 다시 요청해 주세요.');
        }

        return () => clearInterval(timerId);
    }, [isEmailSent, isEmailVerified, emailTimer]);

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

        } else if (name === 'authCode'){ // 인증 코드 입력 처리
            setAuthCode(value.slice(0,6)); // 6자리 제한
            setEmailAuthMessage(''); // 코드 입력하면 메시지 초기화
        } else {
            setFormData(prev =>({
                ...prev,
                [name]: value,
            }));
            if (name === 'email' && isEmailVerified) {
                setIsEmailVerified(false);
                setIsEmailSent(false);
                setEmailTimer(0);
                setEmailAuthMessage('');
            }
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

    const handleEmailSend = async () => {
        const email = formData.email.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailRegex.test(email)) {
            setError('올바른 이메일 형식이 아닙니다.');
            return;
        }

        if (isEmailVerified) {
            setEmailAuthMessage('이미 이메일 인증이 완료되었습니다.');
            return;
        }

        setIsSubmitting(true);
        setEmailAuthMessage('');
        setError(null);

        try{
            const response = await apiClient.post('/email/send',{ email });
            // 성공 시 상태 업데이트
            setIsEmailSent(true);
            setIsEmailVerified(false);
            setAuthCode('');
            setEmailTimer(300); // 5분 = 300초
            setEmailAuthMessage(response.data.message || '인증 코드가 발송되었습니다. (5분 유효)');
        }catch (err){
            console.error('인증 코드 발송 오류:', err);
            let displayError = '이메일 발송에 실패했습니다. 이메일 주소를 확인하거나 잠시 후 다시 시도해주세요.';

            if (err.response && err.response.data) {
                const errorData = err.response.data;

                if (errorData.errors && Array.isArray(errorData.errors) && errorData.errors.length > 0) {
                    displayError = errorData.errors[0];
                }
                else if (errorData.message) {
                    displayError = errorData.message;
                }
            }

            setEmailAuthMessage(displayError);
            setIsEmailSent(false); // 재발송 가능하도록
        } finally {
            setIsSubmitting(false);
        }
    };
    // 인증 코드 검증 확인
    const handleEmailVerify = async () => {
        const email = formData.email.trim();
        const code = authCode.trim();

        if (!code || code.length !== 6) {
            setEmailAuthMessage('6자리 인증 코드를 정확히 입력해 주세요.');
            return;
        }

        if (!isEmailSent) {
            setEmailAuthMessage('먼저 이메일 인증 코드를 요청해 주세요.');
            return;
        }

        setIsSubmitting(true);
        setEmailAuthMessage('');
        setError(null);

        try{
            const response = await apiClient.post('/email/verify',{ email, code });
            // 성공 시 상태 업데이트
            setIsEmailVerified(true);
            setIsEmailSent(false); // 타이머 중지
            setEmailTimer(0);
            setEmailAuthMessage(response.data.message || '이메일 인증이 성공적으로 완료되었습니다.');
        }catch (err){
            console.error('인증 코드 검증 오류:', err);
            let displayError = '인증 코드 확인에 실패했습니다. 코드를 다시 확인해 주세요.';

            if (err.response && err.response.data) {
                const errorData = err.response.data;

                if (errorData.errors && Array.isArray(errorData.errors) && errorData.errors.length > 0) {
                    displayError = errorData.errors[0];
                }
                else if (errorData.message) {
                    displayError = errorData.message;
                }
            }

            setIsEmailVerified(false);
            // 만료 등의 오류 메시지는 유지
            setEmailAuthMessage(displayError);
        } finally {
            setIsSubmitting(false);
        }
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
        const {email, password, confirmPassword, displayName, phoneNumber,address} = formData;

        if(!isEmailVerified){
            console.log("Validation Failed: 이메일 인증 미완료. isEmailVerified:", isEmailVerified);
            return "이메일 인증을 완료해야 합니다.";
        }


        if (!email || !password || !confirmPassword || !displayName || !phoneNumber || !address) {
            console.log("Validation Failed: 필수 데이터 누락");
            return "모든 필수 데이터를 입력하고 주소 검색을 완료해야 합니다.";
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            console.log("Validation Failed: 이메일 형식 오류");
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
                <div className={styles.formSection}>
                <h3 className={styles.sectionTitle}>사이트 이용 정보 입력</h3>

                <div className={styles.inputGroupBlock}>
                    <label className={styles.label}>이메일 주소 (아이디)</label>
                    <div className={styles.inputGroup}>
                <input
                    type="email"
                    name="email"
                    placeholder="* 이메일 주소"
                    value={formData.email}
                    onChange={handleChange}
                    className={styles.input}
                    disabled={isSubmitting || isEmailVerified}  // 인증 완료 시 비활성화
                />
                        <button
                            type="button"
                            onClick={handleEmailSend}
                            className={isEmailVerified ? styles.verifiedButton : styles.checkButton}
                            disabled={isSubmitting || isEmailVerified}
                        >
                            {isEmailVerified ? '인증 완료' : (isEmailSent ? '재요청' : '인증 요청')}
                        </button>
                    </div>
                </div>
                    {/*인증코드 입력 필드 isEmailSent 상태일 때만 표시*/}
                    {isEmailSent && !isEmailVerified && (
                        <div className={styles.inputGroupBlock} style={{marginTop: '10px'}}>
                            <label className={styles.label}>* 인증 코드 입력</label>
                            <div className={styles.inputGroup}>
                                <input
                                    type="text"
                                    name="authCode"
                                    placeholder="6자리 인증 코드를 입력하세요"
                                    value={authCode}
                                    onChange={handleChange} // handleChange 사용
                                    className={styles.input}
                                    disabled={isSubmitting}
                                    maxLength={6}
                                />
                                <button
                                    type="button"
                                    onClick={handleEmailVerify}
                                    className={styles.checkButton}
                                    disabled={isSubmitting}
                                >
                                    확인
                                </button>
                            </div>
                        </div>
                    )}
                    {/* 타이머 및 인증 메시지 */}
                    {(emailAuthMessage || isEmailVerified || (isEmailSent && emailTimer > 0)) && (
                        <p className={isEmailVerified ? styles.successMessage : styles.errorMessage}
                           style={{
                               marginTop: '-10px',
                               marginBottom: isEmailVerified ? '15px' : '0'
                           }}>
                            {emailAuthMessage}
                            {isEmailSent && emailTimer > 0 && !isEmailVerified && (
                                <span className={styles.timer}> ({Math.floor(emailTimer / 60)}:{('0' + emailTimer % 60).slice(-2)})</span>
                            )}
                        </p>
                    )}

                <div className={styles.inputGroupBlock}>
                    <label className={styles.label}>비밀번호</label>
                <input
                    type="password"
                    name="password"
                    placeholder="* 비밀번호 (영문, 숫자 포함 8~20자)"
                    value={formData.password}
                    onChange={handleChange}
                    className={styles.input}
                    disabled={isSubmitting}
                />
                </div>
                <div className={styles.inputGroupBlock}>
                    <label className={styles.label}>비밀번호 확인</label>
                <input
                    type="password"
                    name="confirmPassword"
                    placeholder="* 비밀번호를 다시 입력하세요"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className={styles.input}
                    disabled={isSubmitting}
                />
                    </div>
                </div>




                <div className={styles.formSection}>
                    <h3 className={styles.sectionTitle}>개인 정보 입력</h3>
                    <div className={styles.inputGroupBlock}>
                    <label className={styles.label}>닉네임</label>
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
                    </div>

                    <div className={styles.inputGroupBlock}>
                        <label className={styles.label}>휴대폰 번호</label>
                        <input
                            type="tel"
                            name="phoneNumber"
                            placeholder="예: 01012345678"
                            value={formData.phoneNumber}
                            onChange={handleChange}
                            className={styles.input}
                            disabled={isSubmitting}
                        />
                    </div>

                    <div className={styles.inputGroupBlock}>
                        <label className={styles.label}>주소</label>
                        <div className={styles.inputGroup}>
                            <input
                                type="text"
                                name="baseAddressDisplay"
                                placeholder={isApiLoaded ? "주소 검색 버튼을 눌러주세요" : "주소 검색 시스템 로딩 중..."}
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
                    </div>

                    {/* 상세 주소 입력 필드 */}
                    <div className={styles.inputGroupBlock}>
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
                    </div>

                </div>

                {error && <p className={styles.error}>{error}</p>}

                <button
                    type="submit"
                    className={styles.button}
                    disabled={isSubmitting} // 이메일 인증 완료해야 가입
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