import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from "../../context/AuthContext.jsx";
import apiClient from "../../api/apiClient.js";
import styles from "./css/DeleteAccount.module.css";


export default function DeleteAccount() {
    const navigate = useNavigate();
    const { user, handleLocalLogout } = useAuth();
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);


    if (!user) {
        return null;
    }

    const handleDeleteAccount = async (e) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        const email = user.email;

        try {
            await apiClient.post("/users/checkPassword",{
                email: email,
                password: password
            });

            await apiClient.delete("/users/delete");
            alert("성공적으로 회원 탈퇴되었습니다. 메인 페이지로 이동합니다.");
            setTimeout(() => {
                handleLocalLogout();
            }, 0);
        } catch (err) {
            console.error("회원탈퇴 처리 실패:",err);
            if (err.response?.data?.message === "비밀번호가 일치하지 않습니다.") {
                setError("비밀번호가 일치하지 않습니다. 다시 확인해 주세요.");
            } else if (err.response?.status === 404) {
                setError("탈퇴 요청 경로를 찾을 수 없습니다.");
            } else if (err.response?.data?.message) {
                setError("탈퇴 실패: " + err.response.data.message);
            } else {
                setError("회원 탈퇴 처리 중 알 수 없는 오류가 발생했습니다.");
            }
        } finally {
            setIsSubmitting(false);
        }
    };
    const handleCancel = () => {
        navigate('/mypage');
    };

    return (
        <div className={styles['delete-account-container']}>
            <h2>회원 탈퇴</h2>
            <div className={styles['warning-box']}>
                <p className={styles.title}>⚠️ 정말로 회원 탈퇴를 하시겠습니까?</p>
                <p className={styles.content}>
                    회원 탈퇴 시, **보유하신 모든 포인트, 쿠폰 및 활동 내역은 영구적으로 삭제되며 복구가 불가능**합니다.
                </p>
                <p className={styles.final_check}>
                    탈퇴를 진행하시려면 **현재 사용 중인 비밀번호**를 입력하고 '탈퇴 확인' 버튼을 눌러주세요.
                </p>
            </div>

            <form onSubmit={handleDeleteAccount} className={styles['password-form']}>
                <div className={styles['form-group']}>
                    <label htmlFor="current-password">비밀번호 확인</label>
                    <input
                        type="password"
                        id="current-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        disabled={isSubmitting}
                        required
                        autoFocus
                    />
                </div>

                {error && <p className={styles['error-text']}>{error}</p>}

                <div className={styles['button-group']}>
                    <button
                        type="submit"
                        className={styles['danger-btn']}
                        disabled={isSubmitting || !password}
                    >
                        {isSubmitting ? "탈퇴 처리 중..." : "탈퇴 확인"}
                    </button>
                    <button
                        type="button"
                        className={styles['secondary-btn']}
                        onClick={handleCancel}
                        disabled={isSubmitting}
                    >
                        취소
                    </button>
                </div>
            </form>
        </div>
    );

}