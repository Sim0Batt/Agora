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

    downloadPdf: async (noteId: number): Promise<Blob> => {
        const response = await apiClient.post(`/latex/download/${noteId}`, undefined, {
            responseType: 'arraybuffer',
        });

        const contentType = response.headers?.['content-type'] || 'application/pdf';
        return new Blob([response.data], { type: contentType });
    },
};
