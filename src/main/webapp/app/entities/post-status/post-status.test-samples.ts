import { IPostStatus, NewPostStatus } from './post-status.model';

export const sampleWithRequiredData: IPostStatus = {
  id: 14004,
  status: 'normal',
};

export const sampleWithPartialData: IPostStatus = {
  id: 31591,
  status: 'tomb warp',
};

export const sampleWithFullData: IPostStatus = {
  id: 7017,
  status: 'responsible',
};

export const sampleWithNewData: NewPostStatus = {
  status: 'eyeglasses',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
