import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IPostStatus, NewPostStatus } from '../post-status.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPostStatus for edit and NewPostStatusFormGroupInput for create.
 */
type PostStatusFormGroupInput = IPostStatus | PartialWithRequiredKeyOf<NewPostStatus>;

type PostStatusFormDefaults = Pick<NewPostStatus, 'id'>;

type PostStatusFormGroupContent = {
  id: FormControl<IPostStatus['id'] | NewPostStatus['id']>;
  status: FormControl<IPostStatus['status']>;
};

export type PostStatusFormGroup = FormGroup<PostStatusFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PostStatusFormService {
  createPostStatusFormGroup(postStatus: PostStatusFormGroupInput = { id: null }): PostStatusFormGroup {
    const postStatusRawValue = {
      ...this.getFormDefaults(),
      ...postStatus,
    };
    return new FormGroup<PostStatusFormGroupContent>({
      id: new FormControl(
        { value: postStatusRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      status: new FormControl(postStatusRawValue.status, {
        validators: [Validators.required],
      }),
    });
  }

  getPostStatus(form: PostStatusFormGroup): IPostStatus | NewPostStatus {
    return form.getRawValue() as IPostStatus | NewPostStatus;
  }

  resetForm(form: PostStatusFormGroup, postStatus: PostStatusFormGroupInput): void {
    const postStatusRawValue = { ...this.getFormDefaults(), ...postStatus };
    form.reset(
      {
        ...postStatusRawValue,
        id: { value: postStatusRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PostStatusFormDefaults {
    return {
      id: null,
    };
  }
}
