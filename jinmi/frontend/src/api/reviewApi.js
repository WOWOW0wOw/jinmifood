import apiClient from './apiClient';

/** 리뷰 작성 */
export const addReview = async (itemId, content, image = null) => {
    const payload = { itemId, content, image };
    const res = await apiClient.post('/reviews/add', payload);
    return res.data;
};

/** 아이템별 리뷰 목록 */
export const getReviewsByItem = async (itemId) => {
    const res = await apiClient.get(`/reviews/listByItem?itemId=${itemId}`);
    return res.data.data;
};

export const updateReview = async (reviewId, content, image = null) => {
    const payload = { content, image };
    const res = await apiClient.post('/reviews/update', payload, {
        params: { reviewId }  // 쿼리 파라미터
    });
    return res.data;
};

export const deleteReview = async (reviewId) => {
    const res = await apiClient.post('/reviews/remove', null, {
        params: { reviewId }  // 쿼리 파라미터
    });
    return res.data;
};