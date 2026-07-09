import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormControl } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SimpleChange } from '@angular/core';
import { MatNativeDateModule } from '@angular/material/core';
import { ActivatedRoute } from '@angular/router';
import { FiscalActivityItemComponent } from './fiscal-activity-item.component';
import { ProjectService } from '../../../../services/project-services';
import { TokenService } from '../../../../services/token.service';
import { of } from 'rxjs';

class MockTokenService {
  credentialsEmitter = of(null);
  authTokenEmitter = of('');
  doesUserHaveApplicationPermissions() { return true; }
}

describe('FiscalActivityItemComponent', () => {
  let component: FiscalActivityItemComponent;
  let fixture: ComponentFixture<FiscalActivityItemComponent>;
  let fb: FormBuilder;

  beforeEach(async () => {
    const mockProjectService = jasmine.createSpyObj('ProjectService', ['getProjectBoundaries']);

    await TestBed.configureTestingModule({
      imports: [
        FiscalActivityItemComponent,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        HttpClientTestingModule,
        MatNativeDateModule
      ],
      providers: [
        FormBuilder,
        { provide: ProjectService, useValue: mockProjectService },
        { provide: TokenService, useClass: MockTokenService },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => 'test-project-guid' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FiscalActivityItemComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(FormBuilder);

    component.activityForm = fb.group({
      activityGuid: ['act-1'],
      projectPlanFiscalGuid: [''],
      activityStatusCode: ['ACTIVE'],
      silvicultureBaseGuid: [''],
      silvicultureTechniqueGuid: [''],
      silvicultureMethodGuid: [''],
      riskRatingCode: [{ 'riskRatingCode': 'LOW_RISK' }],
      contractPhaseCode: [''],
      activityFundingSourceGuid: [''],
      activityName: ['Test Fiscal Activity'],
      activityDescription: ['Test Description'],
      activityDateRange: fb.group({
        activityStartDate: ['2026-01-01'],
        activityEndDate: ['2026-01-10']
      }),
      plannedSpendAmount: [''],
      plannedTreatmentAreaHa: [''],
      reportedSpendAmount: [''],
      completedAreaHa: [''],
      isResultsReportableInd: [false],
      outstandingObligationsInd: [false],
      activityComment: [''],
      isPreviousCarryForwardInd: [false],
      isCarryForwardInd: [false],
      finalOutcomeComments: [''],
      isSpatialAddedInd: [false],
      createDate: [''],
      filteredTechniqueCode: [[]],
      filteredMethodCode: [[]],
    });

    component.activity = { activityGuid: 'act-1' };
    component.index = 0;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should return form control using getControl()', () => {
    fixture.detectChanges();
    const ctrl = component.getControl('activityName');
    expect(ctrl).toBeInstanceOf(FormControl);
    expect(ctrl.value).toBe('Test Fiscal Activity');
  });

  it('should disable form when isReadonly is true', () => {
    component.isReadonly = true;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.activityForm.disabled).toBeTrue();
  });

  it('should enable form when isReadonly is false', () => {
    component.isReadonly = false;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.activityForm.enabled).toBeTrue();
  });

  it('should update form disabled state on OnChanges', () => {
    component.isReadonly = true;
    component.ngOnChanges({
      isReadonly: new SimpleChange(false, true, false)
    });
    fixture.detectChanges();
    expect(component.activityForm.disabled).toBeTrue();
  });

  it('should get activity title based on activityName when isResultsReportableInd is false', () => {
    fixture.detectChanges();
    expect(component.getActivityTitle()).toBe('Test Fiscal Activity');
  });

  it('should construct title from base, technique, and method when isResultsReportableInd is true', () => {
    component.activityForm.patchValue({
      isResultsReportableInd: true,
      silvicultureBaseGuid: 'base-1',
      silvicultureTechniqueGuid: 'tech-1',
      silvicultureMethodGuid: 'method-1',
      filteredTechniqueCode: [{ silvicultureTechniqueGuid: 'tech-1', description: 'Tech Description' }],
      filteredMethodCode: [{ silvicultureMethodGuid: 'method-1', description: 'Method Description' }]
    });
    component.silvicultureBaseCode = [{ silvicultureBaseGuid: 'base-1', description: 'Base Description' }];

    fixture.detectChanges();
    expect(component.getActivityTitle()).toBe('Base Description - Tech Description - Method Description');
  });
});
