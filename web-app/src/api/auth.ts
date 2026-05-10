import { apiClient } from './client';
import type { LoginJson, RegisterJson } from '../types/models';
//TODO: cambiare unknown con cosa restituisce il server
export const authApi = {
  login: async (credentials: LoginJson): Promise<unknown> => {
    const response = await apiClient.post('/login', credentials);
    return response.data;
  },

  register: async (userData: RegisterJson): Promise<unknown> => {
    const response = await apiClient.post('/register', userData);
    return response.data;
  },
};
