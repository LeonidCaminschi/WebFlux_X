import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { PostStatusService } from '../service/post-status.service';
import { IPostStatus } from '../post-status.model';
import { PostStatusFormService } from './post-status-form.service';

import { PostStatusUpdateComponent } from './post-status-update.component';

describe('PostStatus Management Update Component', () => {
  let comp: PostStatusUpdateComponent;
  let fixture: ComponentFixture<PostStatusUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let postStatusFormService: PostStatusFormService;
  let postStatusService: PostStatusService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PostStatusUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(PostStatusUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PostStatusUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    postStatusFormService = TestBed.inject(PostStatusFormService);
    postStatusService = TestBed.inject(PostStatusService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const postStatus: IPostStatus = { id: 3160 };

      activatedRoute.data = of({ postStatus });
      comp.ngOnInit();

      expect(comp.postStatus).toEqual(postStatus);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPostStatus>>();
      const postStatus = { id: 17672 };
      jest.spyOn(postStatusFormService, 'getPostStatus').mockReturnValue(postStatus);
      jest.spyOn(postStatusService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ postStatus });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: postStatus }));
      saveSubject.complete();

      // THEN
      expect(postStatusFormService.getPostStatus).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(postStatusService.update).toHaveBeenCalledWith(expect.objectContaining(postStatus));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPostStatus>>();
      const postStatus = { id: 17672 };
      jest.spyOn(postStatusFormService, 'getPostStatus').mockReturnValue({ id: null });
      jest.spyOn(postStatusService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ postStatus: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: postStatus }));
      saveSubject.complete();

      // THEN
      expect(postStatusFormService.getPostStatus).toHaveBeenCalled();
      expect(postStatusService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPostStatus>>();
      const postStatus = { id: 17672 };
      jest.spyOn(postStatusService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ postStatus });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(postStatusService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
