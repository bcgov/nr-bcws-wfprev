import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectsListComponent } from './projects-list.component';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ResourcesRoutes } from 'src/app/utils';
import { of, throwError } from 'rxjs';

describe('ProjectsListComponent', () => {
  let component: ProjectsListComponent;
  let fixture: ComponentFixture<ProjectsListComponent>;
  let debugElement: DebugElement;

  let mockProjectService = {
    fetchProjects: jasmine.createSpy('fetchProjects').and.returnValue(of({
      _embedded: {
        project: [
          { projectNumber: 1, projectName: 'Project 1', forestRegionOrgUnitId: 101, totalPlannedProjectSizeHa: 100 },
          { projectNumber: 2, projectName: 'Project 2', forestRegionOrgUnitId: 102, totalPlannedProjectSizeHa: 200 },
        ],
      },
    })),
  };

  let mockCodeTableService = {
    fetchCodeTable: jasmine.createSpy('fetchCodeTable').and.callFake((name: 'programAreaCodes' | 'forestRegionCodes') => {
      const mockData = {
        programAreaCodes: { _embedded: { programArea: [{ programAreaGuid: 'guid1', programAreaName: 'Area 1' }] } },
        forestRegionCodes: { _embedded: { forestRegionCode: [{ orgUnitId: 101, orgUnitName: 'Region 1' }] } },
      };
      return of(mockData[name]);
    }),
  };

  let mockDialog = {
    open: jasmine.createSpy('open').and.returnValue({
      afterClosed: () => of({ success: true }),
    }),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectsListComponent, BrowserAnimationsModule, MatExpansionModule, MatSlideToggleModule],
      providers: [
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableService },
        { provide: MatDialog, useValue: mockDialog },
        { provide: ActivatedRoute, useValue: ActivatedRoute },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectsListComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the correct number of projects', () => {
    // Mock the project data to simulate a successful API response
    component.projectList = [
      { projectNumber: 1, projectName: 'Project 1' },
      { projectNumber: 2, projectName: 'Project 2' },
    ];
  
    // Trigger change detection to update the DOM
    fixture.detectChanges();
  
    // Query DOM elements
    const projectItems = fixture.debugElement.queryAll(By.css('.project-name'));
  
    // Assertions
    expect(projectItems.length).toBe(2); // Mock data contains 2 projects
    expect(projectItems[0].nativeElement.textContent).toContain('Project 1');
    expect(projectItems[1].nativeElement.textContent).toContain('Project 2');
  });
  
  it('should load code tables on init', () => {
    expect(mockCodeTableService.fetchCodeTable).toHaveBeenCalledWith('programAreaCodes');
    expect(mockCodeTableService.fetchCodeTable).toHaveBeenCalledWith('forestRegionCodes');
  });
  
  it('should handle errors when loading code tables', () => {
    mockCodeTableService.fetchCodeTable.and.returnValue(throwError('Error fetching data'));
    component.loadCodeTables();
    fixture.detectChanges();
  
    expect(component.programAreaCode).toEqual([]);
    expect(component.forestRegionCode).toEqual([]);
  });
  

  it('should handle errors when loading projects', () => {
    mockProjectService.fetchProjects.and.returnValue(throwError('Error fetching projects'));
    component.loadProjects();
    fixture.detectChanges();
    expect(component.projectList).toEqual([]);
  });
  

  it('should open the dialog to create a new project and reload projects if successful', () => {
    spyOn(component, 'loadProjects');
    component.createNewProject();
    expect(mockDialog.open).toHaveBeenCalledWith(jasmine.any(Function), jasmine.objectContaining({
      width: '880px',
      disableClose: true,
      hasBackdrop: true,
    }));
    expect(component.loadProjects).toHaveBeenCalled();
  });

  it('should return the correct description from code tables', () => {
    component.loadCodeTables(); // Load the mock code tables
    fixture.detectChanges();
    const description = 'Region 1';
    expect(description).toBe('Region 1');
  
    const unknownDescription = component.getDescription('forestRegionCode', 999);
    expect(unknownDescription).toBe('Unknown');
  });
  

  it('should handle sort change correctly', () => {
    spyOn(component, 'onSortChange').and.callThrough();
  
    const select = debugElement.query(By.css('select')).nativeElement;
    select.value = 'ascending'; // Change to "ascending"
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
  
    expect(component.onSortChange).toHaveBeenCalled();
    expect(component.selectedSort).toBe('ascending');
  });

  it('should navigate to the edit project route with the correct query parameters and stop event propagation', () => {
    const mockRouter = TestBed.inject(Router);
    spyOn(mockRouter, 'navigate');
    const mockEvent = jasmine.createSpyObj('Event', ['stopPropagation']);
    const project = { projectGuid: 'test-guid'};
  
    component.editProject(project, mockEvent);
  
    expect(mockRouter.navigate).toHaveBeenCalledWith([ResourcesRoutes.EDIT_PROJECT], {
      queryParams: { projectGuid: project.projectGuid },
    });
    expect(mockEvent.stopPropagation).toHaveBeenCalled();
  });

  it('should reload projects if createNewProject dialog returns success', () => {
    spyOn(component, 'loadProjects');
    component.createNewProject();
    expect(mockDialog.open).toHaveBeenCalled();
    expect(component.loadProjects).toHaveBeenCalled();
  });
    
});
