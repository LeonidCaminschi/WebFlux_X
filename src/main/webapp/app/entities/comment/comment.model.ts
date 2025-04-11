import dayjs from 'dayjs/esm';
import { IPost } from 'app/entities/post/post.model';

export interface IComment {
  id: number;
  content?: string | null;
  createTime?: dayjs.Dayjs | null;
  post?: IPost | null;
}

export type NewComment = Omit<IComment, 'id'> & { id: null };
