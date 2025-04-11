import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPostStatus } from '../post-status.model';
import { PostStatusService } from '../service/post-status.service';
import { PostStatusFormGroup, PostStatusFormService } from './post-status-form.service';

@Component({
  selector: 'jhi-post-status-update',
  templateUrl: './post-status-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PostStatusUpdateComponent implements OnInit {
  isSaving = false;
  postStatus: IPostStatus | null = null;

  protected postStatusService = inject(PostStatusService);
  protected postStatusFormService = inject(PostStatusFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: PostStatusFormGroup = this.postStatusFormService.createPostStatusFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ postStatus }) => {
      this.postStatus = postStatus;
      if (postStatus) {
        this.updateForm(postStatus);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const postStatus = this.postStatusFormService.getPostStatus(this.editForm);
    if (postStatus.id !== null) {
      this.subscribeToSaveResponse(this.postStatusService.update(postStatus));
    } else {
      this.subscribeToSaveResponse(this.postStatusService.create(postStatus));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPostStatus>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(postStatus: IPostStatus): void {
    this.postStatus = postStatus;
    this.postStatusFormService.resetForm(this.editForm, postStatus);
  }
}
