import React, { createContext, useContext, useState, useEffect } from 'react';
import { authStorage, loginUser, registerUser } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Restore session on mount
    const savedToken = authStorage.getToken();
    const savedUser = authStorage.getUser();
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(savedUser);
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const res = await loginUser(email, password);
    if (res?.data) {
      const { token: jwt, ...profile } = res.data;
      setToken(jwt);
      setUser(profile);
      return res.data;
    }
  };

  const register = async ({ name, email, password, role }) => {
    const res = await registerUser({ name, email, password, role });
    if (res?.data) {
      const { token: jwt, ...profile } = res.data;
      setToken(jwt);
      setUser(profile);
      return res.data;
    }
  };

  const logout = () => {
    authStorage.clear();
    setToken(null);
    setUser(null);
  };

  const value = {
    user,
    token,
    loading,
    isAuthenticated: !!token,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
