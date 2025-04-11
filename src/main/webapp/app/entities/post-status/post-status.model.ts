export interface IPostStatus {
  id: number;
  status?: string | null;
}

export type NewPostStatus = Omit<IPostStatus, 'id'> & { id: null };
