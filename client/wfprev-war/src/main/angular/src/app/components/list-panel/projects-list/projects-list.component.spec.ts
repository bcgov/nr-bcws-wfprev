import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
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
import L from 'leaflet';
import { CreateNewProjectDialogComponent } from 'src/app/components/create-new-project-dialog/create-new-project-dialog.component';

describe('ProjectsListComponent', () => {
  let component: ProjectsListComponent;
  let fixture: ComponentFixture<ProjectsListComponent>;
  let debugElement: DebugElement;
  let mockMap: any;
  let mockViewer: any;
  let mockMarkerClusterGroup: any;
  let mockMarker: any;
  let mockPolygon: any;

  const mockProjectList = [
    { 
      projectNumber: 1, 
      projectName: 'Project 1', 
      forestRegionOrgUnitId: 101, 
      totalPlannedProjectSizeHa: 100,
      latitude: 49.2827,
      longitude: -123.1207,
      projectGuid: 'guid1'
    },
    { 
      projectNumber: 2, 
      projectName: 'Project 2', 
      forestRegionOrgUnitId: 102, 
      totalPlannedProjectSizeHa: 200,
      latitude: 49.2849,
      longitude: -123.1217,
      projectGuid: 'guid2'
    },
  ];

  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', ['fetchProjects']);
    mockProjectService.fetchProjects.and.returnValue(of({
      _embedded: {
        project: mockProjectList,
      },
    }));

    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    mockCodeTableService.fetchCodeTable.and.callFake((name: 'programAreaCodes' | 'forestRegionCodes') => {
      const mockData = {
        programAreaCodes: { _embedded: { programArea: [{ programAreaGuid: 'guid1', programAreaName: 'Area 1' }] } },
        forestRegionCodes: { _embedded: { forestRegionCode: [{ orgUnitId: 101, orgUnitName: 'Region 1' }] } },
      };
      return of(mockData[name]);
    });

    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockDialog.open.and.returnValue({
      afterClosed: () => of({ success: true }),
    } as any);

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    // Setup Leaflet mocks
    mockMap = {
      addLayer: jasmine.createSpy('addLayer'),
      on: jasmine.createSpy('on')
    };

    mockViewer = {
      map: mockMap
    };

    mockMarkerClusterGroup = jasmine.createSpyObj('markerClusterGroup', ['addLayer']);
    mockMarker = jasmine.createSpyObj('marker', ['on', 'setIcon', 'getLatLng']);
    mockPolygon = jasmine.createSpyObj('polygon', ['setStyle']);

    spyOn(L, 'markerClusterGroup').and.returnValue(mockMarkerClusterGroup);
    spyOn(L, 'marker').and.returnValue(mockMarker);
    spyOn(L, 'polygon').and.returnValue(mockPolygon);
  
    await TestBed.configureTestingModule({
      imports: [
        ProjectsListComponent,
        BrowserAnimationsModule,
        MatExpansionModule,
        MatSlideToggleModule
      ],
      providers: [
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableService },
        { provide: MatDialog, useValue: mockDialog },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: {} }
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectsListComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    component.getActiveMap = () => ({ $viewer: mockViewer });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the correct number of projects', () => {
    component.projectList = mockProjectList;
    fixture.detectChanges();

    const projectItems = fixture.debugElement.queryAll(By.css('.project-name'));
    expect(projectItems.length).toBe(2);
    expect(projectItems[0].nativeElement.textContent).toContain('Project 1');
    expect(projectItems[1].nativeElement.textContent).toContain('Project 2');
  });

  it('should load code tables on init', () => {
    expect(mockCodeTableService.fetchCodeTable).toHaveBeenCalledWith('programAreaCodes');
    expect(mockCodeTableService.fetchCodeTable).toHaveBeenCalledWith('forestRegionCodes');
  });

  it('should handle errors when loading code tables', () => {
    mockCodeTableService.fetchCodeTable.and.returnValue(throwError(() => new Error('Error fetching data')));
    component.loadCodeTables();
    fixture.detectChanges();

    expect(component.programAreaCode).toEqual([]);
    expect(component.forestRegionCode).toEqual([]);
  });

  it('should handle errors when loading projects', () => {
    mockProjectService.fetchProjects.and.returnValue(throwError(() => new Error('Error fetching projects')));
    component.loadProjects();
    fixture.detectChanges();
    expect(component.projectList).toEqual([]);
  });

  it('should open the dialog to create a new project and reload projects if successful', () => {
    spyOn(component, 'loadProjects');
    
    component.createNewProject();
    
    expect(mockDialog.open).toHaveBeenCalledWith(
      CreateNewProjectDialogComponent,
      {
        width: '1000px',
        disableClose: true,
        hasBackdrop: true,
      }
    );
    expect(component.loadProjects).toHaveBeenCalled();
  });

  it('should return the correct description from code tables', () => {
    component.programAreaCode = [{ programAreaGuid: 'guid1', programAreaName: 'Area 1' }];
    component.forestRegionCode = [{ orgUnitId: 101, orgUnitName: 'Region 1' }];

    const programAreaDescription = component.getDescription('programAreaCode', 'guid1');
    expect(programAreaDescription).toBe('Area 1');

    const regionDescription = component.getDescription('forestRegionCode', 101);
    expect(regionDescription).toBe('Region 1');

    const unknownDescription = component.getDescription('forestRegionCode', 999);
    expect(unknownDescription).toBe('Unknown');
  });

  it('should handle sort change correctly', () => {
    const mockEvent = { target: { value: 'ascending' } };
    component.onSortChange(mockEvent);
    expect(component.selectedSort).toBe('ascending');
  });

  it('should navigate to the edit project route with correct parameters', () => {
    const mockEvent = jasmine.createSpyObj('Event', ['stopPropagation']);
    const project = { projectGuid: 'test-guid' };

    component.editProject(project, mockEvent);

    expect(mockRouter.navigate).toHaveBeenCalledWith(
      [ResourcesRoutes.EDIT_PROJECT],
      { queryParams: { projectGuid: project.projectGuid } }
    );
    expect(mockEvent.stopPropagation).toHaveBeenCalled();
  });

  // Map Function Tests
  describe('loadCoordinatesOnMap', () => {
    beforeEach(() => {
      component.projectList = mockProjectList;
      mockMarker.getLatLng.and.returnValue({ lat: 49.2827, lng: -123.1207 });
    });

    it('should create markers and clusters for valid coordinates', fakeAsync(() => {
      component.loadCoordinatesOnMap();
      tick();

      expect(L.marker).toHaveBeenCalledTimes(2);
      expect(mockMarkerClusterGroup.addLayer).toHaveBeenCalledTimes(2);
      expect(mockMap.addLayer).toHaveBeenCalledWith(mockMarkerClusterGroup);
    }));

    it('should filter out projects with null coordinates', fakeAsync(() => {
      component.projectList = [
        ...mockProjectList,
        { 
          projectNumber: 3,
          projectName: 'Project 3',
          latitude: null,
          longitude: -123.1207
        }
      ];
      
      component.loadCoordinatesOnMap();
      tick();

      expect(L.marker).toHaveBeenCalledTimes(2);
    }));

    it('should handle marker click events correctly', fakeAsync(() => {
      component.loadCoordinatesOnMap();
      tick();

      const clickHandler = mockMarker.on.calls.argsFor(0)[1];
      
      // Test activation
      clickHandler();
      expect(mockMarker.setIcon).toHaveBeenCalled();
      expect(mockPolygon.setStyle).toHaveBeenCalledWith({ weight: 5 });

      // Test deactivation
      clickHandler();
      expect(mockPolygon.setStyle).toHaveBeenCalledWith({ weight: 2 });
    }));

    it('should not create markers when projectList is empty', fakeAsync(() => {
      component.projectList = [];
      component.loadCoordinatesOnMap();
      tick();

      expect(L.marker).not.toHaveBeenCalled();
      expect(mockMarkerClusterGroup.addLayer).not.toHaveBeenCalled();
    }));
  });

  describe('highlightProjectPolygons', () => {
    beforeEach(() => {
      component.markerPolygons = new Map();
      component.markerPolygons.set(mockMarker, [mockPolygon]);
      mockMarker.getLatLng.and.returnValue({ lat: 49.2827, lng: -123.1207 });
    });

    it('should highlight polygons for matching coordinates', () => {
      const project = { latitude: 49.2827, longitude: -123.1207 };
      component.highlightProjectPolygons(project);

      expect(mockMarker.setIcon).toHaveBeenCalled();
      expect(mockPolygon.setStyle).toHaveBeenCalledWith({ weight: 5 });
    });

    it('should reset previously active marker', () => {
      const oldMarker = jasmine.createSpyObj('marker', ['setIcon']);
      component.activeMarker = oldMarker;
      
      const project = { latitude: 49.2827, longitude: -123.1207 };
      component.highlightProjectPolygons(project);

      expect(oldMarker.setIcon).toHaveBeenCalled();
    });

    it('should not highlight when coordinates do not match any marker', () => {
      const project = { latitude: 0, longitude: 0 };
      component.highlightProjectPolygons(project);

      expect(mockPolygon.setStyle).not.toHaveBeenCalled();
    });

    it('should update activeMarker when highlighting new marker', () => {
      const project = { latitude: 49.2827, longitude: -123.1207 };
      component.highlightProjectPolygons(project);

      expect(component.activeMarker).toBe(mockMarker);
    });

    it('should handle null project coordinates', () => {
      const project = { latitude: null, longitude: null };
      component.highlightProjectPolygons(project);

      expect(mockMarker.setIcon).not.toHaveBeenCalled();
      expect(mockPolygon.setStyle).not.toHaveBeenCalled();
    });
  });

  it('should navigate to edit project page if projectGuid is returned after project creation', () => {
    mockDialog.open.and.returnValue({
      afterClosed: () => of({ success: true, projectGuid: 'new-guid' })
    } as any);
  
    component.createNewProject();
  
    expect(mockDialog.open).toHaveBeenCalledWith(
      CreateNewProjectDialogComponent,
      {
        width: '1000px',
        disableClose: true,
        hasBackdrop: true,
      }
    );
  
    expect(mockRouter.navigate).toHaveBeenCalledWith(
      [ResourcesRoutes.EDIT_PROJECT],
      { queryParams: { projectGuid: 'new-guid' } }
    );
  });
  
  it('should reload projects if no projectGuid is returned after project creation', () => {
    spyOn(component, 'loadProjects');
    mockDialog.open.and.returnValue({
      afterClosed: () => of({ success: true })
    } as any);
  
    component.createNewProject();
  
    expect(component.loadProjects).toHaveBeenCalled();
  });
  
});
