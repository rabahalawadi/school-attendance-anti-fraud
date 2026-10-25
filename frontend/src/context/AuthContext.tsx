import React, { createContext, useContext, useState, useEffect } from 'react';
import { UserProfile, Role } from '../types';
import { apiRequest } from '../lib/api';

interface AuthContextType {
  user: UserProfile | null;
  token: string | null;
  role: Role | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const fetchProfile = async () => {
    try {
      const profile = await apiRequest<UserProfile>('/auth/me');
      setUser(profile);
      localStorage.setItem('user', JSON.stringify(profile));
    } catch (err) {
      console.warn('Failed to fetch profile', err);
      logout();
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (token) {
      fetchProfile();
    } else {
      setIsLoading(false);
    }
  }, [token]);

  const login = async (username: string, password: string) => {
    const response: any = await apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });

    localStorage.setItem('token', response.accessToken);
    localStorage.setItem('refreshToken', response.refreshToken);
    setToken(response.accessToken);

    const profile: UserProfile = {
      id: response.userId,
      username: response.username,
      role: response.role,
      status: 'ACTIVE',
      profileId: response.profileId,
      identifier: response.username,
      fullName: response.fullName,
    };

    setUser(profile);
    localStorage.setItem('user', JSON.stringify(profile));
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        role: user?.role || null,
        isLoading,
        login,
        logout,
        refreshProfile: fetchProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
