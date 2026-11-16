import { apiClient } from './client';
import type { Playlist, SearchSong, Song } from '../types/models';

export const musicApi = {
    search: async (title: string): Promise<SearchSong[]> => {
        const response = await apiClient.post<SearchSong[]>('/music/search', { title });
        if (!Array.isArray(response.data)) {
            throw new Error(String(response.data));
        }
        return response.data;
    },
    download: async (song: SearchSong): Promise<void> => {
        await apiClient.post('/music/download', song, { timeout: 600_000 });
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
    importPlaylist: async (title: string, url: string): Promise<void> => {
        await apiClient.post('/music/import', { title, url }, { timeout: 0 });
    },
    deleteSong: async (songId: string): Promise<void> => {
        await apiClient.delete(`/song/${songId}`);
    },
    removeSongFromPlaylist: async (playlistId: string, songId: string): Promise<void> => {
        await apiClient.delete(`/playlist/${playlistId}/${songId}`);
    },
};
