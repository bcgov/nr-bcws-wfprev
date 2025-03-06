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
    const testActivity = { activityGuid: 'test-guid', activityName: 'Original Name' };
    component.activities.push(testActivity);
    const form = component.createActivityForm(testActivity);
    component.activityForms.push(form);
    component.isActivityDirty.push(true); // Mark as dirty
  
    form.get('activityName')?.setValue('Modified Name');
    component.onCancelActivity(0);
  
    expect(component.isActivityDirty[0]).toBeFalse();
    expect(component.activityForms[0].pristine).toBeTrue();
  });
  

});
