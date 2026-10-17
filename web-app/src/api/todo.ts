import { apiClient } from './client';
import type { Todo } from '../types/models';

export const todoApi = {
    listTodos: async (): Promise<Todo[]> => {
        const response = await apiClient.get<Todo[]>('/todo');
        return response.data;
    },
};
