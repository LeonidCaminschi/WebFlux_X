import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import PostStatusResolve from './route/post-status-routing-resolve.service';

const postStatusRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/post-status.component').then(m => m.PostStatusComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/post-status-detail.component').then(m => m.PostStatusDetailComponent),
    resolve: {
      postStatus: PostStatusResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/post-status-update.component').then(m => m.PostStatusUpdateComponent),
    resolve: {
      postStatus: PostStatusResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/post-status-update.component').then(m => m.PostStatusUpdateComponent),
    resolve: {
      postStatus: PostStatusResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default postStatusRoute;
