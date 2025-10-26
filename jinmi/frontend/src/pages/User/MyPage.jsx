
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../../api/apiClient.js";
import { useAuth } from "../../context/AuthContext.jsx"
import axios from 'axios';
import styles from './css/MyPage.module.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const initialMyInfo = { /* ... */ };

const STEPS = {
    INFO_DISPLAY: 1,
    PASSWORD_CHECK: 2,
    INFO_EDIT: 3
};


export default function MyPage() {
    const navigate = useNavigate();
    const { handleLogout } = useAuth();
    const [myInfo, setMyInfo] = useState(initialMyInfo);
    const [isSocialUser, setIsSocialUser] = useState(false);
    const [formData, setFormData] = useState({
        currentPassword: "",
        newPassword: "",
        displayName: "",
        phoneNumber: "",
        address: ""
    });

    const [step, setStep] = useState(STEPS.INFO_DISPLAY);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState("");

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
                console.error("Daum Postcode API 로드 시간 초과.");
            }
        }, 100);

        return () => clearInterval(checkApi);
    }, []);

    const getAddressParts = () => {
        if (!formData.address) return { base: '', detail: '' };

        const parts = formData.address.split(' 상세: ');
        const base = parts[0].trim();
        const detail = parts.length > 1 ? parts[1].trim() : '';

        return { base, detail };
    };


    const handleAddressSearch = () => {
        if (!isApiLoaded) {
            alert("주소 검색 시스템 로딩 중입니다. 잠시 후 다시 시도해 주세요.");
            return;
        }

        new window.daum.Postcode({
            oncomplete: (data) => {
                let baseAddress = data.address;
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

                const existingDetail = formData.address.includes(' 상세: ') ?
                    formData.address.split(' 상세: ')[1].trim() : '';

                const newFullAddress = `${baseAddress} 상세: ${existingDetail}`;

                setFormData(prevFormData => ({
                    ...prevFormData,
                    address: newFullAddress,
                }));

                const detailInput = document.getElementById('detailAddressUpdateInput');
                if(detailInput) {
                    detailInput.focus();
                }

                if (error) setError(null);
            }
        }).open();
    };

    const fetchMyInfo = async () => {
        setLoading(true);
        setError(null);
        try{

            const response = await apiClient.get("/users/myInfo");

            const data = response.data.data;
            setMyInfo(data);

            const isSocial = !!data.provider && data.provider !== 'NONE' && data.provider !== 'LOCAL';
            setIsSocialUser(isSocial);

            setFormData(prev => ({
                ...prev,
                displayName: data.displayName,
                phoneNumber: data.phoneNumber || "",
                address: data.address || ""
            }));

        } catch (err) {
            console.error("내 정보 불러오기 실패:", err);
            const errorMessage = err.response?.data?.message || "정보를 불러오는데 실패했습니다.";
            setError(errorMessage);
            setMessage(errorMessage);

            setIsSocialUser(false);

            if (!localStorage.getItem('accessToken')) {
                handleLogout();
                navigate("/login");
            }

        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMyInfo();
    }, []);


    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'detailAddress'){
            const addressParts = getAddressParts();

            if (addressParts.base.length > 0){
                const newDetail = value.trim();
                const fullAddress = `${addressParts.base} 상세: ${newDetail}`;

                setFormData(prev =>({
                    ...prev,
                    address: fullAddress,
                }));
            } else {
                console.warn("기본 주소 설정 전에 상세 주소를 입력할 수 없습니다.");
            }
        } else {
            setFormData(prev => ({ ...prev, [name]: value }));
        }

        if (error) setError(null);
    };
    const handleEditClick = () => {
        setError(null);
        setMessage("");
        if (isSocialUser) {
            setStep(STEPS.INFO_EDIT);
            setMessage("소셜 로그인 사용자입니다. 바로 정보를 수정할 수 있습니다.");
        } else {
            setStep(STEPS.PASSWORD_CHECK);
        }
    };

    const handlePasswordCheck = async (password) => {
        setError(null);
        setMessage("");

        try {
            await apiClient.post("/users/checkPassword", {
                email: myInfo.email,
                password: password
            });

            setStep(STEPS.INFO_EDIT);
            setMessage("비밀번호 확인 완료. 이제 정보를 수정할 수 있습니다.");

            setFormData(prev => ({
                ...prev,
                currentPassword: password //
            }));

            return true;

        } catch (err) {
            console.error("비밀번호 확인 실패:", err.response? err.response.data : err);

            const fixedErrorMessage = "비밀번호가 일치하지 않습니다.";
            setError(fixedErrorMessage);

            setMessage("");

            return false;
        }
    };



    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage("");
        setError(null);

        const updateRequest = {
            currentPassword: formData.currentPassword,
            newPassword: formData.newPassword,
            displayName: formData.displayName,
            phoneNumber: formData.phoneNumber,
            address: formData.address.trim(),
        };

        if (!formData.newPassword) {
            delete updateRequest.newPassword;
        }
        try {
            await apiClient.put("/users/myUpdateInfo", updateRequest);

            setMessage("회원정보가 성공적으로 수정되었습니다.");

            window.location.reload();


        } catch (err) {
            console.error("정보 수정 실패:", err.response ? err.response.data : err);
            const errorMessage = err.response?.data?.message || "정보 수정에 실패했습니다.";

            if (!localStorage.getItem('accessToken')) {

                handleLogout();
                navigate("/login");
                return;
            }


            let displayMessage;
            if(errorMessage === "PASSWORD_MISPATTERN"){
                displayMessage = "영문 대소문자, 숫자를 포함한 8~20자리를 입력하세요.";
            }else if(errorMessage === "DUPLICATE_PHONENUMBER"){
                displayMessage = "이미 가입된 휴대전화번호 입니다."
            }else if(errorMessage === "DUPLICATE_NICKNAME"){
                displayMessage = "이미 가입된 닉네임 입니다."
            }else if(errorMessage === "PHONENUMBER_MISPATTERN"){
                displayMessage = "휴대폰 번호 형식이 올바르지 않습니다. 예(: 010xxxxxxxx)"
            }else{
                displayMessage = errorMessage;
            }
            setError(displayMessage);
        }
    };

    if (loading) {
        return <div>로딩 중...</div>;
    }
    if (error && step === STEPS.INFO_DISPLAY) {
        return (
            <div className={styles['my-page-container']}>
                <h2>마이 페이지</h2>
                <div className={`${styles['status-message']} ${styles.error}`}>
                    <p>내 정보를 불러오는 데 실패했습니다.</p>
                    <p>오류: {error}</p>
                </div>
            </div>
        );
    }

    let content;

    if (step === STEPS.INFO_DISPLAY) {
        content = (
            <InfoDisplay
                myInfo={myInfo}
                onEditClick={handleEditClick}
                onDelete={() => navigate("/deleteAccount")}
                isLoading={loading}
            />
        );
    } else if (step === STEPS.PASSWORD_CHECK) {
        content = (
            <PasswordCheckForm
                email={myInfo.email}
                onConfirm={handlePasswordCheck}
                onCancel={() => {window.location.reload();}}
                errorMessage={error}
                clearError={() => setError(null)}
            />
        );
    } else if (step === STEPS.INFO_EDIT) {
        content = (
            <UpdateForm
                myInfo={myInfo}
                formData={formData}
                handleChange={handleChange}
                handleSubmit={handleSubmit}
                onCancel={() => {window.location.reload();}}
                errorMessage={error}
                getAddressParts={getAddressParts}
                handleAddressSearch={handleAddressSearch}
                isApiLoaded={isApiLoaded}
                isSocialUser={isSocialUser}
            />
        );
    }


    return (
        <div className={styles['my-page-container']}>
            <h2>마이 페이지</h2>
            {content}
        </div>
    );
}


const InfoDisplay = ({ myInfo, onEditClick, onDelete, isLoading }) => (
    <div className={styles['info-display']}>
        <div className={styles['info-item']}><strong>이메일:</strong> <span>{myInfo.email}</span></div>
        <div className={styles['info-item']}><strong>닉네임:</strong> <span>{myInfo.displayName}</span></div>
        <div className={styles['info-item']}><strong>휴대폰 번호:</strong> <span>{myInfo.phoneNumber || "미입력"}</span></div>
        <div className={styles['info-item']}><strong>주소:</strong> <span>{myInfo.address || "미입력"}</span></div>

        <div className={styles['button-group']}>
            <button className={styles['primary-btn']} onClick={onEditClick} disabled={isLoading}>

                회원 정보 수정
            </button>
            <button className={styles['danger-btn']} onClick={onDelete} disabled={isLoading}>
                회원 탈퇴
            </button>
        </div>
    </div>
);



const PasswordCheckForm = ({ email, onConfirm, onCancel, errorMessage, clearError }) => {
    const [password, setPassword] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handlePasswordChange = (e) => {
        setPassword(e.target.value);
        clearError();
    };

    const handleConfirm = async (e) => {
        e.preventDefault();
        clearError();
        setIsSubmitting(true);
        await onConfirm(password);
        setIsSubmitting(false);
    };

    return (
        <div className={styles['password-check-form']}>
            <h3>회원 정보 수정을 위해 비밀번호를 다시 한 번 확인해 주세요.</h3>

            <form onSubmit={handleConfirm}>
                <div className={styles['form-group']}>
                    <label>이메일:</label>
                    <input type="text" value={email} readOnly disabled />
                </div>

                <div className={`${styles['form-group']} ${styles.required}`}>
                    <label htmlFor="check-password">현재 비밀번호*</label>
                    <input
                        type="password"
                        id="check-password"
                        value={password}
                        onChange={handlePasswordChange}
                        disabled={isSubmitting}
                        required
                        autoFocus
                    />
                </div>

                {errorMessage && <p className={styles['error-text']}>{errorMessage}</p>}

                <div className={styles['button-group']}>
                    <button type="submit" className={styles['primary-btn']} disabled={isSubmitting || !password}>
                        {isSubmitting ? "확인 중..." : "확인"}
                    </button>
                    <button type="button" className={styles['secondary-btn']} onClick={onCancel} disabled={isSubmitting}>
                        취소
                    </button>
                </div>
            </form>
        </div>
    );
};



const UpdateForm = ({
                        myInfo,
                        formData,
                        handleChange,
                        handleSubmit,
                        onCancel,
                        errorMessage,
                        getAddressParts,
                        handleAddressSearch,
                        isApiLoaded,
                        isSocialUser
                    }) => {

    const [isPasswordChanging, setIsPasswordChanging] = useState(false);
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');

    const addressParts = getAddressParts();

    const handleDetailAddressChange = (e) => {
        handleChange({
            target: {
                name: 'detailAddress',
                value: e.target.value
            }
        });
    };
    const handlePasswordToggle = () => {
        setIsPasswordChanging(prev => !prev);
        setConfirmNewPassword('');
        handleChange({ target: { name: 'newPassword', value: '' } });
        setPasswordError('');
    };

    const handleConfirmNewPasswordChange = (e) => {
        const value = e.target.value;
        setConfirmNewPassword(value);
        if (value !== formData.newPassword) {
            setPasswordError('새 비밀번호와 확인이 일치하지 않습니다.');
        } else {
            setPasswordError('');
        }
    };

    const handleFormSubmit = (e) => {
        e.preventDefault();

        if (isPasswordChanging) {
            if (formData.newPassword !== confirmNewPassword) {
                setPasswordError('새 비밀번호와 확인이 일치하지 않습니다.');
                return;
            }
            if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/.test(formData.newPassword)) {
                setPasswordError('비밀번호는 영문 대소문자, 숫자를 포함한 8~20자리여야 합니다.');
                return;
            }
        }
        handleSubmit(e);
    };

    return (
        <form onSubmit={handleFormSubmit} className={styles['update-form']}>
            <h3>내 정보 수정</h3>

            <div className={styles['form-group']}>
                <label>이메일</label>
                <input type="text" value={myInfo.email} readOnly disabled />
            </div>
            {!isSocialUser && (
                <>
                    <div className={styles['form-group']}>
                        <label>비밀번호 변경</label>
                        <button
                            type="button"
                            className={styles['secondary-btn']}
                            onClick={handlePasswordToggle}
                            style={{ marginLeft: '10px' }}
                        >
                            {isPasswordChanging ? '변경 취소' : '비밀번호 변경'}
                        </button>
                    </div>

                    {isPasswordChanging && (
                        <>
                            <div className={`${styles['form-group']} ${styles.required}`}>
                                <label htmlFor="newPassword">새 비밀번호*</label>
                                <input
                                    type="password"
                                    id="newPassword"
                                    name="newPassword"
                                    value={formData.newPassword}
                                    onChange={handleChange}
                                    required={isPasswordChanging}
                                    placeholder="영문 대소문자, 숫자 포함 8~20자"
                                />
                            </div>

                            <div className={`${styles['form-group']} ${styles.required}`}>
                                <label htmlFor="confirmNewPassword">새 비밀번호 확인*</label>
                                <input
                                    type="password"
                                    id="confirmNewPassword"
                                    name="confirmNewPassword"
                                    value={confirmNewPassword}
                                    onChange={handleConfirmNewPasswordChange}
                                    required={isPasswordChanging}
                                    placeholder="새 비밀번호를 다시 한번 입력해주세요"
                                />
                                {passwordError && <small className={styles['error-text']}>{passwordError}</small>}
                            </div>
                        </>
                    )}
                </>
            )}


            <div className={`${styles['form-group']} ${styles.required}`}>
                <label htmlFor="displayName">닉네임*</label>
                <input
                    type="text"
                    id="displayName"
                    name="displayName"
                    value={formData.displayName}
                    onChange={handleChange}
                    required
                />
            </div>

            <div className={styles['form-group']}>
                <label htmlFor="phoneNumber">휴대폰 번호</label>
                <input
                    type="text"
                    id="phoneNumber"
                    name="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    placeholder="010xxxxxxxx 형식"
                />
            </div>

            <div className={styles['address-group']}>
                <div className={`${styles['form-group']} ${styles.required}`} style={{ marginBottom: '10px' }}>
                    <label>주소*</label>
                    <div className={styles['input-group']}>
                        <input
                            type="text"
                            name="baseAddressDisplay"
                            placeholder={isApiLoaded ? "주소 검색 버튼을 클릭하여 기본 주소 입력" : "주소 검색 시스템 로딩 중..."}
                            value={addressParts.base}
                            readOnly
                            onClick={handleAddressSearch}
                            className={styles.input}
                            disabled={!isApiLoaded}
                        />
                        <button
                            type="button"
                            onClick={handleAddressSearch}
                            className={styles['secondary-btn']}
                            disabled={!isApiLoaded}
                        >
                            주소 검색
                        </button>
                    </div>
                </div>

                <div className={styles['form-group']}>
                    <input
                        type="text"
                        name="detailAddress"
                        placeholder="나머지 상세 주소 입력 (선택 사항)"
                        value={addressParts.detail}
                        onChange={handleDetailAddressChange}
                        className={styles.input}
                        disabled={!isApiLoaded || addressParts.base.length === 0}
                        id="detailAddressUpdateInput"
                    />
                </div>
            </div>

            {errorMessage && <p className={styles['error-text']}>{errorMessage}</p>}


            <div className={styles['button-group']}>
                <button type="submit" className={styles['primary-btn']}>수정 완료</button>
                <button
                    type="button"
                    className={styles['secondary-btn']}
                    onClick={onCancel}
                >
                    취소
                </button>
            </div>
        </form>
    );
};