import { apiClient } from './client';
import type { LatexNoteJson } from '../types/models';

export const latexApi = {
    createNote: async (note: LatexNoteJson): Promise<unknown> => {
        const response = await apiClient.post('/latex', note);
        return response.data;
    },
};
