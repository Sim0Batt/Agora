import axios from 'axios';
import { URL } from '../assets/variables';

export const apiClient = axios.create({
  baseURL: URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// apiClient.interceptors.request.use((config) => {
//   return config;
// });
