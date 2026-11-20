import { apiClient } from './client';
import type { LatexNoteJson } from '../types/models';

export const latexApi = {
    createNote: async (note: LatexNoteJson): Promise<unknown> => {
        const response = await apiClient.post('/latex', note);
        return response.data;
    },

    uploadImages: async (title: string, images: File[]): Promise<unknown> => {
        const formData = new FormData();
        formData.append('title', title);
        images.forEach((image) => formData.append('images', image));

        const response = await apiClient.post('/latex/image', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });

        return response.data;
    },

    deleteNote: async (noteId: number): Promise<void> => {
        await apiClient.delete(`/latex/${noteId}`);
    },

    listImages: async (title: string): Promise<string[]> => {
        const response = await apiClient.get<string[]>(`/latex/image/${encodeURIComponent(title)}`);
        if (!Array.isArray(response.data)) {
            throw new Error(String(response.data));
        }
        return response.data;
    },

    downloadPdf: async (noteId: number): Promise<Blob> => {
        const decode = (data: ArrayBuffer) => new TextDecoder().decode(data).trim();

        let response;
        try {
            response = await apiClient.post<ArrayBuffer>(`/latex/download/${noteId}`, undefined, {
                responseType: 'arraybuffer',
            });
        } catch (err) {
            // Con responseType arraybuffer anche il corpo dell'errore arriva come buffer
            const data = (err as { response?: { data?: unknown } })?.response?.data;
            if (data instanceof ArrayBuffer && data.byteLength > 0 && (err as { code?: string }).code !== 'ERR_BACKEND_FAILURE') {
                throw new Error(decode(data));
            }
            throw err;
        }

        // Se la generazione fallisce il backend risponde con il testo dell'errore invece del PDF
        const head = new TextDecoder().decode(new Uint8Array(response.data, 0, Math.min(4, response.data.byteLength)));
        if (head !== '%PDF') {
            throw new Error(decode(response.data) || 'Il server non ha restituito un PDF.');
        }

        return new Blob([response.data], { type: 'application/pdf' });
    },
};
