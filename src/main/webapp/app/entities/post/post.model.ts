import dayjs from 'dayjs/esm';
import { IPostStatus } from 'app/entities/post-status/post-status.model';

export interface IPost {
  id: number;
  title?: string | null;
  content?: string | null;
  createTime?: dayjs.Dayjs | null;
  updateTime?: dayjs.Dayjs | null;
  postStatus?: IPostStatus | null;
}

export type NewPost = Omit<IPost, 'id'> & { id: null };
