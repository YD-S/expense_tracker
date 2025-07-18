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

    const isAuthenticated = !!user;

    const getTokens = () => {
        const accessToken = localStorage.getItem('accessToken');
        const refreshToken = localStorage.getItem('refreshToken');
        return { accessToken, refreshToken };
    };

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

    const login = async (email: string, password: string) => {
        try {
            const response = await axios.post('/api/auth/login', {
                email,
                password
            });

            const { accessToken, refreshToken, user } = response.data;
            setTokens(accessToken, refreshToken);
            setUser(user);
        } catch (error) {
            console.error('Login error:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('Login failed');
        }
    };

    const register = async (email : string, password : string) => {
        try {
            const response = await axios.post('/api/auth/register', {
                email,
                password
            });

            const { accessToken, refreshToken, user } = response.data;
            setTokens(accessToken, refreshToken);
            setUser(user);
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
        const { accessToken } = getTokens();

        if (accessToken) {
            verifyUser();
        } else {
            setIsLoading(false);
        }
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
