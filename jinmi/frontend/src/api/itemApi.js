import apiClient from './apiClient';

/**
 * 모든 상품 목록을 가져옵니다.
 * @returns {Promise<Array>} 상품 객체 배열
 */
export const fetchAllItems = async () => {
    try {
        const response = await apiClient.get('/items/list');
        return response.data.data; // StatusResponseDTO의 'data' 필드
    } catch (error) {
        console.error('Error fetching all items:', error);
        throw error; // 에러를 호출자에게 다시 던져서 처리하게 함
    }
};

/**
 * 특정 카테고리 ID에 해당하는 상품 목록을 가져옵니다.
 * @param {number} categoryId - 카테고리 ID
 * @returns {Promise<Array>} 상품 객체 배열
 */
export const fetchItemsByCategoryId = async (categoryId) => {
    try {
        const response = await apiClient.get(`/items/listByCategory`, {
            params: { categoryId: categoryId }
        });
        return response.data.data; // StatusResponseDTO의 'data' 필드
    } catch (error) {
        console.error(`Error fetching items for category ${categoryId}:`, error);
        throw error;
    }
};

// 필요한 경우 다른 상품 관련 API 함수들을 여기에 추가할 수 있습니다.
// 예: 상품 추가, 수정, 삭제 등 (관리자용)
/*
export const addItem = async (itemData) => {
    try {
        const response = await apiClient.post('/items/add', itemData);
        return response.data.data;
    } catch (error) {
        console.error('Error adding item:', error);
        throw error;
    }
};
*/