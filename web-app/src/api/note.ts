import { apiClient } from './client';
import type { NoteJson } from '../types/models';

export const noteApi = {
    createNote: async (note: NoteJson): Promise<unknown> => {
        const response = await apiClient.post('/note', note);
        return response.data;
    },
    getNote: async (id: number): Promise<unknown> => {
        const response = await apiClient.get(`/note/${id}`);
        return response.data;
    },
    updateNote: async (noteId: number, content: string): Promise<unknown> => {
        const response = await apiClient.post('/note/update', { noteId, content });
        return response.data;
    },
    listNotes: async (userId: number): Promise<unknown> => {
        const response = await apiClient.get(`/note/list/${userId}`);
        return response.data;
    },
};
