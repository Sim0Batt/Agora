export interface LatexNoteJson {
  title: string;
  content: string;
  type: string;
}

export interface NoteJson {
  title: string;
  type: string;
  length: number;
}

export interface NoteRecord {
  id: number;
  title: string;
  length: number;
  type: string;
  content: string;
}

export type TodoType = 'Work' | 'University' | 'Other';

export interface Todo {
  id?: number;
  type: TodoType;
  title: string;
  description: string;
  expirationDate: string;
}

export interface SearchSong {
  id: string;
  title: string;
  author: string;
  duration: number;
  url: string;
}

export interface Song {
  id: string;
  title: string;
  author: string;
  duration: number;
}

export interface Playlist {
  id: string;
  title: string;
  count: number;
}
