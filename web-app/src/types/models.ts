export interface LoginJson {
  email: string;
  password: string;
}

export interface RegisterJson {
  name: string;
  surname: string;
  email: string;
  password: string;
  role: string;
}

export interface FishingLicenceJson {
  licenseText: string;
  userId: number;
}

export interface User {
  name: string;
  surname: string;
  fiscalCode: string;
  verified: boolean;
}

export interface LatexNoteJson {
  title: string;
  content: string;
  type: string;
  userId: number;
}

export interface NoteJson {
  title: string;
  content: string;
  type: string;
  userId: number;
}

export interface NoteRecord {
  id: number;
  title: string;
  length: number;
  type: string;
  userId: number;
  content: string;
}

export interface Licence {
  licenseNumber: string;
  releasedBy: string;
  typeOfPermit: string;
  season: string;
  noKill: boolean;
  type: string;
  qrCodeToken: string;
  userId: number;
}

export type TodoType = 'Work' | 'University' | 'Other';

export interface Todo {
  id?: number;
  type: TodoType;
  title: string;
  description: string;
  expirationDate: string;
}
