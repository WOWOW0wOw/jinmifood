import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../../api/apiClient.js";
import { useAuth } from "../../context/AuthContext.jsx"
import { MdOutlineEmail, MdLockOutline, MdPersonOutline, MdOutlineSmartphone, MdOutlineLocationOn, MdOutlineSecurity } from 'react-icons/md';
import styles from './css/MyPage.module.css';

const STEPS = {
    INFO_DISPLAY: 1,
    PASSWORD_CHECK_EDIT: 2,
    INFO_EDIT: 3,
    PASSWORD_CHECK_DELETE: 4
};


export default function MyPage() {
    const navigate = useNavigate();
    const { handleLogout } = useAuth();
    const [myInfo, setMyInfo] = useState({});
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

            const isSocial = !!data.provider && data.provider !== 'local';
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
            setStep(STEPS.PASSWORD_CHECK_EDIT);
        }
    };

    const handleDeleteClick = () => {
        setError(null);
        setMessage("");
        if (isSocialUser) {
            navigate("/deleteAccount");
        } else {
            setStep(STEPS.PASSWORD_CHECK_DELETE);
        }
    };

    const checkPassword = async (password) => {
        try {
            await apiClient.post("/users/checkPassword", {
                email: myInfo.email,
                password: password
            });
            return true;
        } catch (err) {
            console.error("비밀번호 확인 실패:", err.response? err.response.data : err);
            setError("비밀번호가 일치하지 않습니다.");
            setMessage("");
            return false;
        }
    };

    const handlePasswordCheckEdit = async (password) => {
        setError(null);
        setMessage("");

        const isVerified = await checkPassword(password);

        if (isVerified) {
            setStep(STEPS.INFO_EDIT);
            setMessage("비밀번호 확인 완료. 이제 정보를 수정할 수 있습니다.");
            setFormData(prev => ({
                ...prev,
                currentPassword: password
            }));
        }
        return isVerified;
    };

    const handlePasswordCheckDelete = async (password) => {
        setError(null);
        setMessage("");

        const isVerified = await checkPassword(password);

        if (isVerified) {
            navigate("/deleteAccount");
        }
        return isVerified;
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

        if (isSocialUser) {
            delete updateRequest.currentPassword;
        }

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
        return <div className={styles.container}>로딩 중...</div>;
    }
    if (error && step === STEPS.INFO_DISPLAY) {
        return (
            <div className={styles.container}>
                <h1 className={styles.greetingHeader}>마이 페이지</h1>
                <div className={styles.error} style={{ textAlign: 'center' }}>
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
                onDelete={handleDeleteClick}
                isLoading={loading}
            />
        );
    } else if (step === STEPS.PASSWORD_CHECK_EDIT) {
        content = (
            <PasswordCheckForm
                email={myInfo.email}
                onConfirm={handlePasswordCheckEdit}
                onCancel={() => {setStep(STEPS.INFO_DISPLAY); setError(null); setMessage("");}}
                errorMessage={error}
                clearError={() => setError(null)}
                purposeText="회원 정보 수정을 위해 비밀번호를 다시 한 번 확인해 주세요."
            />
        );
    } else if (step === STEPS.PASSWORD_CHECK_DELETE) {
        content = (
            <PasswordCheckForm
                email={myInfo.email}
                onConfirm={handlePasswordCheckDelete}
                onCancel={() => {setStep(STEPS.INFO_DISPLAY); setError(null); setMessage("");}}
                errorMessage={error}
                clearError={() => setError(null)}
                purposeText="회원 탈퇴를 위해 비밀번호를 다시 한 번 확인해 주세요."
            />
        );
    } else if (step === STEPS.INFO_EDIT) {
        content = (
            <UpdateForm
                myInfo={myInfo}
                formData={formData}
                handleChange={handleChange}
                handleSubmit={handleSubmit}
                onCancel={() => {setStep(STEPS.INFO_DISPLAY); setError(null); setMessage("");}}
                errorMessage={error}
                getAddressParts={getAddressParts}
                handleAddressSearch={handleAddressSearch}
                isApiLoaded={isApiLoaded}
                isSocialUser={isSocialUser}
            />
        );
    }


    return (
        <div className={styles.container}>
            <h1 className={styles.greetingHeader} style={{ fontSize: '28px' }}>
                마이 페이지
            </h1>
            {message && <p className={styles.successMessage} style={{ textAlign: 'center' }}>{message}</p>}
            {content}
        </div>
    );
}



const InfoDisplay = ({ myInfo, onEditClick, onDelete, isLoading }) => (
    // form 디자인과 유사하게 처리
    <div className={styles.form} style={{ gap: '15px' }}>
        <div className={styles.inputGroupBlock}>
            <label className={styles.label}>이메일</label>
            <div className={styles.inputGroup}>
                <MdOutlineEmail className={styles.inputIcon} />
                <input type="text" value={myInfo.email} readOnly disabled className={styles.input} />
            </div>
        </div>
        <div className={styles.inputGroupBlock}>
            <label className={styles.label}>닉네임</label>
            <div className={styles.inputGroup}>
                <MdPersonOutline className={styles.inputIcon} />
                <input type="text" value={myInfo.displayName} readOnly disabled className={styles.input} />
            </div>
        </div>
        <div className={styles.inputGroupBlock}>
            <label className={styles.label}>휴대폰 번호</label>
            <div className={styles.inputGroup}>
                <MdOutlineSmartphone className={styles.inputIcon} />
                <input type="text" value={myInfo.phoneNumber || "미입력"} readOnly disabled className={styles.input} />
            </div>
        </div>
        <div className={styles.inputGroupBlock}>
            <label className={styles.label}>주소</label>
            <div className={styles.inputGroup}>
                <MdOutlineLocationOn className={styles.inputIcon} />
                <input type="text" value={myInfo.address || "미입력"} readOnly disabled className={styles.input} />
            </div>
        </div>

        <div className={styles.buttonGroup} style={{ marginTop: '30px', gap: '10px' }}>
            <button
                className={styles.submitButton}
                onClick={onEditClick}
                disabled={isLoading}
                style={{
                    width: '50%',
                    fontSize: '16px',
                    marginTop: '0'
                }}
            >
                회원 정보 수정
            </button>
            <button
                className={styles.dangerButton}
                onClick={onDelete}
                disabled={isLoading}
                style={{
                    width: '50%',
                    fontSize: '16px',
                    marginTop: '0'
                }}
            >
                회원 탈퇴
            </button>
        </div>
    </div>
);



const PasswordCheckForm = ({ email, onConfirm, onCancel, errorMessage, clearError, purposeText }) => {
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
        <div className={styles.form}>
            <p className={styles.subHeader} style={{textAlign: 'center', marginBottom: '15px', color: '#000', fontSize: '15px', fontWeight: '600'}}>{purposeText}</p>

            <form onSubmit={handleConfirm} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <div className={styles.inputGroupBlock}>
                    <label className={styles.label}>이메일</label>
                    <div className={styles.inputGroup}>
                        <MdOutlineEmail className={styles.inputIcon} />
                        <input type="text" value={email} readOnly disabled className={styles.input} />
                    </div>
                </div>

                <div className={styles.inputGroupBlock}>
                    <label htmlFor="check-password" className={styles.label}>현재 비밀번호</label>
                    <div className={styles.inputGroup}>
                        <MdLockOutline className={styles.inputIcon} />
                        <input
                            type="password"
                            id="check-password"
                            value={password}
                            onChange={handlePasswordChange}
                            disabled={isSubmitting}
                            required
                            autoFocus
                            className={styles.input}
                        />
                    </div>
                </div>

                {errorMessage && <p className={styles.error} style={{ marginTop: '0', marginBottom: '0' }}>{errorMessage}</p>}

                <div className={styles.buttonGroup} style={{ marginTop: '20px', gap: '15px' }}>
                    <button type="submit" className={styles.submitButton} disabled={isSubmitting || !password} style={{ width: '50%', padding: '15px', fontSize: '16px', marginTop: '0' }}>
                        {isSubmitting ? "확인 중..." : "확인"}
                    </button>
                    <button
                        type="button"
                        className={styles.checkButton}
                        onClick={onCancel}
                        disabled={isSubmitting}
                        style={{
                            width: '50%',
                            padding: '15px',
                            backgroundColor: '#f8f8f8',
                            color: '#333',
                            border: '1px solid #ddd',
                            fontSize: '16px',
                            marginTop: '0'
                        }}
                    >
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
            if (!formData.newPassword) {
                setPasswordError('새 비밀번호를 입력해주세요.');
                return;
            }
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
        <form onSubmit={handleFormSubmit} className={styles.form}>
            <p className={styles.subHeader} style={{textAlign: 'center', color: '#000', fontSize: '15px', fontWeight: '600'}}>회원 정보를 수정해주세요.</p>
            <div className={styles.divider}></div>

            <div className={styles.inputGroupBlock}>
                <label className={styles.label}>이메일</label>
                <div className={styles.inputGroup}>
                    <MdOutlineEmail className={styles.inputIcon} />
                    <input type="text" value={myInfo.email} readOnly disabled className={styles.input} />
                </div>
            </div>

            {!isSocialUser && (
                <div className={styles.inputGroupBlock}>
                    <div className={styles.inputGroup} style={{ justifyContent: 'space-between', marginBottom: '10px' }}>
                        <label className={styles.label} style={{ marginBottom: 0, fontWeight: 'bold' }}>비밀번호 변경</label>
                        <button
                            type="button"
                            onClick={handlePasswordToggle}
                            className={styles.checkButton}
                            style={{
                                padding: '10px 15px',
                                height: 'auto',
                                minWidth: 'auto',
                                backgroundColor: isPasswordChanging ? '#f8f8f8' : '#000',
                                color: isPasswordChanging ? '#333' : 'white',
                                border: isPasswordChanging ? '1px solid #ddd' : 'none'
                            }}
                        >
                            {isPasswordChanging ? '변경 취소' : '변경하기'}
                        </button>
                    </div>

                    {isPasswordChanging && (
                        <>
                            <div className={styles.inputGroupBlock}>
                                <label htmlFor="newPassword" className={styles.label}>새 비밀번호</label>
                                <div className={styles.inputGroup}>
                                    <MdLockOutline className={styles.inputIcon} />
                                    <input
                                        type="password"
                                        id="newPassword"
                                        name="newPassword"
                                        value={formData.newPassword}
                                        onChange={handleChange}
                                        required={isPasswordChanging}
                                        placeholder="영문 대소문자, 숫자 포함 8~20자"
                                        className={styles.input}
                                    />
                                </div>
                            </div>

                            <div className={styles.inputGroupBlock}>
                                <label htmlFor="confirmNewPassword" className={styles.label}>새 비밀번호 확인</label>
                                <div className={styles.inputGroup}>
                                    <MdLockOutline className={styles.inputIcon} />
                                    <input
                                        type="password"
                                        id="confirmNewPassword"
                                        name="confirmNewPassword"
                                        value={confirmNewPassword}
                                        onChange={handleConfirmNewPasswordChange}
                                        required={isPasswordChanging}
                                        placeholder="새 비밀번호를 다시 한번 입력해주세요"
                                        className={styles.input}
                                    />
                                </div>
                                {passwordError && <p className={styles.errorMessage}>{passwordError}</p>}
                            </div>
                            <div className={styles.divider}></div>
                        </>
                    )}
                </div>
            )}


            <div className={styles.inputGroupBlock}>
                <label htmlFor="displayName" className={styles.label}>닉네임</label>
                <div className={styles.inputGroup}>
                    <MdPersonOutline className={styles.inputIcon} />
                    <input
                        type="text"
                        id="displayName"
                        name="displayName"
                        value={formData.displayName}
                        onChange={handleChange}
                        required
                        className={styles.input}
                    />
                </div>
            </div>

            <div className={styles.inputGroupBlock}>
                <label htmlFor="phoneNumber" className={styles.label}>휴대폰 번호</label>
                <div className={styles.inputGroup}>
                    <MdOutlineSmartphone className={styles.inputIcon} />
                    <input
                        type="tel"
                        id="phoneNumber"
                        name="phoneNumber"
                        value={formData.phoneNumber}
                        onChange={handleChange}
                        placeholder="010xxxxxxxx 형식"
                        className={styles.input}
                    />
                </div>
            </div>

            <div className={styles.addressGroup}>
                <div className={styles.inputGroupBlock} style={{ marginBottom: '10px' }}>
                    <label className={styles.label}>주소</label>
                    <div className={styles.inputGroup}>
                        <MdOutlineLocationOn className={styles.inputIcon} style={{ left: '15px', color: '#666' }} />
                        <input
                            type="text"
                            name="baseAddressDisplay"
                            placeholder={isApiLoaded ? "주소 검색" : "주소 검색 시스템 로딩 중..."}
                            value={addressParts.base}
                            readOnly
                            onClick={handleAddressSearch}
                            className={styles.input}
                            disabled={!isApiLoaded}
                            style={{ paddingLeft: '45px' }}
                        />
                        <button
                            type="button"
                            onClick={handleAddressSearch}
                            className={styles.checkButton}
                            disabled={!isApiLoaded}
                            style={{ height: '50px' }}
                        >
                            검색
                        </button>
                    </div>
                </div>

                <div style={{ padding: '0 0 10px 0' }}>
                    <input
                        type="text"
                        name="detailAddress"
                        placeholder="나머지 상세 주소 입력 (선택 사항)"
                        value={addressParts.detail}
                        onChange={handleDetailAddressChange}
                        className={`${styles.input} ${styles.detailAddressInput}`}
                        disabled={!isApiLoaded || addressParts.base.length === 0}
                        id="detailAddressUpdateInput"
                        style={{ paddingLeft: '15px' }}
                    />
                </div>
            </div>

            {errorMessage && <p className={styles.error}>{errorMessage}</p>}


            <button type="submit" className={styles.submitButton}>수정 완료</button>
            <div className={styles.loginLink}>
                <button
                    type="button"
                    className={styles.link}
                    onClick={onCancel}
                    style={{ background: 'none', border: 'none', padding: 0 }}
                >
                    취소하기
                </button>
            </div>
        </form>
    );
};