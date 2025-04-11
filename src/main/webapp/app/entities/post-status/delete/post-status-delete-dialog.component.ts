import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPostStatus } from '../post-status.model';
import { PostStatusService } from '../service/post-status.service';

@Component({
  templateUrl: './post-status-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class PostStatusDeleteDialogComponent {
  postStatus?: IPostStatus;

  protected postStatusService = inject(PostStatusService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.postStatusService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
