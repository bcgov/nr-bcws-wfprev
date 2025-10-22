import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivitiesComponent } from './activities.component';
import { ReactiveFormsModule, FormBuilder, FormControl } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { Observable, of, Subject, throwError } from 'rxjs';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { fakeAsync, tick } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DebugElement } from '@angular/core';
import { IconButtonComponent } from 'src/app/components/shared/icon-button/icon-button.component';
import { By } from '@angular/platform-browser';

describe('ActivitiesComponent', () => {
  let component: ActivitiesComponent;
  let fixture: ComponentFixture<ActivitiesComponent>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;
  let mockSnackbarService: jasmine.SpyObj<MatSnackBar>;
  let mockDialog: jasmine.SpyObj<MatDialog>;

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', [
      'getFiscalActivities',
      'updateFiscalActivities',
      'createFiscalActivity',
      'getProjectByProjectGuid'
    ]);

    mockProjectService.getProjectByProjectGuid.and.returnValue(of({ projectTypeCode: { projectTypeCode: 'STANDARD' } }));

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
        BrowserAnimationsModule,
        HttpClientTestingModule
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

  it('should not add when a new activity is already being added', () => {
    component.isNewActivityBeingAdded = true;
    const lenghA = component.activities.length;
    const lenghF = component.activityForms.length;

    component.addActivity();

    expect(component.activities.length).toBe(lenghA);
    expect(component.activityForms.length).toBe(lenghF);
  });

  it('should create only after panel opens when collapsed', fakeAsync(() => {
    const opened$ = new Subject<void>();
    (component as any).activitiesPanel = {
      expanded: false,
      open: jasmine.createSpy('open'),
      opened: opened$
    } as any;

    const before = component.activities.length;

    component.addActivity();
    expect((component as any).activitiesPanel.open).toHaveBeenCalledTimes(1);
    expect(component.activities.length).toBe(before);

    opened$.next();
    opened$.complete();
    tick(0);

    expect(component.activities.length).toBe(before + 1);
    expect(component.activityForms.length).toBe(before + 1);
  }));

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
    const activity = { activityStatusCode: 'ACTIVE' };
    component.activities.push(activity);
    const form = component.createActivityForm(activity);
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

    expect(form.get('filteredMethodCode')?.value).toEqual([
      { silvicultureTechniqueGuid: 'technique1', silvicultureMethodGuid: 'method1' }
    ]);
    expect(form.get('silvicultureMethodGuid')?.enabled).toBeTrue();
    expect(form.get('silvicultureMethodGuid')?.value).toBeNull();
  });


  it('should return "" if activity is missing', () => {
    expect(component.getActivityTitle(0)).toBe('');
  });

  it('should return activity name if Results Reportable is OFF', () => {
    const form = component.createActivityForm({
      activityName: 'Manual Activity',
      isResultsReportableInd: false
    });
    component.activityForms.push(form);

    expect(component.getActivityTitle(0)).toBe('Manual Activity');
  });

  it('should return "" if Results Reportable is ON but no base, technique, or method is set', () => {
    const form = component.createActivityForm({
      isResultsReportableInd: true,
      silvicultureBaseGuid: null,
      silvicultureTechniqueGuid: null,
      silvicultureMethodGuid: null
    });
    component.activityForms.push(form);

    expect(component.getActivityTitle(0)).toBe('');
  });

  it('should construct title with missing elements when Results Reportable is ON', () => {
    component.silvicultureBaseCode = [{ silvicultureBaseGuid: 'base1', description: 'Base A' }];
    component.silvicultureTechniqueCode = [{ silvicultureTechniqueGuid: 'tech1', description: 'Technique B', silvicultureBaseGuid: 'base1' }];
    component.silvicultureMethodCode = [{ silvicultureMethodGuid: 'method1', description: 'Method C' }];

    const form = component.createActivityForm({
      isResultsReportableInd: true,
      silvicultureBaseGuid: 'base1',
      silvicultureTechniqueGuid: 'tech1',
      silvicultureMethodGuid: null
    });
    component.activityForms.push(form);

    expect(component.getActivityTitle(0)).toBe('Base A - Technique B');
  });


  it('should construct title with missing elements when Results Reportable is ON', () => {
    component.silvicultureBaseCode = [{ silvicultureBaseGuid: 'base1', description: 'Base A' }];
    component.silvicultureTechniqueCode = [{ silvicultureTechniqueGuid: 'tech1', description: 'Technique B', silvicultureBaseGuid: 'base1' }];
    component.silvicultureMethodCode = [{ silvicultureMethodGuid: 'method1', description: 'Method C' }];

    // Only Base and Technique set
    const form1 = component.createActivityForm({
      isResultsReportableInd: true,
      silvicultureBaseGuid: 'base1',
      silvicultureTechniqueGuid: 'tech1',
      silvicultureMethodGuid: null
    });

    component.activityForms.push(form1);
    component.cd.detectChanges();

    expect(component.getActivityTitle(0)).toBe('Base A - Technique B');

    const form2 = component.createActivityForm({
      isResultsReportableInd: true,
      silvicultureBaseGuid: 'base1',
      silvicultureTechniqueGuid: null,
      silvicultureMethodGuid: null
    });

    component.activityForms.push(form2);
    component.cd.detectChanges();

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
    component.fiscalGuid = 'test-guid';
    component.isNewActivityBeingAdded = true;
    fixture.detectChanges();

    const debugEl: DebugElement = fixture.debugElement.query(By.directive(IconButtonComponent));
    expect(debugEl).withContext('IconButtonComponent should exist').not.toBeNull();

    const buttonInstance = debugEl.componentInstance as IconButtonComponent;
    expect(buttonInstance.disabled).withContext('Button should be disabled').toBeTrue();
  });

  it('should enable "Add Activity" button when isNewActivityBeingAdded is false', () => {
    component.fiscalGuid = 'test-guid';
    component.isNewActivityBeingAdded = false;
    fixture.detectChanges();

    const debugEl: DebugElement = fixture.debugElement.query(By.directive(IconButtonComponent));
    expect(debugEl).withContext('IconButtonComponent should exist').not.toBeNull();

    const buttonInstance = debugEl.componentInstance as IconButtonComponent;
    expect(buttonInstance.disabled).withContext('Button should not be disabled').toBeFalse();
  });

  it('should sort an array of objects by a given key', () => {
    const testArray = [
      { name: 'Charlie' },
      { name: 'Alice' },
      { name: 'Bob' }
    ];

    const sortedArray = component.sortArray(testArray, 'name');
    expect(sortedArray).toEqual([
      { name: 'Alice' },
      { name: 'Bob' },
      { name: 'Charlie' }
    ]);
  });

  it('should sort an array of strings when no key is provided', () => {
    const testArray = ['Charlie', 'Alice', 'Bob'];

    const sortedArray = component.sortArray(testArray);
    expect(sortedArray).toEqual(['Alice', 'Bob', 'Charlie']);
  });

  it('should return an empty array when input is null or undefined', () => {
    expect(component.sortArray(null as any)).toEqual([]);
    expect(component.sortArray(undefined as any)).toEqual([]);
  });

  it('should return the original array if there is no key and all elements are equal', () => {
    const testArray = ['same', 'same', 'same'];

    const sortedArray = component.sortArray(testArray);
    expect(sortedArray).toEqual(['same', 'same', 'same']);
  });

  it('should detect if any form is dirty', () => {
    const form1 = component.createActivityForm({});
    const form2 = component.createActivityForm({});

    form1.markAsPristine();
    form2.markAsDirty();

    component.activityForms.push(form1, form2);

    expect(component.isFormDirty()).toBeTrue();
  });

  it('should return false if no forms are dirty', () => {
    const form1 = component.createActivityForm({});
    const form2 = component.createActivityForm({});

    form1.markAsPristine();
    form2.markAsPristine();

    component.activityForms.push(form1, form2);

    expect(component.isFormDirty()).toBeFalse();
  });

  it('should return an observable from canDeactivate when a form is dirty', () => {
    const form = component.createActivityForm({});
    component.activityForms.push(form);

    form.markAsDirty();

    mockDialog.open.and.returnValue({
      afterClosed: () => of(true)
    } as any);

    const result = component.canDeactivate();

    expect(mockDialog.open).toHaveBeenCalledWith(jasmine.any(Function), jasmine.any(Object));
    expect(result).toBeInstanceOf(Observable);
  });

  it('should return true from canDeactivate when no forms are dirty', () => {
    const form = component.createActivityForm({});
    component.activityForms.push(form);

    form.markAsPristine();

    expect(component.canDeactivate()).toBeTrue();
  });

  it('should toggle activity status correctly', () => {
    const form = component.createActivityForm({ activityStatusCode: 'ACTIVE' });
    component.activityForms.push(form);

    component.toggleActivityStatus(0);
    expect(component.activityForms[0].get('activityStatusCode')?.value).toBe('COMPLETED');

    component.toggleActivityStatus(0);
    expect(component.activityForms[0].get('activityStatusCode')?.value).toBe('ACTIVE');
  });

  it('should update technique and method options on base change', () => {
    const form = component.createActivityForm({});
    component.activityForms.push(form);

    component.silvicultureTechniqueCode = [{ silvicultureBaseGuid: 'base1', silvicultureTechniqueGuid: 'tech1' }];
    component.silvicultureMethodCode = [{ silvicultureTechniqueGuid: 'tech1', silvicultureMethodGuid: 'method1' }];

    component.onBaseChange('base1', form);

    expect(form.get('filteredTechniqueCode')?.value).toEqual([{ silvicultureBaseGuid: 'base1', silvicultureTechniqueGuid: 'tech1' }]);
    expect(form.get('filteredMethodCode')?.value).toEqual([]);
    expect(form.get('silvicultureTechniqueGuid')?.enabled).toBeTrue();
    expect(form.get('silvicultureMethodGuid')?.disabled).toBeTrue();
  });


  it('should return empty string when date is null or undefined', () => {
    expect(component.getFormattedDate(null)).toBe('');
    expect(component.getFormattedDate(undefined as any)).toBe('');
  });


  it('should reset the form to original values when cancelling an edit', () => {
    const originalActivity = { activityGuid: 'test-guid', activityName: 'Original Name' };
    component.activities.push(originalActivity);
    component.originalActivitiesValues.push(originalActivity);

    const form = component.createActivityForm(originalActivity);
    component.activityForms.push(form);

    form.get('activityName')?.setValue('Modified Name');
    component.onCancelActivity(0);

    expect(component.activityForms[0].get('activityName')?.value).toBe('Original Name');
  });

  it('should handle errors in getActivities gracefully', () => {
    mockProjectService.getFiscalActivities.and.returnValue(of({ _embedded: null }));

    expect(() => component.getActivities()).not.toThrow();
    expect(component.activities).toEqual([]);
  });

  describe('expandAndScrollToActivity', () => {
    beforeEach(() => {
      component.activities = [
        { activityGuid: '123' },
        { activityGuid: '456' },
        { activityGuid: '789' },
      ];
      component.expandedPanels = [false, false, false];
    });

    it('should expand only the specified activity and scroll to it', fakeAsync(() => {
      const scrollSpy = jasmine.createSpy('scrollIntoView');

      spyOn(document, 'getElementById').and.callFake((id) => {
        if (id === 'activity-1') {
          return { scrollIntoView: scrollSpy } as any;
        }
        return null;
      });

      component.expandAndScrollToActivity('456');

      tick(100);

      expect(component.expandedPanels).toEqual([false, true, false]);
      expect(document.getElementById).toHaveBeenCalledWith('activity-1');
      expect(scrollSpy).toHaveBeenCalledWith({
        behavior: 'smooth',
        block: 'start',
      });
    }));

    it('should do nothing if activityGuid is not found', fakeAsync(() => {
      spyOn(document, 'getElementById');

      component.expandAndScrollToActivity('999');

      tick(100);

      expect(component.expandedPanels).toEqual([false, false, false]);
      expect(document.getElementById).not.toHaveBeenCalled();
    }));
  });

  it('should remove a new activity when onCancelActivity is called', () => {
    const newActivity = { activityGuid: null, activityName: 'New Activity' };
    component.activities.push(newActivity);
    component.activityForms.push(component.createActivityForm(newActivity));
    component.isNewActivityBeingAdded = true;

    const initialLength = component.activities.length;

    component.onCancelActivity(0);

    expect(component.activities.length).toBe(initialLength - 1);
    expect(component.activityForms.length).toBe(initialLength - 1);
    expect(component.isNewActivityBeingAdded).toBeFalse();
  });

  it('should remove an unsaved activity when onDeleteActivity is called', () => {
    const unsavedActivity = { activityGuid: null, activityName: 'Unsaved Activity' };
    component.activities.push(unsavedActivity);
    component.activityForms.push(component.createActivityForm(unsavedActivity));
    component.isNewActivityBeingAdded = true;

    const initialLength = component.activities.length;

    mockDialog.open.and.returnValue({
      afterClosed: () => of(true)
    } as any);

    component.onDeleteActivity(0);

    expect(component.activities.length).toBe(initialLength - 1);
    expect(component.activityForms.length).toBe(initialLength - 1);
    expect(component.isNewActivityBeingAdded).toBeFalse();
    expect(mockDialog.open).toHaveBeenCalled();
  });

  it('should exit early in getActivities if fiscalGuid is missing', () => {
    component.fiscalGuid = '';
    component.getActivities();
    expect(mockProjectService.getFiscalActivities).not.toHaveBeenCalled();
  });

  it('should exit early in getActivities if projectGuid is missing', () => {
    component.fiscalGuid = 'test-fiscal-guid';
    const route = TestBed.inject(ActivatedRoute);
    spyOn(route.snapshot.queryParamMap, 'get').and.returnValue(null); // simulate missing projectGuid
    component.getActivities();
    expect(mockProjectService.getFiscalActivities).not.toHaveBeenCalled();
  });

  it('should handle empty activities array gracefully', () => {
    component.fiscalGuid = 'test-fiscal-guid';
    spyOn(TestBed.inject(ActivatedRoute).snapshot.queryParamMap, 'get').and.returnValue('project-guid');
    mockProjectService.getFiscalActivities.and.returnValue(of({ _embedded: { activities: [] } }));
    component.getActivities();
    expect(component.activities).toEqual([]);
  });

  it('should call callback after fetching activities', () => {
    component.fiscalGuid = 'test-fiscal-guid';
    spyOn(TestBed.inject(ActivatedRoute).snapshot.queryParamMap, 'get').and.returnValue('project-guid');
    const callback = jasmine.createSpy('callback');
    mockProjectService.getFiscalActivities.and.returnValue(of({
      _embedded: { activities: [{ activityName: 'B' }, { activityName: 'A' }] }
    }));
    component.getActivities(callback);
    expect(callback).toHaveBeenCalled();
    expect(component.activities[0].activityName).toBe('A');
  });

  it('should handle error and show snackbar when getFiscalActivities fails', () => {
    component.fiscalGuid = 'test-fiscal-guid';
    spyOn(TestBed.inject(ActivatedRoute).snapshot.queryParamMap, 'get').and.returnValue('project-guid');
    spyOn(console, 'error');
    mockProjectService.getFiscalActivities.and.returnValue(throwError(() => new Error('API failure')));

    component.getActivities();
    expect(mockSnackbarService.open).toHaveBeenCalledWith(
      'Failed to load activities. Please try again later.',
      'OK',
      { duration: 5000, panelClass: 'snackbar-error' }
    );
  });

  it('should reset technique and method if baseGuid is empty', () => {
    const form = component.createActivityForm({
      silvicultureTechniqueGuid: 'tech1',
      silvicultureMethodGuid: 'method1'
    });

    component.activityForms.push(form);
    form.get('silvicultureTechniqueGuid')?.enable();
    form.get('silvicultureMethodGuid')?.enable();

    component.onBaseChange('', form);

    expect(form.get('silvicultureTechniqueGuid')?.disabled).toBeTrue();
    expect(form.get('silvicultureMethodGuid')?.disabled).toBeTrue();
    expect(form.get('silvicultureTechniqueGuid')?.value).toBeNull();
    expect(form.get('silvicultureMethodGuid')?.value).toBeNull();
  });

  it('should reset technique and method if current technique is invalid for base', () => {
    component.silvicultureTechniqueCode = [{ silvicultureBaseGuid: 'base1', silvicultureTechniqueGuid: 'techA' }];
    const form = component.createActivityForm({
      silvicultureTechniqueGuid: 'invalid-tech',
      silvicultureMethodGuid: 'method1'
    });

    component.activityForms.push(form);
    component.onBaseChange('base1', form);

    expect(form.get('silvicultureTechniqueGuid')?.value).toBeNull();
    expect(form.get('silvicultureMethodGuid')?.value).toBeNull();
    expect(form.get('silvicultureTechniqueGuid')?.disabled).toBeFalse();
    expect(form.get('silvicultureMethodGuid')?.disabled).toBeTrue();
  });

  it('should reset method only if current method is invalid for valid technique', () => {
    component.silvicultureTechniqueCode = [{ silvicultureBaseGuid: 'base1', silvicultureTechniqueGuid: 'tech1' }];
    component.silvicultureMethodCode = [
      { silvicultureTechniqueGuid: 'tech1', silvicultureMethodGuid: 'methodA' }
    ];

    const form = component.createActivityForm({
      silvicultureBaseGuid: 'base1',
      silvicultureTechniqueGuid: 'tech1',
      silvicultureMethodGuid: 'invalid-method'
    });

    component.activityForms.push(form);
    component.onBaseChange('base1', form);

    expect(form.get('silvicultureTechniqueGuid')?.enabled).toBeTrue();
    expect(form.get('silvicultureMethodGuid')?.disabled).toBeFalse();
    expect(form.get('silvicultureMethodGuid')?.value).toBeNull();
  });

  it('should retain technique and method if both are valid', () => {
    component.silvicultureTechniqueCode = [{ silvicultureBaseGuid: 'base1', silvicultureTechniqueGuid: 'tech1' }];
    component.silvicultureMethodCode = [
      { silvicultureTechniqueGuid: 'tech1', silvicultureMethodGuid: 'method1' }
    ];

    const form = component.createActivityForm({
      silvicultureBaseGuid: 'base1',
      silvicultureTechniqueGuid: 'tech1',
      silvicultureMethodGuid: 'method1'
    });

    component.activityForms.push(form);
    component.onBaseChange('base1', form);

    expect(form.get('silvicultureTechniqueGuid')?.enabled).toBeTrue();
    expect(form.get('silvicultureMethodGuid')?.enabled).toBeTrue();
    expect(form.get('silvicultureTechniqueGuid')?.value).toBe('tech1');
    expect(form.get('silvicultureMethodGuid')?.value).toBe('method1');
  });

  // technique changes
  it('should reset method and disable it if techniqueGuid is empty', () => {
    const form = component.createActivityForm({ silvicultureMethodGuid: 'method1' });
    component.activityForms.push(form);
    form.get('silvicultureMethodGuid')?.enable();

    component.onTechniqueChange('', form);

    expect(form.get('silvicultureMethodGuid')?.value).toBeNull();
    expect(form.get('silvicultureMethodGuid')?.disabled).toBeTrue();
    expect(form.get('filteredMethodCode')?.value).toEqual([]);
  });

  it('should reset method if current method does not belong to selected technique', () => {
    component.silvicultureMethodCode = [
      { silvicultureTechniqueGuid: 'tech1', silvicultureMethodGuid: 'methodA' }
    ];

    const form = component.createActivityForm({ silvicultureMethodGuid: 'invalid-method' });
    component.activityForms.push(form);

    component.onTechniqueChange('tech1', form);

    expect(form.get('filteredMethodCode')?.value).toEqual([
      { silvicultureTechniqueGuid: 'tech1', silvicultureMethodGuid: 'methodA' }
    ]);
    expect(form.get('silvicultureMethodGuid')?.value).toBeNull();
  });

  it('should return the correct FormControl from getControl', () => {
    const form = component.createActivityForm({ activityName: 'Test Activity' });
    component.activityForms.push(form);

    const controlFromMethod = component.getControl(0, 'activityName');
    const controlDirect = form.get('activityName') as FormControl;

    expect(controlFromMethod).toBe(controlDirect);
    expect(controlFromMethod.value).toBe('Test Activity');

    controlFromMethod.setValue('Updated Activity');
    expect(controlDirect.value).toBe('Updated Activity');
  });

  describe('onDeleteActivity blocks when attachments exist', () => {
    beforeEach(() => {
      const form = component.createActivityForm({ activityGuid: 'act-1', activityName: 'A1' });
      component.activityForms.push(form);
      component.activities.push({ activityGuid: 'act-1' });

      mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);

      const filesChildStub = { hasAttachments: true } as any;
      (component as any).attachmentFiles = { toArray: () => [filesChildStub] } as any;

      (mockProjectService as any).deleteActivity = jasmine.createSpy('deleteActivity');
    });

    it('shows attachment-delete failure and does NOT call delete', () => {
      component.onDeleteActivity(0);

      expect(mockDialog.open).toHaveBeenCalled(); // confirmation shown
      expect(mockSnackbarService.open).toHaveBeenCalledWith(
        component.messages.activityWithAttachmentDeleteFailure,
        'OK',
        { duration: 5000, panelClass: 'snackbar-error' }
      );
      expect((mockProjectService as any).deleteActivity).not.toHaveBeenCalled();
    });
  });

  describe('onDeleteActivity proceeds when no attachments', () => {
    beforeEach(() => {
      const form = component.createActivityForm({ activityGuid: 'act-2', activityName: 'A2' });
      component.activityForms = [form];
      component.activities = [{ activityGuid: 'act-2' }];

      component.fiscalGuid = 'fg-1';
      (component as any).projectGuid = 'test-project-guid';

      mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);

      const filesChildStub = { hasAttachments: false } as any;
      (component as any).attachmentFiles = { toArray: () => [filesChildStub] } as any;

      (mockProjectService as any).deleteActivity = jasmine.createSpy('deleteActivity')
        .and.returnValue(of({}));
    });

    it('calls delete service and shows success snackbar', () => {
      spyOn(component, 'getActivities');

      component.onDeleteActivity(0);

      expect((mockProjectService as any).deleteActivity).toHaveBeenCalledWith(
        'test-project-guid', 
        'fg-1',              
        'act-2'
      );
      expect(mockSnackbarService.open).toHaveBeenCalledWith(
        component.messages.activityDeletedSuccess,
        'OK',
        { duration: 5000, panelClass: 'snackbar-success' }
      );
      expect(component.getActivities).toHaveBeenCalled();
    });
  });

  describe('onSaveActivity early exit cases', () => {
    it('should exit early if activity is already being saved', () => {
      component.activities = [{ activityGuid: 'test-guid' }];
      component.activityForms = [component.createActivityForm({ activityGuid: 'test-guid' })];
      component.isActivitySaving[0] = true;
      component.onSaveActivity(0);

      expect(mockProjectService.updateFiscalActivities).not.toHaveBeenCalled();
      expect(component.isActivitySaving[0]).toBeTrue();
    });

    it('should reset isActivitySaving to false and return if form does not exist', () => {
      component.activities = [{}];
      component.activityForms = [];
      component.isActivitySaving[0] = false;

      component.onSaveActivity(0);

      expect(component.isActivitySaving[0]).toBeFalse();
    });
  });

  it('should load all code tables and assign values', fakeAsync(() => {
    const mockData = { _embedded: { dummy: [] } };
    mockCodeTableService.fetchCodeTable.and.returnValue(of(mockData));

    const assignSpy = spyOn(component, 'assignCodeTableData').and.callThrough();

    component.loadCodeTables().subscribe();

    tick(); // flush forkJoin

    expect(mockCodeTableService.fetchCodeTable).toHaveBeenCalledTimes(5);
    expect(assignSpy).toHaveBeenCalledWith('contractPhaseCode', mockData);
    expect(assignSpy).toHaveBeenCalledWith('silvicultureMethodCode', mockData);
  }));

  it('should handle errors when loading code tables', fakeAsync(() => {
    mockCodeTableService.fetchCodeTable.and.returnValue(throwError(() => new Error('Network fail')));

    const assignSpy = spyOn(component, 'assignCodeTableData').and.callThrough();
    const consoleSpy = spyOn(console, 'error');

    component.loadCodeTables().subscribe({
      error: () => {}
    });

    tick();

    expect(consoleSpy).not.toHaveBeenCalledWith('Error fetching');
    expect(assignSpy).not.toHaveBeenCalled();
  }));

  it('should load code tables then fetch activities and toggle reportable', fakeAsync(() => {
    const toggleSpy = spyOn(component, 'toggleResultsReportableInd');
    const getActivitiesSpy = spyOn(component, 'getActivities').and.callFake((cb?: any) => {
      component.activityForms = [
        new FormBuilder().group({ isResultsReportableInd: [true] })
      ];
      if (cb) cb();
    });

    spyOn(component, 'loadCodeTables').and.returnValue(of(void 0));

    component.ngOnChanges({
      fiscalGuid: { currentValue: 'guid', previousValue: '', firstChange: true, isFirstChange: () => true }
    });

    tick();

    expect(component.loadCodeTables).toHaveBeenCalled();
    expect(getActivitiesSpy).toHaveBeenCalled();
    expect(toggleSpy).toHaveBeenCalledWith(0);
  }));

  it('should call getActivities directly when loadCodeTables fails', fakeAsync(() => {
    spyOn(component, 'getActivities');
    spyOn(component, 'loadCodeTables').and.returnValue(throwError(() => new Error('fail')));

    component.ngOnChanges({
      fiscalGuid: { currentValue: 'guid', previousValue: '', firstChange: true, isFirstChange: () => true }
    });

    tick();

    expect(component.getActivities).toHaveBeenCalled();
  }));

  it('should skip ngOnChanges logic if fiscalGuid is missing', () => {
    spyOn(component, 'loadCodeTables');
    spyOn(component, 'getActivities');
    component.ngOnChanges({ fiscalGuid: { currentValue: '', previousValue: '', firstChange: true, isFirstChange: () => true } });
    expect(component.loadCodeTables).not.toHaveBeenCalled();
    expect(component.getActivities).not.toHaveBeenCalled();
  });

});
