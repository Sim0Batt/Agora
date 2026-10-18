import { apiClient } from './client';
import type { Todo } from '../types/models';

export const todoApi = {
    listTodos: async (): Promise<Todo[]> => {
        const response = await apiClient.get<Todo[]>('/todo');
        return response.data;
    },
    addTodo: async (todo: Omit<Todo, 'id'>): Promise<void> => {
        await apiClient.post('/todo', todo);
    },
    removeTodo: async (id: number): Promise<void> => {
        await apiClient.delete(`/todo/${id}`);
    },
};
