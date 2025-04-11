import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'webFluxApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'comment',
    data: { pageTitle: 'webFluxApp.comment.home.title' },
    loadChildren: () => import('./comment/comment.routes'),
  },
  {
    path: 'post',
    data: { pageTitle: 'webFluxApp.post.home.title' },
    loadChildren: () => import('./post/post.routes'),
  },
  {
    path: 'post-status',
    data: { pageTitle: 'webFluxApp.postStatus.home.title' },
    loadChildren: () => import('./post-status/post-status.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
