import axios from 'axios';
import { getToken, clearAuth } from '../stores/auth';
export const http = axios.create({
    baseURL: '/api',
    timeout: 12000
});
http.interceptors.request.use((config) => {
    const token = getToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
http.interceptors.response.use((response) => response, (error) => {
    if (error.response?.data?.code === 401) {
        clearAuth();
    }
    return Promise.reject(error);
});
export function getApiErrorMessage(error) {
    if (axios.isAxiosError(error)) {
        return error.response?.data?.message || error.message || '请求失败';
    }
    return '请求失败';
}
