import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IPostStatus } from '../post-status.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../post-status.test-samples';

import { PostStatusService } from './post-status.service';

const requireRestSample: IPostStatus = {
  ...sampleWithRequiredData,
};

describe('PostStatus Service', () => {
  let service: PostStatusService;
  let httpMock: HttpTestingController;
  let expectedResult: IPostStatus | IPostStatus[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(PostStatusService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a PostStatus', () => {
      const postStatus = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(postStatus).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a PostStatus', () => {
      const postStatus = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(postStatus).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a PostStatus', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of PostStatus', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a PostStatus', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addPostStatusToCollectionIfMissing', () => {
      it('should add a PostStatus to an empty array', () => {
        const postStatus: IPostStatus = sampleWithRequiredData;
        expectedResult = service.addPostStatusToCollectionIfMissing([], postStatus);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(postStatus);
      });

      it('should not add a PostStatus to an array that contains it', () => {
        const postStatus: IPostStatus = sampleWithRequiredData;
        const postStatusCollection: IPostStatus[] = [
          {
            ...postStatus,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addPostStatusToCollectionIfMissing(postStatusCollection, postStatus);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a PostStatus to an array that doesn't contain it", () => {
        const postStatus: IPostStatus = sampleWithRequiredData;
        const postStatusCollection: IPostStatus[] = [sampleWithPartialData];
        expectedResult = service.addPostStatusToCollectionIfMissing(postStatusCollection, postStatus);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(postStatus);
      });

      it('should add only unique PostStatus to an array', () => {
        const postStatusArray: IPostStatus[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const postStatusCollection: IPostStatus[] = [sampleWithRequiredData];
        expectedResult = service.addPostStatusToCollectionIfMissing(postStatusCollection, ...postStatusArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const postStatus: IPostStatus = sampleWithRequiredData;
        const postStatus2: IPostStatus = sampleWithPartialData;
        expectedResult = service.addPostStatusToCollectionIfMissing([], postStatus, postStatus2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(postStatus);
        expect(expectedResult).toContain(postStatus2);
      });

      it('should accept null and undefined values', () => {
        const postStatus: IPostStatus = sampleWithRequiredData;
        expectedResult = service.addPostStatusToCollectionIfMissing([], null, postStatus, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(postStatus);
      });

      it('should return initial array if no PostStatus is added', () => {
        const postStatusCollection: IPostStatus[] = [sampleWithRequiredData];
        expectedResult = service.addPostStatusToCollectionIfMissing(postStatusCollection, undefined, null);
        expect(expectedResult).toEqual(postStatusCollection);
      });
    });

    describe('comparePostStatus', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.comparePostStatus(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 17672 };
        const entity2 = null;

        const compareResult1 = service.comparePostStatus(entity1, entity2);
        const compareResult2 = service.comparePostStatus(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 17672 };
        const entity2 = { id: 3160 };

        const compareResult1 = service.comparePostStatus(entity1, entity2);
        const compareResult2 = service.comparePostStatus(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 17672 };
        const entity2 = { id: 17672 };

        const compareResult1 = service.comparePostStatus(entity1, entity2);
        const compareResult2 = service.comparePostStatus(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
