import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { YearEndPerformanceUpdateComponent } from './year-end-performance-update.component';
import { PermissionsService } from 'src/app/services/permissions.service';
import { ResourcesRoutes } from 'src/app/utils';

describe('YearEndPerformanceUpdateComponent', () => {
  let component: YearEndPerformanceUpdateComponent;
  let fixture: ComponentFixture<YearEndPerformanceUpdateComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: any;
  let mockPermissionsService: jasmine.SpyObj<PermissionsService>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockPermissionsService = jasmine.createSpyObj('PermissionsService', ['hasAction']);
    mockActivatedRoute = {
      snapshot: {
        queryParamMap: {
          get: (key: string) => {
            if (key === 'projectGuid') return 'test-project-guid';
            if (key === 'fiscalGuid') return 'test-fiscal-guid';
            return null;
          }
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [YearEndPerformanceUpdateComponent],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: PermissionsService, useValue: mockPermissionsService }
      ]
    }).compileComponents();
  });

  describe('authorized user', () => {
    beforeEach(() => {
      mockPermissionsService.hasAction.and.returnValue(true);
      fixture = TestBed.createComponent(YearEndPerformanceUpdateComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize projectGuid and fiscalGuid from query params', () => {
      expect(component.projectGuid).toBe('test-project-guid');
      expect(component.fiscalGuid).toBe('test-fiscal-guid');
    });

    it('should navigate back to project on goBack()', () => {
      component.goBack();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/' + ResourcesRoutes.EDIT_PROJECT], {
        queryParams: {
          projectGuid: 'test-project-guid',
          fiscalGuid: 'test-fiscal-guid'
        }
      });
    });
  });

  describe('unauthorized user', () => {
    beforeEach(() => {
      mockPermissionsService.hasAction.and.returnValue(false);
      fixture = TestBed.createComponent(YearEndPerformanceUpdateComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should redirect unauthorized user to error page', () => {
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/' + ResourcesRoutes.ERROR_PAGE]);
    });
  });
});
