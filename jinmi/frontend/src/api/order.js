import apiClient from "./apiClient.js";


export const fetchOrderList = async (userId, offset = null) => {
    if (!userId) {
        throw new Error("주문 목록을 가져오려면 사용자 ID가 필요합니다.");
    }

    let url = `/order/list?userId=${userId}`;
    if (offset !== null) {
        url += `&offset=${offset}`;
    }

    try {
        const response = await apiClient.get(url);
        return response.data.data;
    } catch (error) {
        console.error("Error fetching order list:", error);
        throw error;
    }
};


export const addOrder = async (orderRequests) => {
    try {
        const response = await apiClient.post("/order/add", orderRequests);
        return response.data.data;
    } catch (error) {
        console.error("Error adding order:", error);
        throw error;
    }
};