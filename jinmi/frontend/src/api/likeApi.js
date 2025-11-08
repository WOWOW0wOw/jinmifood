import apiClient from './apiClient';

/**
 * 아이템에 좋아요를 추가합니다.
 * @param {number} itemId - 좋아요를 추가할 아이템의 ID
 * @returns {Promise<any>}
 */
export const addLike = async (itemId) => {
    try {
        const response = await apiClient.post('/likes/add', { itemId });
        return response.data;
    } catch (error) {
        console.error('좋아요 추가 실패:', error.response?.data || error.message);
        throw error;
    }
};

/**
 * 아이템의 좋아요를 취소합니다.
 * @param {number} itemId - 좋아요를 취소할 아이템의 ID
 * @returns {Promise<any>}
 */
export const removeLike = async (itemId) => {
    try {
        const response = await apiClient.post('/likes/remove', { itemId });
        return response.data;
    } catch (error) {
        console.error('좋아요 취소 실패:', error.response?.data || error.message);
        throw error;
    }
};