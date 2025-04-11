import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IPostStatus, NewPostStatus } from '../post-status.model';

export type PartialUpdatePostStatus = Partial<IPostStatus> & Pick<IPostStatus, 'id'>;

export type EntityResponseType = HttpResponse<IPostStatus>;
export type EntityArrayResponseType = HttpResponse<IPostStatus[]>;

@Injectable({ providedIn: 'root' })
export class PostStatusService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/post-statuses');

  create(postStatus: NewPostStatus): Observable<EntityResponseType> {
    return this.http.post<IPostStatus>(this.resourceUrl, postStatus, { observe: 'response' });
  }

  update(postStatus: IPostStatus): Observable<EntityResponseType> {
    return this.http.put<IPostStatus>(`${this.resourceUrl}/${this.getPostStatusIdentifier(postStatus)}`, postStatus, {
      observe: 'response',
    });
  }

  partialUpdate(postStatus: PartialUpdatePostStatus): Observable<EntityResponseType> {
    return this.http.patch<IPostStatus>(`${this.resourceUrl}/${this.getPostStatusIdentifier(postStatus)}`, postStatus, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IPostStatus>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IPostStatus[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getPostStatusIdentifier(postStatus: Pick<IPostStatus, 'id'>): number {
    return postStatus.id;
  }

  comparePostStatus(o1: Pick<IPostStatus, 'id'> | null, o2: Pick<IPostStatus, 'id'> | null): boolean {
    return o1 && o2 ? this.getPostStatusIdentifier(o1) === this.getPostStatusIdentifier(o2) : o1 === o2;
  }

  addPostStatusToCollectionIfMissing<Type extends Pick<IPostStatus, 'id'>>(
    postStatusCollection: Type[],
    ...postStatusesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const postStatuses: Type[] = postStatusesToCheck.filter(isPresent);
    if (postStatuses.length > 0) {
      const postStatusCollectionIdentifiers = postStatusCollection.map(postStatusItem => this.getPostStatusIdentifier(postStatusItem));
      const postStatusesToAdd = postStatuses.filter(postStatusItem => {
        const postStatusIdentifier = this.getPostStatusIdentifier(postStatusItem);
        if (postStatusCollectionIdentifiers.includes(postStatusIdentifier)) {
          return false;
        }
        postStatusCollectionIdentifiers.push(postStatusIdentifier);
        return true;
      });
      return [...postStatusesToAdd, ...postStatusCollection];
    }
    return postStatusCollection;
  }
}
