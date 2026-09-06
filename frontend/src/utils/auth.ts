import axios from 'axios';

export type AuthUser = {
  userId: number;
  npk: string;
  fullName: string;
  roleCode: string;
  roleName: string;
  branchName?: string | null;
  token: string;
};

export const AUTH_STORAGE_KEY = 'spj_auth_token';
export const USER_STORAGE_KEY = 'spj_auth_user';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = getAuthToken();

  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      clearAuthState();

      if (window.location.pathname !== '/login') {
        window.location.replace('/login');
      }
    }

    return Promise.reject(error);
  },
);

export const getAuthToken = (): string | null => {
  return localStorage.getItem(AUTH_STORAGE_KEY);
};

export const getStoredUser = (): AuthUser | null => {
  const rawUser = localStorage.getItem(USER_STORAGE_KEY);

  if (!rawUser) {
    return null;
  }

  try {
    return JSON.parse(rawUser) as AuthUser;
  } catch {
    clearAuthState();
    return null;
  }
};

export const isAuthenticated = (): boolean => Boolean(getAuthToken());

export const setAuthState = (user: AuthUser) => {
  localStorage.setItem(AUTH_STORAGE_KEY, user.token);
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
};

export const clearAuthState = () => {
  localStorage.removeItem(AUTH_STORAGE_KEY);
  localStorage.removeItem(USER_STORAGE_KEY);
};
