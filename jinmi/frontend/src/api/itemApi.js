import apiClient from './apiClient';

/**
 * 모든 상품 목록을 가져옵니다.
 * @returns {Promise<Array>} 상품 객체 배열
 */
export const fetchAllItems = async () => {
    try {
        const response = await apiClient.get('/items/list');
        return response.data.data;
    } catch (error) {
        console.error('Error fetching all items:', error);
        throw error;
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

/**
 * 모든 카테고리 목록을 가져옵니다.
 */
export const fetchAllCategories = async () => {
    try {
        const response = await apiClient.get('/categories/list');
        return response.data.data;
    } catch (error) {
        console.error('Error fetching all categories:', error);
        throw error;
    }
};

/**
 * 아이템 상세정보를 가져옵니다.
 * @param {string | number} itemId - 조회할 아이템의 ID
 */
export const fetchItem = async (itemId) => {
    try {
        const response = await apiClient.get(`/items/itemDetail/${itemId}`);
        return response.data.data;
    } catch (error) {
        // 에러 로그는 그대로 두어 나중에 다른 문제가 생겼을 때 확인하기 좋습니다.
        console.error(`Error fetching item with ID ${itemId}:`, error);
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