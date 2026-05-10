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
