import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../post-status.test-samples';

import { PostStatusFormService } from './post-status-form.service';

describe('PostStatus Form Service', () => {
  let service: PostStatusFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PostStatusFormService);
  });

  describe('Service methods', () => {
    describe('createPostStatusFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createPostStatusFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            status: expect.any(Object),
          }),
        );
      });

      it('passing IPostStatus should create a new form with FormGroup', () => {
        const formGroup = service.createPostStatusFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            status: expect.any(Object),
          }),
        );
      });
    });

    describe('getPostStatus', () => {
      it('should return NewPostStatus for default PostStatus initial value', () => {
        const formGroup = service.createPostStatusFormGroup(sampleWithNewData);

        const postStatus = service.getPostStatus(formGroup) as any;

        expect(postStatus).toMatchObject(sampleWithNewData);
      });

      it('should return NewPostStatus for empty PostStatus initial value', () => {
        const formGroup = service.createPostStatusFormGroup();

        const postStatus = service.getPostStatus(formGroup) as any;

        expect(postStatus).toMatchObject({});
      });

      it('should return IPostStatus', () => {
        const formGroup = service.createPostStatusFormGroup(sampleWithRequiredData);

        const postStatus = service.getPostStatus(formGroup) as any;

        expect(postStatus).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IPostStatus should not enable id FormControl', () => {
        const formGroup = service.createPostStatusFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewPostStatus should disable id FormControl', () => {
        const formGroup = service.createPostStatusFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
