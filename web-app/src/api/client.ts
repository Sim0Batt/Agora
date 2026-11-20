import axios, { AxiosError } from 'axios';
import { API_BASE_URL } from './config';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 60000,
});

// Il backend segnala un errore rispondendo 200 con corpo "FA".
export const BACKEND_FAILURE = 'FA';

const isBackendFailure = (data: unknown) => {
  if (typeof data === 'string') return data.trim() === BACKEND_FAILURE;
  if (data instanceof ArrayBuffer) {
    const bytes = new Uint8Array(data);
    return bytes.length === 2 && bytes[0] === 0x46 && bytes[1] === 0x41;
  }
  return false;
};

apiClient.interceptors.response.use((response) => {
  if (isBackendFailure(response.data)) {
    throw new AxiosError(
      'Il server ha segnalato un errore',
      'ERR_BACKEND_FAILURE',
      response.config,
      response.request,
      response,
    );
  }
  return response;
});
