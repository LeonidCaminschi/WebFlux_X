import dayjs from 'dayjs/esm';

import { IComment, NewComment } from './comment.model';

export const sampleWithRequiredData: IComment = {
  id: 20452,
  content: 'and better',
  createTime: dayjs('2025-04-10T18:30'),
};

export const sampleWithPartialData: IComment = {
  id: 12398,
  content: 'immediately',
  createTime: dayjs('2025-04-10T21:43'),
};

export const sampleWithFullData: IComment = {
  id: 28427,
  content: 'testing ugh',
  createTime: dayjs('2025-04-10T22:40'),
};

export const sampleWithNewData: NewComment = {
  content: 'metabolise pish jubilant',
  createTime: dayjs('2025-04-10T21:23'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
