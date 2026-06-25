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

  it('should validate reportedSpendAmount is non negative when status is COMPLETED', () => {
    component.ngOnChanges({
      activity: new SimpleChange(null, component.activity, true)
    });
    fixture.detectChanges();

    const spendCtrl = component.form.get('reportedSpendAmount');
    spendCtrl?.setValue(0);
    expect(spendCtrl?.valid).toBeTrue();

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

    it('should not set isMissingInfo to true when completedAreaHa is 0', () => {
      component.form.get('completedAreaHa')?.setValue(0);
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
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

  describe('isMissingInfo - COMPLETED status', () => {
    beforeEach(() => {
      component.activity = {
        ...component.activity,
        activityStatusCode: { activityStatusCode: 'COMPLETED' },
        reportedSpendAmount: 1000,
        completedAreaHa: 10,
        activityStartDate: '2026-01-01T00:00:00.000Z',
        activityEndDate: '2026-01-10T00:00:00.000Z',
        isCarryForwardInd: false,
        outstandingObligationsInd: false,
        isResultsReportableInd: false
      };
      component.ngOnChanges({
        activity: new SimpleChange(null, component.activity, true)
      });
      fixture.detectChanges();
    });

    it('should not set isMissingInfo when reportedSpendAmount is 0 and finalOutcomeComments is present', () => {
      component.form.get('reportedSpendAmount')?.setValue(0);
      component.form.get('finalOutcomeComments')?.setValue('Some comment');
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });

    it('should set isMissingInfo when outstandingObligationsInd is true and activityComment is blank', () => {
      component.form.get('outstandingObligationsInd')?.setValue(true);
      component.form.get('activityComment')?.setValue('');
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should not set isMissingInfo when outstandingObligationsInd is true and activityComment is present', () => {
      component.form.get('outstandingObligationsInd')?.setValue(true);
      component.form.get('activityComment')?.setValue('Addressing obligations');
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });

    it('should set isMissingInfo when isResultsReportableInd is true and no spatial file', () => {
      component.activity = { ...component.activity, isResultsReportableInd: true, isSpatialAddedInd: false };
      component.updateMissingInfo();
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should not set isMissingInfo when isResultsReportableInd is true and spatial file present', () => {
      component.activity = { ...component.activity, isResultsReportableInd: true, isSpatialAddedInd: true };
      component.updateMissingInfo();
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });

    it('should not set isMissingInfo when isResultsReportableInd is false and no spatial file', () => {
      component.activity = { ...component.activity, isResultsReportableInd: false, isSpatialAddedInd: false };
      component.updateMissingInfo();
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });
  });

  describe('isMissingInfo - DEFERRED status', () => {
    beforeEach(() => {
      component.activity = {
        ...component.activity,
        activityStatusCode: { activityStatusCode: 'DEFERRED' },
        reportedSpendAmount: 0,
        completedAreaHa: 0,
        activityStartDate: '2026-01-01T00:00:00.000Z',
        activityEndDate: '2026-01-10T00:00:00.000Z',
        finalOutcomeComments: 'Deferred due to weather',
        outstandingObligationsInd: false,
        isResultsReportableInd: false
      };
      component.ngOnChanges({
        activity: new SimpleChange(null, component.activity, true)
      });
      fixture.detectChanges();
    });

    it('should not set isMissingInfo when all required fields are present', () => {
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });

    it('should set isMissingInfo when finalOutcomeComments is blank', () => {
      component.form.get('finalOutcomeComments')?.setValue('');
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should set isMissingInfo when reportedSpendAmount is null', () => {
      component.form.get('reportedSpendAmount')?.setValue(null);
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should set isMissingInfo when completedAreaHa is null', () => {
      component.form.get('completedAreaHa')?.setValue(null);
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should set isMissingInfo when outstandingObligationsInd is true and activityComment is blank', () => {
      component.form.get('outstandingObligationsInd')?.setValue(true);
      component.form.get('activityComment')?.setValue('');
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });

    it('should not set isMissingInfo when outstandingObligationsInd is true and activityComment is present', () => {
      component.form.get('outstandingObligationsInd')?.setValue(true);
      component.form.get('activityComment')?.setValue('Will address next fiscal');
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });
  });

  describe('isMissingInfo - CANCELLED status', () => {
    beforeEach(() => {
      component.activity = {
        ...component.activity,
        activityStatusCode: { activityStatusCode: 'CANCELLED' },
        finalOutcomeComments: 'Project cancelled due to budget'
      };
      component.ngOnChanges({
        activity: new SimpleChange(null, component.activity, true)
      });
      fixture.detectChanges();
    });

    it('should not set isMissingInfo when finalOutcomeComments is present', () => {
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });

    it('should set isMissingInfo when finalOutcomeComments is blank', () => {
      component.form.get('finalOutcomeComments')?.setValue('');
      expect(component.form.get('isMissingInfo')?.value).toBeTrue();
    });
  });

  describe('CANCELLED status - validator behaviour', () => {
    beforeEach(() => {
      component.activity = {
        ...component.activity,
        activityStatusCode: { activityStatusCode: 'CANCELLED' },
        reportedSpendAmount: null,
        completedAreaHa: null,
        finalOutcomeComments: 'Cancelled due to budget',
        outstandingObligationsInd: false
      };
      component.ngOnChanges({
        activity: new SimpleChange(null, component.activity, true)
      });
      fixture.detectChanges();
    });

    it('should not require reportedSpendAmount when status is CANCELLED', () => {
      const ctrl = component.form.get('reportedSpendAmount');
      ctrl?.setValue(null);
      expect(ctrl?.hasError('required')).toBeFalse();
    });

    it('should not require completedAreaHa when status is CANCELLED', () => {
      const ctrl = component.form.get('completedAreaHa');
      ctrl?.setValue(null);
      expect(ctrl?.hasError('required')).toBeFalse();
    });

    it('should not require activityComment when outstandingObligationsInd is true and status is CANCELLED', () => {
      component.form.get('outstandingObligationsInd')?.setValue(true);
      const ctrl = component.form.get('activityComment');
      ctrl?.setValue('');
      expect(ctrl?.hasError('required')).toBeFalse();
    });

    it('should not set isMissingInfo when outstandingObligationsInd is true and activityComment is blank', () => {
      component.form.get('outstandingObligationsInd')?.setValue(true);
      component.form.get('activityComment')?.setValue('');
      expect(component.form.get('isMissingInfo')?.value).toBeFalse();
    });
  });

  describe('status change - validator toggling', () => {
    beforeEach(() => {
      component.activity = {
        ...component.activity,
        activityStatusCode: { activityStatusCode: 'COMPLETED' },
        reportedSpendAmount: 1000,
        completedAreaHa: 10,
        outstandingObligationsInd: true,
        activityComment: ''
      };
      component.ngOnChanges({
        activity: new SimpleChange(null, component.activity, true)
      });
      fixture.detectChanges();
    });

    it('should remove required from reportedSpendAmount when switching to CANCELLED', () => {
      component.form.get('activityStatusCode')?.setValue('CANCELLED');
      const ctrl = component.form.get('reportedSpendAmount');
      ctrl?.setValue(null);
      expect(ctrl?.hasError('required')).toBeFalse();
    });

    it('should remove required from completedAreaHa when switching to CANCELLED', () => {
      component.form.get('activityStatusCode')?.setValue('CANCELLED');
      const ctrl = component.form.get('completedAreaHa');
      ctrl?.setValue(null);
      expect(ctrl?.hasError('required')).toBeFalse();
    });

    it('should remove required from activityComment when switching to CANCELLED', () => {
      component.form.get('activityStatusCode')?.setValue('CANCELLED');
      const ctrl = component.form.get('activityComment');
      ctrl?.setValue('');
      expect(ctrl?.hasError('required')).toBeFalse();
    });

    it('should re-add required to reportedSpendAmount when switching from CANCELLED to COMPLETED', () => {
      component.form.get('activityStatusCode')?.setValue('CANCELLED');
      component.form.get('activityStatusCode')?.setValue('COMPLETED');
      const ctrl = component.form.get('reportedSpendAmount');
      ctrl?.setValue(null);
      expect(ctrl?.hasError('required')).toBeTrue();
    });

    it('should re-add required to activityComment when switching from CANCELLED to COMPLETED with obligations checked', () => {
      component.form.get('activityStatusCode')?.setValue('CANCELLED');
      component.form.get('activityStatusCode')?.setValue('COMPLETED');
      const ctrl = component.form.get('activityComment');
      ctrl?.setValue('');
      expect(ctrl?.hasError('required')).toBeTrue();
    });

    it('should not re-add required to activityComment when switching from CANCELLED to COMPLETED without obligations checked', () => {
      component.form.get('outstandingObligationsInd')?.setValue(false);
      component.form.get('activityStatusCode')?.setValue('CANCELLED');
      component.form.get('activityStatusCode')?.setValue('COMPLETED');
      const ctrl = component.form.get('activityComment');
      ctrl?.setValue('');
      expect(ctrl?.hasError('required')).toBeFalse();
    });
  });
});
