import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IPostStatus } from '../post-status.model';
import { PostStatusService } from '../service/post-status.service';

const postStatusResolve = (route: ActivatedRouteSnapshot): Observable<null | IPostStatus> => {
  const id = route.params.id;
  if (id) {
    return inject(PostStatusService)
      .find(id)
      .pipe(
        mergeMap((postStatus: HttpResponse<IPostStatus>) => {
          if (postStatus.body) {
            return of(postStatus.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default postStatusResolve;
