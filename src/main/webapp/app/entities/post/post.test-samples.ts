import dayjs from 'dayjs/esm';

import { IPost, NewPost } from './post.model';

export const sampleWithRequiredData: IPost = {
  id: 8730,
  title: 'lawful above unless',
  createTime: dayjs('2025-04-10T10:53'),
  updateTime: dayjs('2025-04-10T12:12'),
};

export const sampleWithPartialData: IPost = {
  id: 26014,
  title: 'boohoo',
  createTime: dayjs('2025-04-11T07:34'),
  updateTime: dayjs('2025-04-11T03:44'),
};

export const sampleWithFullData: IPost = {
  id: 9917,
  title: 'clearly darn icy',
  content: 'wide recount',
  createTime: dayjs('2025-04-10T13:48'),
  updateTime: dayjs('2025-04-10T19:37'),
};

export const sampleWithNewData: NewPost = {
  title: 'willfully settler',
  createTime: dayjs('2025-04-10T10:35'),
  updateTime: dayjs('2025-04-10T13:53'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
