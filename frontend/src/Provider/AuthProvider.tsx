import React, {type ReactNode, useEffect, useState} from "react";
import {AuthContext, type AuthContextType, type User} from "../Context/AuthContext.tsx";
import axios from "axios";

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

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

    const login = async (username: string, password: string) => {
        try {
            const response = await axios.post('http://localhost:8080/api/auth/login', {
                username,
                password
            });

            const { accessToken, refreshToken } = response.data;
            setTokens(accessToken, refreshToken);
            setIsAuthenticated(true);
        } catch (error : unknown) {
            console.error('Login error:', error);
            if (axios.isAxiosError(error) && error.response?.data?.message) {
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

        } catch (error : unknown) {
            console.error('Registration error:', error);
            if (axios.isAxiosError(error) && error.response?.data?.message) {
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
