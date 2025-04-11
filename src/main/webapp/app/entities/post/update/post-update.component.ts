import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPostStatus } from 'app/entities/post-status/post-status.model';
import { PostStatusService } from 'app/entities/post-status/service/post-status.service';
import { IPost } from '../post.model';
import { PostService } from '../service/post.service';
import { PostFormGroup, PostFormService } from './post-form.service';

@Component({
  selector: 'jhi-post-update',
  templateUrl: './post-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PostUpdateComponent implements OnInit {
  isSaving = false;
  post: IPost | null = null;

  postStatusesCollection: IPostStatus[] = [];

  protected postService = inject(PostService);
  protected postFormService = inject(PostFormService);
  protected postStatusService = inject(PostStatusService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: PostFormGroup = this.postFormService.createPostFormGroup();

  comparePostStatus = (o1: IPostStatus | null, o2: IPostStatus | null): boolean => this.postStatusService.comparePostStatus(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ post }) => {
      this.post = post;
      if (post) {
        this.updateForm(post);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const post = this.postFormService.getPost(this.editForm);
    if (post.id !== null) {
      this.subscribeToSaveResponse(this.postService.update(post));
    } else {
      this.subscribeToSaveResponse(this.postService.create(post));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPost>>): void {
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

  protected updateForm(post: IPost): void {
    this.post = post;
    this.postFormService.resetForm(this.editForm, post);

    this.postStatusesCollection = this.postStatusService.addPostStatusToCollectionIfMissing<IPostStatus>(
      this.postStatusesCollection,
      post.postStatus,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.postStatusService
      .query({ filter: 'post-is-null' })
      .pipe(map((res: HttpResponse<IPostStatus[]>) => res.body ?? []))
      .pipe(
        map((postStatuses: IPostStatus[]) =>
          this.postStatusService.addPostStatusToCollectionIfMissing<IPostStatus>(postStatuses, this.post?.postStatus),
        ),
      )
      .subscribe((postStatuses: IPostStatus[]) => (this.postStatusesCollection = postStatuses));
  }
}
