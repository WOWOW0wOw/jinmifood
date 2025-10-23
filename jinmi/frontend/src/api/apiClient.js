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
const shouldRedirectToMyPage = () => {
    const pathname = window.location.pathname;
    return pathname.startsWith('/mypage') || pathname.startsWith('/users');
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

        const isAlreadyLoggedOut = !localStorage.getItem('accessToken') && !localStorage.getItem('refreshToken');


        if (isAlreadyLoggedOut && (error.response?.status === 401 || error.response?.status === 403 || error.response?.status === 404)) {
            console.warn("Clean-up 상태 감지: 인터셉터의 자동 리다이렉트를 무시하고 상위 컴포넌트의 navigate('/')를 따름");
            return Promise.reject(error);
        }
        if (error.response && error.response.status === 401 && !originalRequest._retry) {

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
                        window.location.href = '/login';
                        return Promise.reject(error);
                    }

                    try {
                        console.log("재발급 시도 Refresh Token:", refreshToken);
                        const rs = await apiClient.post('/auth/reissue', {
                            refreshToken: refreshToken
                        });

                        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = rs.data;
                        localStorage.setItem('accessToken', newAccessToken);
                        localStorage.setItem('refreshToken', newRefreshToken);

                        console.log("Access Token 재발급 성공 및 토큰 갱신 완료");

                        originalRequest.headers.Authorization = 'Bearer ' + newAccessToken;
                        isRefreshing = false;
                        processQueue(null, newAccessToken);
                        return apiClient(originalRequest); // 재시도!
                    } catch (err) {
                        isRefreshing = false;
                        processQueue(err, null);

                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('refreshToken');
                        let redirectPath = '/login';

                        if (shouldRedirectToMyPage()) {
                            redirectPath = '/mypage';
                        }

                        let errorMessage = "토큰 재발급 실패. 보안상의 이유로 로그아웃됩니다.";
                        if (err.response?.status === 401) {
                            errorMessage = "보안 위험(비정상적 토큰) 감지로 인해 자동 로그아웃됩니다.";
                        } else if (err.response?.status === 404) {
                            errorMessage = "세션이 만료되었습니다. 다시 로그인해주세요.";
                            if (shouldRedirectToMyPage()) {
                                redirectPath = '/mypage';
                            }
                        }
                        console.error(errorMessage, err.response?.data);
                        window.location.href = redirectPath;
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