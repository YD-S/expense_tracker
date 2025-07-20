import React, {type ReactNode, useEffect, useState} from "react";
import {AuthContext, type AuthContextType, type User} from "../Context/AuthContext.tsx";
import UseAxios from "../hooks/UseAxios";
import axios from "axios";

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const api = UseAxios();

    const getTokens = () => {
        const accessToken = localStorage.getItem('accessToken');
        const refreshToken = localStorage.getItem('refreshToken');
        return { accessToken, refreshToken };
    };

    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => {
        const { accessToken, refreshToken } = getTokens();
        return !!accessToken && !!refreshToken;
    });

    const setTokens = (accessToken: string, refreshToken: string) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
    };

    const clearTokens = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
    };


    const verifyUser = async () => {
        try {
            const response = await api.get('/api/auth/me');
            setUser(response.data);
        } catch (error) {
            console.error('Error verifying user:', error);
            clearTokens();
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    };

    const login = async (username: string, password: string) => {
        try {
            console.log('Attempting to login with:', username, password);
            const response = await axios.post('http://localhost:8080/api/auth/login', {
                username,
                password
            });

            const { accessToken, refreshToken } = response.data;
            setTokens(accessToken, refreshToken);
            setIsAuthenticated(true);
        } catch (error) {
            console.error('Login error:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('Login failed');
        }
    };

    const register = async (username : string,  email : string, password : string) => {
        try {
            const response = await axios.post('http://localhost:8080/api/auth/register', {
                username,
                email,
                password
            });

            const responseCode = response.status;
            if (responseCode === 201) {
                console.log('Registration successful');
                return true;
            }else {
                console.error('Registration failed with status:', responseCode);
                throw new Error('Registration failed');
            }

        } catch (error) {
            console.error('Registration error:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('Registration failed');
        }
    }

    const logout = () => {
        clearTokens();
        setUser(null);
    };

    useEffect(() => {
        const { accessToken, refreshToken } = getTokens();
        setIsAuthenticated(!!accessToken && !!refreshToken);
        setIsLoading(false);
    }, []);

    const value: AuthContextType = {
        user,
        login,
        logout,
        register,
        isLoading,
        isAuthenticated,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
