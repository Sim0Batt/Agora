import { apiClient } from './client';
import type { PickedTrack, Playlist, SearchResult, Song } from '../types/models';

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
    listPlaylists: async (): Promise<Playlist[]> => {
        const response = await apiClient.get<Playlist[]>('/playlist');
        if (!Array.isArray(response.data)) {
            throw new Error(String(response.data));
        }
        return response.data;
    },
    createPlaylist: async (title: string): Promise<void> => {
        await apiClient.post('/playlist', { title });
    },
    addSongToPlaylist: async (playlistId: string, songId: string): Promise<void> => {
        await apiClient.post(`/playlist/add/${playlistId}/${songId}`);
    },
    listPlaylistSongs: async (playlistId: string): Promise<Song[]> => {
        const response = await apiClient.get<Song[]>(`/playlist/song/${playlistId}`);
        if (!Array.isArray(response.data)) {
            throw new Error(String(response.data));
        }
        return response.data;
    },
    deletePlaylist: async (playlistId: string): Promise<void> => {
        await apiClient.delete(`/playlist/${playlistId}`);
    },
    removeSongFromPlaylist: async (playlistId: string, songId: string): Promise<void> => {
        await apiClient.delete(`/playlist/${playlistId}/${songId}`);
    },
};
