import React, { createContext, useContext, useState, useEffect } from 'react';
import { UserResponse } from '../types/api';
import { api } from '../services/api';

interface AuthContextType {
  user: UserResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserResponse | null>(() => {
    try {
      const stored = localStorage.getItem('skillswap_user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Validate session on mount
  useEffect(() => {
    const verifySession = async () => {
      try {
        const currentUser = await api.getCurrentUser();
        setUser(currentUser);
        localStorage.setItem('skillswap_user', JSON.stringify(currentUser));
      } catch {
        // If 401 Unauthorized or network error, session is invalid
        setUser(null);
        localStorage.removeItem('skillswap_user');
        localStorage.removeItem('skillswap_auth');
      } finally {
        setIsLoading(false);
      }
    };

    verifySession();
  }, []);

  const login = async (email: string, password: string) => {
    const trimmedEmail = email.trim().toLowerCase();
    const response = await api.login({ email: trimmedEmail, password });
    
    // Store only non-sensitive user metadata in localStorage (no passwords or tokens)
    localStorage.setItem('skillswap_user', JSON.stringify(response.user));
    setUser(response.user);
  };

  const register = async (name: string, email: string, password: string) => {
    const trimmedName = name.trim();
    const trimmedEmail = email.trim().toLowerCase();
    const userResponse = await api.register({ name: trimmedName, email: trimmedEmail, password });
    if (userResponse) {
      localStorage.setItem('skillswap_user', JSON.stringify(userResponse));
      setUser(userResponse);
    }
  };

  const logout = async () => {
    try {
      await api.logout();
    } catch (err) {
      console.warn('Logout server request failed:', err);
    } finally {
      localStorage.removeItem('skillswap_user');
      localStorage.removeItem('skillswap_auth');
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
