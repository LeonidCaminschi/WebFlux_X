import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IPostStatus } from '../post-status.model';

@Component({
  selector: 'jhi-post-status-detail',
  templateUrl: './post-status-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class PostStatusDetailComponent {
  postStatus = input<IPostStatus | null>(null);

  previousState(): void {
    window.history.back();
  }
}
