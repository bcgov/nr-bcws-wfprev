import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ProjectFiscalsComponent } from './project-fiscals.component';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatTabsModule } from '@angular/material/tabs';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatMenuModule } from '@angular/material/menu';

describe('ProjectFiscalsComponent', () => {
  let component: ProjectFiscalsComponent;
  let fixture: ComponentFixture<ProjectFiscalsComponent>;
  let projectService: jasmine.SpyObj<ProjectService>;
  let codeTableService: jasmine.SpyObj<CodeTableServices>;
  let snackbarService: jasmine.SpyObj<MatSnackBar>;
  let route: ActivatedRoute;

  beforeEach(async () => {
    // Mock services
    const projectServiceSpy = jasmine.createSpyObj('ProjectService', ['getProjectFiscalsByProjectGuid', 'updateProjectFiscal', 'createProjectFiscal']);
    const codeTableServiceSpy = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    const snackbarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        MatTabsModule,
        MatExpansionModule,
        MatButtonModule,
        MatSlideToggleModule,
        MatMenuModule
      ],
      declarations: [ProjectFiscalsComponent],
      providers: [
        FormBuilder,
        { provide: ProjectService, useValue: projectServiceSpy },
        { provide: CodeTableServices, useValue: codeTableServiceSpy },
        { provide: MatSnackBar, useValue: snackbarSpy },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => 'test-guid' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectFiscalsComponent);
    component = fixture.componentInstance;
    projectService = TestBed.inject(ProjectService) as jasmine.SpyObj<ProjectService>;
    codeTableService = TestBed.inject(CodeTableServices) as jasmine.SpyObj<CodeTableServices>;
    snackbarService = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;

    projectService.getProjectFiscalsByProjectGuid.and.returnValue(of({ _embedded: { projectFiscals: [] } }));
    codeTableService.fetchCodeTable.and.returnValue(of({ _embedded: {} }));
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component and load data', fakeAsync(() => {
    spyOn(component, 'loadCodeTables').and.callThrough();
    spyOn(component, 'generateFiscalYears').and.callThrough();
    spyOn(component, 'loadProjectFiscals').and.callThrough();

    component.ngOnInit();
    tick(); // Simulates async operation

    expect(component.loadCodeTables).toHaveBeenCalled();
    expect(component.generateFiscalYears).toHaveBeenCalled();
    expect(component.loadProjectFiscals).toHaveBeenCalled();
  }));

  it('should generate fiscal years correctly', () => {
    component.generateFiscalYears();
    expect(component.fiscalYears.length).toBe(11); // 5 past + current + 5 future
  });

  it('should load project fiscals and create forms', fakeAsync(() => {
    const mockFiscals = {
      _embedded: {
        projectFiscals: [
          { fiscalYear: 2023, projectFiscalName: 'Test Fiscal 1', projectPlanFiscalGuid: 'guid-1' },
          { fiscalYear: 2024, projectFiscalName: 'Test Fiscal 2', projectPlanFiscalGuid: 'guid-2' }
        ]
      }
    };

    projectService.getProjectFiscalsByProjectGuid.and.returnValue(of(mockFiscals));
    component.loadProjectFiscals();
    tick();

    expect(component.projectFiscals.length).toBe(2);
    expect(component.fiscalForms.length).toBe(2);
    expect(component.fiscalForms[0].value.projectFiscalName).toBe('Test Fiscal 1');
  }));

  it('should create a new fiscal and add to the list', () => {
    component.addNewFiscal();
    expect(component.projectFiscals.length).toBe(1);
    expect(component.fiscalForms.length).toBe(1);
  });

  it('should call updateProjectFiscal when saving an existing fiscal', fakeAsync(() => {
    const mockFiscal = {
      projectPlanFiscalGuid: 'guid-1',
      projectFiscalName: 'Updated Fiscal'
    };
    component.projectFiscals.push(mockFiscal);
    component.fiscalForms.push(component.createFiscalForm(mockFiscal));

    projectService.updateProjectFiscal.and.returnValue(of({}));

    component.onSaveFiscal(0);
    tick();

    expect(projectService.updateProjectFiscal).toHaveBeenCalled();
    expect(snackbarService.open).toHaveBeenCalledWith(jasmine.stringMatching('success'), 'OK', { duration: 5000, panelClass: 'snackbar-success' });
  }));

  it('should call createProjectFiscal when saving a new fiscal', fakeAsync(() => {
    const mockFiscal = {
      projectFiscalName: 'New Fiscal',
      projectGuid: 'test-guid'
    };
    component.projectFiscals.push(mockFiscal);
    component.fiscalForms.push(component.createFiscalForm(mockFiscal));

    projectService.createProjectFiscal.and.returnValue(of({}));

    component.onSaveFiscal(0);
    tick();

    expect(projectService.createProjectFiscal).toHaveBeenCalled();
    expect(snackbarService.open).toHaveBeenCalledWith(jasmine.stringMatching('success'), 'OK', { duration: 5000, panelClass: 'snackbar-success' });
  }));

  it('should handle error when updating a fiscal', fakeAsync(() => {
    projectService.updateProjectFiscal.and.returnValue(throwError(() => new Error('Update Failed')));

    const mockFiscal = { projectPlanFiscalGuid: 'guid-1', projectFiscalName: 'Updated Fiscal' };
    component.projectFiscals.push(mockFiscal);
    component.fiscalForms.push(component.createFiscalForm(mockFiscal));

    component.onSaveFiscal(0);
    tick();

    expect(snackbarService.open).toHaveBeenCalledWith(jasmine.stringMatching('failure'), 'OK', { duration: 5000, panelClass: 'snackbar-error' });
  }));

  it('should handle error when creating a new fiscal', fakeAsync(() => {
    projectService.createProjectFiscal.and.returnValue(throwError(() => new Error('Creation Failed')));

    const mockFiscal = { projectFiscalName: 'New Fiscal', projectGuid: 'test-guid' };
    component.projectFiscals.push(mockFiscal);
    component.fiscalForms.push(component.createFiscalForm(mockFiscal));

    component.onSaveFiscal(0);
    tick();

    expect(snackbarService.open).toHaveBeenCalledWith(jasmine.stringMatching('failure'), 'OK', { duration: 5000, panelClass: 'snackbar-error' });
  }));
});
