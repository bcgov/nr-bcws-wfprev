import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SimpleChange } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { YearEndActivityItemComponent } from './year-end-activity-item.component';
import { ProjectService } from '../../../services/project-services';
import { TokenService } from '../../../services/token.service';
import { of } from 'rxjs';

class MockTokenService {
  credentialsEmitter = of(null);
  authTokenEmitter = of('');
  doesUserHaveApplicationPermissions() { return true; }
}

describe('YearEndActivityItemComponent', () => {
  let component: YearEndActivityItemComponent;
  let fixture: ComponentFixture<YearEndActivityItemComponent>;

  beforeEach(async () => {
    const mockProjectService = jasmine.createSpyObj('ProjectService', ['getProjectBoundaries']);

    await TestBed.configureTestingModule({
      imports: [
        YearEndActivityItemComponent,
        CommonModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: ProjectService, useValue: mockProjectService },
        { provide: TokenService, useClass: MockTokenService },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => 'test-project-guid' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(YearEndActivityItemComponent);
    component = fixture.componentInstance;

    component.activity = {
      activityGuid: 'act-123',
      activityStatusCode: { activityStatusCode: 'COMPLETED' },
      reportedSpendAmount: 1000,
      completedAreaHa: 10,
      activityStartDate: '2026-01-01T00:00:00.000Z',
      activityEndDate: '2026-01-10T00:00:00.000Z',
      isCarryForwardInd: false,
      outstandingObligationsInd: false
    };
    component.index = 0;
  });

  it('should create', () => {
    component.ngOnChanges({
      activity: new SimpleChange(null, component.activity, true)
    });
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should initialize form with activity values on Changes', () => {
    component.ngOnChanges({
      activity: new SimpleChange(null, component.activity, true)
    });
    fixture.detectChanges();

    expect(component.form).toBeTruthy();
    expect(component.form.get('activityGuid')?.value).toBe('act-123');
    expect(component.form.get('reportedSpendAmount')?.value).toBe(1000);
  });

  it('should make finalOutcomeComments required when isCarryForwardInd changes to true', () => {
    component.ngOnChanges({
      activity: new SimpleChange(null, component.activity, true)
    });
    fixture.detectChanges();

    const isCarryForwardCtrl = component.form.get('isCarryForwardInd');
    const commentsCtrl = component.form.get('finalOutcomeComments');

    expect(commentsCtrl?.validator).toBeTruthy();

    isCarryForwardCtrl?.setValue(true);
    commentsCtrl?.setValue('');
    expect(commentsCtrl?.valid).toBeFalse();

    isCarryForwardCtrl?.setValue(false);
    commentsCtrl?.setValue('');
    expect(commentsCtrl?.valid).toBeTrue();
  });

  it('should validate reportedSpendAmount is strictly positive (> 0) when status is COMPLETED', () => {
    component.ngOnChanges({
      activity: new SimpleChange(null, component.activity, true)
    });
    fixture.detectChanges();

    const spendCtrl = component.form.get('reportedSpendAmount');
    spendCtrl?.setValue(0);
    expect(spendCtrl?.valid).toBeFalse();

    spendCtrl?.setValue(50);
    expect(spendCtrl?.valid).toBeTrue();
  });

  it('should allow reportedSpendAmount to be 0 when status is CANCELLED', () => {
    component.activity.activityStatusCode.activityStatusCode = 'CANCELLED';
    component.ngOnChanges({
      activity: new SimpleChange(null, component.activity, true)
    });
    fixture.detectChanges();

    const spendCtrl = component.form.get('reportedSpendAmount');
    spendCtrl?.setValue(0);
    expect(spendCtrl?.valid).toBeTrue();
  });

  describe('isMissingInfo', () => {
    beforeEach(() => {
      component.ngOnChanges({
        activity: new SimpleChange(null, component.activity, true)
      });
      fixture.detectChanges();
    });

    it('should set isMissingInfo to false when all required fields are present', () => {
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });

    it('should set isMissingInfo to true when activityStatusCode is missing', () => {
      component.form.get('activityStatusCode')?.setValue('');
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should set isMissingInfo to true when reportedSpendAmount is null', () => {
      component.form.get('reportedSpendAmount')?.setValue(null);
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should set isMissingInfo to true when reportedSpendAmount is 0', () => {
      component.form.get('reportedSpendAmount')?.setValue(0);
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should set isMissingInfo to true when completedAreaHa is 0', () => {
      component.form.get('completedAreaHa')?.setValue(0);
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should set isMissingInfo to true when activityStartDate is missing', () => {
      component.form.get('activityDateRange.activityStartDate')?.setValue('');
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should set isMissingInfo to true when activityEndDate is missing', () => {
      component.form.get('activityDateRange.activityEndDate')?.setValue('');
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should set isMissingInfo to false when all fields are corrected', () => {
      component.form.get('activityStatusCode')?.setValue('');
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();

      component.form.get('activityStatusCode')?.setValue('COMPLETED');
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });

    it('should initialize isMissingInfo to true when activity is missing required fields', () => {
      component.activity = {
        ...component.activity,
        activityStatusCode: null,
        reportedSpendAmount: null,
      };
      component.ngOnChanges({
        activity: new SimpleChange(null, component.activity, true)
      });

      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });
  });
});
