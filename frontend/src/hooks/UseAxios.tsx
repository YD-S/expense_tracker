import {useMemo} from 'react';
import axios, {type AxiosInstance} from 'axios';

const UseAxios = (): AxiosInstance => {
    return useMemo(() => {
        const getBaseURL = () => {
            if (import.meta.env.DEV) {
                return 'http://localhost:8080';
            }
            return 'http://backend:8080';
        };

        const instance = axios.create({
            baseURL: getBaseURL(),
            timeout: 10000,
        });

        instance.interceptors.request.use(
            (config) => {
                const accessToken = localStorage.getItem('accessToken');
                if (accessToken) {
                    config.headers.Authorization = `Bearer ${accessToken}`;
                }
                return config;
            },
            (error) => Promise.reject(error)
        );

        instance.interceptors.response.use(
            (response) => response,
            async (error) => {
                const originalRequest = error.config;

                if (error.response?.status === 401 && !originalRequest._retry) {
                    originalRequest._retry = true;

                    const newAccessToken = await refreshAccessToken();

                    if (newAccessToken) {
                        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                        return instance(originalRequest);
                    } else {
                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('refreshToken');
                        window.location.href = '/login';
                        return Promise.reject(error);
                    }
                }

                return Promise.reject(error);
            }
        );

        return instance;
    }, []);
};

const refreshAccessToken = async (): Promise<string | null> => {
    try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) return null;

        const getBaseURL = () => {
            if (import.meta.env.DEV) {
                return 'http://localhost:8080';
            }
            return 'http://backend:8080';
        };

        const response = await axios.post(`${getBaseURL()}/api/auth/refresh`, {
            refreshToken
        });

        if (response.data) {
            localStorage.setItem('accessToken', response.data.accessToken);
            localStorage.setItem('refreshToken', response.data.refreshToken);
            return response.data.accessToken;
        }
        return null;
    } catch (error) {
        console.error('Error refreshing token:', error);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        return null;
    }
};

export default UseAxios;