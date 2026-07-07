import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../utils/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Initialize Auth state from localStorage
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      const savedUser = localStorage.getItem('user');

      if (token && savedUser) {
        try {
          setUser(JSON.parse(savedUser));
          // Verify token and fetch latest profile details
          const response = await api.get('/api/students/me');
          if (response.data && response.data.success) {
            const updatedUser = {
              ...JSON.parse(savedUser),
              name: response.data.data.name,
              email: response.data.data.email,
            };
            setUser(updatedUser);
            localStorage.setItem('user', JSON.stringify(updatedUser));
          }
        } catch (error) {
          console.error("Token verification failed", error);
          logout();
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (email, password) => {
    try {
      const response = await api.post('/api/auth/login', { email, password });
      if (response.data && response.data.success) {
        const { token, name, email: userEmail, role } = response.data.data;
        
        localStorage.setItem('token', token);
        const userData = { name, email: userEmail, role };
        localStorage.setItem('user', JSON.stringify(userData));
        
        setUser(userData);
        return { success: true };
      }
      return { success: false, error: response.data.message || 'Login failed' };
    } catch (error) {
      const message = error.response?.data?.message || 'Invalid email or password';
      return { success: false, error: message };
    }
  };

  const signup = async (signupData) => {
    try {
      const response = await api.post('/api/auth/signup', signupData);
      if (response.data && response.data.success) {
        return { success: true };
      }
      return { success: false, error: response.data.message || 'Signup failed' };
    } catch (error) {
      const message = error.response?.data?.message || 'Registration failed. Please try again.';
      return { success: false, error: message };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
export default AuthContext;
