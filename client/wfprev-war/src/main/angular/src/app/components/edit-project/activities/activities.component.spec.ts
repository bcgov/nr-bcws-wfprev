import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivitiesComponent } from './activities.component';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import * as moment from 'moment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('ActivitiesComponent', () => {
  let component: ActivitiesComponent;
  let fixture: ComponentFixture<ActivitiesComponent>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;
  let mockSnackbarService: jasmine.SpyObj<MatSnackBar>;
  let mockDialog: jasmine.SpyObj<MatDialog>;

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', ['getFiscalActivities', 'updateFiscalActivities', 'createFiscalActivity']);
    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    mockSnackbarService = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockCodeTableService.fetchCodeTable.and.returnValue(of({ _embedded: { contractPhaseCode: [] } }));
    mockProjectService.getFiscalActivities.and.returnValue(of({ _embedded: { activities: [] } }));

    await TestBed.configureTestingModule({
      imports: [
        ActivitiesComponent,
        ReactiveFormsModule,
        MatExpansionModule,
        MatSlideToggleModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatInputModule,
        BrowserAnimationsModule
      ],
      providers: [
        FormBuilder,
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableService },
        { provide: MatSnackBar, useValue: mockSnackbarService },
        { provide: MatDialog, useValue: mockDialog },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => 'test-project-guid' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ActivitiesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should format date correctly', () => {
    expect(component.getFormattedDate('2025-03-20T00:00:00.000+00:00')).toBe('2025-03-20');
  });

  it('should initialize activities on changes', () => {
    spyOn(component, 'getActivities').and.callThrough();
    component.ngOnChanges({ fiscalGuid: { currentValue: 'test-guid', previousValue: '', firstChange: true, isFirstChange: () => true } });
    expect(component.getActivities).toHaveBeenCalled();
  });

  it('should call getActivities on component initialization', () => {
    spyOn(component, 'getActivities').and.callThrough(); // Spy on getActivities
    spyOn(component, 'loadCodeTables').and.callThrough(); // Spy on loadCodeTables
  
    component.ngOnChanges({
      fiscalGuid: { 
        currentValue: 'test-guid', 
        previousValue: '', 
        firstChange: true, 
        isFirstChange: () => true 
      } 
    });
  
    expect(component.getActivities).toHaveBeenCalled();
    expect(component.loadCodeTables).toHaveBeenCalled();
  });
  
  it('should create an activity form with correct values', () => {
    const testActivity = {
      activityGuid: 'test-guid',
      activityName: 'Test Activity',
      activityStartDate: '2025-03-10T00:00:00.000+00:00',
      activityEndDate: '2025-03-20T00:00:00.000+00:00',
    };

    const form = component.createActivityForm(testActivity);
    expect(form.get('activityGuid')?.value).toBe('test-guid');
    expect(form.get('activityName')?.value).toBe('Test Activity');
    expect(form.get('activityDateRange.activityStartDate')?.value).toBe('2025-03-10');
    expect(form.get('activityDateRange.activityEndDate')?.value).toBe('2025-03-20');
  });

  it('should add a new activity', () => {
    const initialLength = component.activities.length;
    component.addActivity();
    expect(component.activities.length).toBe(initialLength + 1);
    expect(component.activityForms.length).toBe(initialLength + 1);
  });

  it('should mark an activity as dirty on value change', () => {
    const form = component.createActivityForm({}); 
    component.activityForms.push(form);
    component.isActivityDirty.push(false);
  
    form.get('activityName')?.setValue('Updated Name');
  
    form.markAsDirty();
    
    form.updateValueAndValidity();
  
    fixture.detectChanges();
  
    expect(component.isActivityDirty[0]).toBeTrue(); 
  });
  
  
  it('should save an activity and trigger API update', () => {
    const form = component.createActivityForm({
      activityGuid: 'test-guid',
      activityName: 'Updated Activity',
    });
    component.activityForms.push(form);
    component.activities.push({ activityGuid: 'test-guid' });

    mockProjectService.updateFiscalActivities.and.returnValue(of({}));

    component.onSaveActivity(0);
    expect(mockProjectService.updateFiscalActivities).toHaveBeenCalled();
    expect(mockSnackbarService.open).toHaveBeenCalledWith(
      component.messages.activityUpdatedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' }
    );
  });

  it('should correctly toggle activity status', () => {
    const form = component.createActivityForm({
      activityStatusCode: { activityStatusCode: 'ACTIVE' }
    });
    component.activityForms.push(form);
    component.toggleActivityStatus(0);
    expect(component.activityForms[0].get('activityStatusCode')?.value).toBe('COMPLETED');
  });

  it('should delete an activity after confirmation', () => {
    const form = component.createActivityForm({ activityGuid: 'test-guid' });
    component.activityForms.push(form);
    component.activities.push({ activityGuid: 'test-guid' });

    mockDialog.open.and.returnValue({
      afterClosed: () => of(true)
    } as any);

    mockProjectService.deleteActivity = jasmine.createSpy().and.returnValue(of({}));

    component.onDeleteActivity(0);
    expect(mockDialog.open).toHaveBeenCalled();
    expect(mockProjectService.deleteActivity).toHaveBeenCalled();
    expect(mockSnackbarService.open).toHaveBeenCalledWith(
      component.messages.activityDeletedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' }
    );
  });
  
  it('should enable delete button for active activity', () => {
    const form = component.createActivityForm({ activityStatusCode: 'ACTIVE' });
    component.activityForms.push(form);
    expect(component.canDeleteActivity(0)).toBeTrue();
  });

  it('should format the activity title correctly', () => {
    const form = component.createActivityForm({
      activityName: 'Test Activity',
      isResultsReportableInd: false
    });
    component.activityForms.push(form);
    expect(component.getActivityTitle(0)).toBe('Test Activity');
  });

  it('should set form control to pristine on cancel', () => {
    const testActivity = { 
      activityGuid: 'test-guid', 
      activityName: 'Original Name',
      activityStartDate: '2025-03-10T00:00:00.000+00:00',
      activityEndDate: '2025-03-20T00:00:00.000+00:00',
    };

    component.activities.push(testActivity);
    component.originalActivitiesValues.push({ ...testActivity });

    const form = component.createActivityForm(testActivity);
    component.activityForms.push(form);
    component.isActivityDirty.push(true);

    form.get('activityName')?.setValue('Modified Name');

    component.onCancelActivity(0);

    expect(component.isActivityDirty[0]).toBeFalse();
    expect(component.activityForms[0].pristine).toBeTrue();
    expect(component.activityForms[0].get('activityName')?.value).toBe('Original Name');
    expect(component.activityForms[0].get('activityDateRange.activityStartDate')?.value).toBe('2025-03-10');
    expect(component.activityForms[0].get('activityDateRange.activityEndDate')?.value).toBe('2025-03-20');
  });

  it('should disable and clear method field if techniqueGuid is null', () => {
    const form = component.createActivityForm({});
    component.activityForms.push(form);
  
    // Set a default value to method field before calling function
    form.get('silvicultureMethodGuid')?.setValue('some-value');
    form.get('silvicultureMethodGuid')?.enable();
  
    component.onTechniqueChange(null as any, form);
  
    expect(form.get('silvicultureMethodGuid')?.value).toBeNull();
    expect(form.get('silvicultureMethodGuid')?.disabled).toBeTrue();
    expect(component.filteredMethodCode.length).toBe(0);
  });
  
  it('should filter and enable method field when techniqueGuid is provided', () => {
    const form = component.createActivityForm({});
    component.activityForms.push(form);
  
    // Mock silvicultureMethodCode data
    component.silvicultureMethodCode = [
      { silvicultureTechniqueGuid: 'technique1', silvicultureMethodGuid: 'method1' },
      { silvicultureTechniqueGuid: 'technique2', silvicultureMethodGuid: 'method2' }
    ];
  
    component.onTechniqueChange('technique1', form);
  
    expect(component.filteredMethodCode).toEqual([{ silvicultureTechniqueGuid: 'technique1', silvicultureMethodGuid: 'method1' }]);
    expect(form.get('silvicultureMethodGuid')?.enabled).toBeTrue();
    expect(form.get('silvicultureMethodGuid')?.value).toBeNull();
  });

  it('should return "N/A" if activity is missing', () => {
    expect(component.getActivityTitle(0)).toBe('N/A');
  });
  
  it('should return activity name if Results Reportable is OFF', () => {
    const form = component.createActivityForm({
      activityName: 'Manual Activity',
      isResultsReportableInd: false
    });
    component.activityForms.push(form);
  
    expect(component.getActivityTitle(0)).toBe('Manual Activity');
  });
  
  it('should return "N/A" if Results Reportable is ON but no base, technique, or method is set', () => {
    const form = component.createActivityForm({
      isResultsReportableInd: true,
      silvicultureBaseGuid: null,
      silvicultureTechniqueGuid: null,
      silvicultureMethodGuid: null
    });
    component.activityForms.push(form);
  
    expect(component.getActivityTitle(0)).toBe('N/A');
  });
  
  it('should construct title from base, technique, and method when Results Reportable is ON', () => {
    component.silvicultureBaseCode = [{ silvicultureBaseGuid: 'base1', description: 'Base A' }];
    component.silvicultureTechniqueCode = [{ silvicultureTechniqueGuid: 'tech1', description: 'Technique B' }];
    component.silvicultureMethodCode = [{ silvicultureMethodGuid: 'method1', description: 'Method C' }];
  
    const form = component.createActivityForm({
      isResultsReportableInd: true,
      silvicultureBaseGuid: 'base1',
      silvicultureTechniqueGuid: 'tech1',
      silvicultureMethodGuid: 'method1'
    });
    component.activityForms.push(form);
  
    expect(component.getActivityTitle(0)).toBe('Base A - Technique B - Method C');
  });
  
  it('should construct title with missing elements when Results Reportable is ON', () => {
    component.silvicultureBaseCode = [{ silvicultureBaseGuid: 'base1', description: 'Base A' }];
    component.silvicultureTechniqueCode = [{ silvicultureTechniqueGuid: 'tech1', description: 'Technique B' }];
    component.silvicultureMethodCode = [{ silvicultureMethodGuid: 'method1', description: 'Method C' }];
  
    // Only Base and Technique set
    const form1 = component.createActivityForm({
      isResultsReportableInd: true,
      silvicultureBaseGuid: 'base1',
      silvicultureTechniqueGuid: 'tech1',
      silvicultureMethodGuid: null
    });
    component.activityForms.push(form1);
  
    expect(component.getActivityTitle(0)).toBe('Base A - Technique B');
  
    // Only Base set
    const form2 = component.createActivityForm({
      isResultsReportableInd: true,
      silvicultureBaseGuid: 'base1',
      silvicultureTechniqueGuid: null,
      silvicultureMethodGuid: null
    });
    component.activityForms.push(form2);
  
    expect(component.getActivityTitle(1)).toBe('Base A');
  });

  it('should enable required validator and disable activityName when Results Reportable is ON', () => {
    const form = component.createActivityForm({
      isResultsReportableInd: true,
      activityName: 'Original Name'
    });
  
    component.activityForms.push(form);
    spyOn(component, 'getActivityTitle').and.returnValue('Generated Title');
  
    component.toggleResultsReportableInd(0);
  
    expect(form.get('silvicultureBaseGuid')?.validator).toBeTruthy(); // Validator should be set
    expect(form.get('activityName')?.disabled).toBeTrue();
    expect(form.get('activityName')?.value).toBe('Generated Title');
  });
  
  it('should clear validators and enable activityName when Results Reportable is OFF', () => {
    const form = component.createActivityForm({
      isResultsReportableInd: false,
      activityName: 'Original Name'
    });
  
    component.activityForms.push(form);
    spyOn(component, 'getActivityTitle').and.returnValue('Generated Title');
  
    component.toggleResultsReportableInd(0);
  
    expect(form.get('silvicultureBaseGuid')?.validator).toBeNull(); // Validator should be cleared
    expect(form.get('activityName')?.enabled).toBeTrue();
    expect(form.get('activityName')?.value).toBe('');
  });
  
  it('should update activityName dynamically when base, technique, or method changes', () => {
    component.silvicultureBaseCode = [{ silvicultureBaseGuid: 'base1', description: 'Base A' }];
    component.silvicultureTechniqueCode = [{ silvicultureTechniqueGuid: 'tech1', description: 'Technique B' }];
    component.silvicultureMethodCode = [{ silvicultureMethodGuid: 'method1', description: 'Method C' }];
  
    const form = component.createActivityForm({
      isResultsReportableInd: true,
      silvicultureBaseGuid: '',
      silvicultureTechniqueGuid: '',
      silvicultureMethodGuid: ''
    });
  
    component.activityForms.push(form);
    spyOn(component, 'getActivityTitle').and.returnValue('Updated Title');
  
    component.toggleResultsReportableInd(0);
  
    form.get('silvicultureBaseGuid')?.setValue('base1');
    form.get('silvicultureTechniqueGuid')?.setValue('tech1');
    form.get('silvicultureMethodGuid')?.setValue('method1');
  
    component.cd.detectChanges();
  
    expect(form.get('activityName')?.value).toBe('Updated Title');
  });

  it('should disable "Add Activity" button when isNewActivityBeingAdded is true', () => {
    component.fiscalGuid = 'test-guid'; // Ensure button is rendered
    component.isNewActivityBeingAdded = true;
    fixture.detectChanges(); // Update the DOM
  
    const button = fixture.nativeElement.querySelector('.dropdown-button');
    expect(button).not.toBeNull(); // Ensure button exists
    expect(button?.disabled).toBeTrue();
  });
  
  it('should enable "Add Activity" button when isNewActivityBeingAdded is false', () => {
    component.fiscalGuid = 'test-guid'; // Ensure button is rendered
    component.isNewActivityBeingAdded = false;
    fixture.detectChanges(); // Update the DOM
  
    const button = fixture.nativeElement.querySelector('.dropdown-button');
    expect(button).not.toBeNull(); // Ensure button exists
    expect(button?.disabled).toBeFalse();
  });
  
});


