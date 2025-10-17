import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

let isRefreshing = false;
let failedQueue = [];


const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

apiClient.interceptors.request.use(
    (config) => {
        const accessToken = localStorage.getItem('accessToken');
        const isAuthRequest = config.url.includes('/users/login') ||
            config.url.includes('/users/join');

        if (accessToken && !isAuthRequest && !config.url.includes('/auth/reissue')) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // 401 오류가 발생했고, 재시도 중이 아닌 경우에만 처리 시작
        if (error.response && error.response.status === 401 && !originalRequest._retry) {

            // 🚨 1. 특정 URL (회원 정보 수정)에 대한 예외 처리 🚨
            const IS_FORCE_LOGOUT_URL = originalRequest.url.includes('/users/myUpdateInfo');

            if (IS_FORCE_LOGOUT_URL) {
                console.error("회원정보 수정 중 Access Token 만료. 만료 시간이 지나 로그아웃됩니다.");
                alert("만료 시간이 지나 로그아웃됩니다.");

                // 토큰 삭제 및 강제 로그아웃 처리
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');

                return Promise.reject(error);
            }

            if (!originalRequest.url.includes('/auth/reissue')) {

                originalRequest._retry = true; // 재시도 플래그 설정

                const retryPromise = new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                });

                if (!isRefreshing) {
                    isRefreshing = true;
                    const refreshToken = localStorage.getItem('refreshToken');

                    if (!refreshToken) {
                        isRefreshing = false;
                        processQueue('NO_REFRESH_TOKEN', null);
                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('refreshToken');
                        console.error("Refresh Token이 없어 Access Token 재발급 실패. 자동 로그아웃을 진행합니다.");
                        return Promise.reject(error);
                    }

                    try {
                        const rs = await axios.post(`${API_BASE_URL}/auth/reissue`, {
                            refreshToken: refreshToken
                        });

                        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = rs.data;
                        localStorage.setItem('accessToken', newAccessToken);
                        localStorage.setItem('refreshToken', newRefreshToken);

                        console.log("Access Token 재발급 성공 및 토큰 갱신 완료");

                        isRefreshing = false;
                        processQueue(null, newAccessToken);

                        originalRequest.headers.Authorization = 'Bearer ' + newAccessToken;
                        return apiClient(originalRequest);
                    } catch (err) {
                        isRefreshing = false;
                        processQueue(err, null);

                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('refreshToken');
                        console.error(" Access Token 재발급 실패. 자동 로그아웃을 진행합니다.");
                        if (err.response?.status !== 404) {
                            console.error("재발급 실패 상세:", err.response?.data);
                        }
                        return Promise.reject(err);
                    }
                } else {
                    return retryPromise.then(token => {
                        originalRequest.headers.Authorization = 'Bearer ' + token;
                        return apiClient(originalRequest);
                    });
                }
            }
        }

        return Promise.reject(error);
    }
);

export default apiClient;