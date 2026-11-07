import { apiClient } from './client';
import type { PickedTrack, SearchResult, Song } from '../types/models';

export const musicApi = {
    search: async (title: string): Promise<SearchResult> => {
        const response = await apiClient.post<SearchResult>('/music/search', { title });
        return response.data;
    },
    download: async (messageId: number, index: number): Promise<PickedTrack> => {
        const response = await apiClient.get<PickedTrack>(`/music/${messageId}/${index}/download`);
        return response.data;
    },
    listSongs: async (): Promise<Song[]> => {
        const response = await apiClient.get<Song[]>('/songs');
        if (!Array.isArray(response.data)) {
            throw new Error(String(response.data));
        }
        return response.data;
    },
};
