import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 재발급 요청 중복 방지위한 플래그
let isRefreshing = false;

// 토큰 재발급 후 원래 요청들을 다시 시도하기 위해
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if(error){
            prom.reject(error);
        }else{
            //새로운 토큰으로 헤더 업데이트후 원래 요청 재시도
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

// 모든 요청 보내지기전에 실행
apiClient.interceptors.request.use(
    (config) => {
        const accessToken = localStorage.getItem('accessToken');

        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 토큰 만료 처리
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        // 401 에러고, 이미 재시도한 요청이 아니어야함
        if(error.response && error.response.status === 401 && !originalRequest._retry){
            // 재시도 플래그를 true로 변경
            originalRequest._retry = true;

            // 재발급 플래그 확인 (다른 요청이 재발급 시도중인지)
            if(isRefreshing){
                //재발급 대기열에 넣고 , 재발급 끝날때까지 기다림
                return new Promise(function (resolve,reject){
                    failedQueue.push({resolve,reject});
                })
                    .then(token => {
                        //새로운 토큰으로 원래 요청을 재구성해서 반환
                        originalRequest.headers.Authorization = 'Bearer ' + token;
                        return apiClient(originalRequest);
                    })
                    .catch(err => {
                        return Promise.reject(err);
                    });
            }
            isRefreshing = true;
            const refreshToken = localStorage.getItem('refreshToken');

            if(!refreshToken){
                isRefreshing = false;
                return Promise.reject(error);
            }
            try{
                const rs = await axios.post(`${API_BASE_URL}/auth/reissue`, {
                    refreshToken: refreshToken
                });

                const { accessToken: newAccessToken, refreshToken: newRefreshToken } = rs.data;
                localStorage.setItem('accessToken', newAccessToken);
                localStorage.setItem('refreshToken', newRefreshToken);

                console.log("Access Token 재발급 성공 및 토큰 갱신 완료");

                //  대기열에 있던 요청들과 현재 요청 재시도
                isRefreshing = false;
                processQueue(null, newAccessToken); // 대기열 요청 처리

                originalRequest.headers.Authorization = 'Bearer ' + newAccessToken;
                return apiClient(originalRequest);
            } catch (err){
                isRefreshing = false;
                processQueue(err,null);

                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');

                console.error(" Access Token 재발급 실패. 자동 로그아웃을 진행합니다.");

                return Promise.reject(err);


            }
        }


        return Promise.reject(error);
    }
);

export default apiClient;