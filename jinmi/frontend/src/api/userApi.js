
import apiClient from './apiClient.js';

export const fetchMyUserInfo = async () => {
    try{
        // 토큰이 자동으로 헤더에 담겨서 백엔드로 전송
        const response = await apiClient.get('/users/myInfo');
        return response.date;
    } catch (error){
        console.error("회원 정보 조회 실패:", error);
        throw error;
    }
};

export const updateMyInfo = async (updateDate) => {
    try{
        const response = await apiClient.put('users/myUpdateInfo',updateDate);
        return response.data;
    } catch (error){
        console.error("회원 정보 수정 실패:", error);
        throw error;
    }
}
