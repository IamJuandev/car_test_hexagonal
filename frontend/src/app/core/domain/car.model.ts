export interface Car {
  id: number;
  brand: string;
  model: string;
  year: number;
  plateNumber: string;
  color: string;
  photoUrl: string | null;
}

export type CarInput = Omit<Car, 'id'>;
