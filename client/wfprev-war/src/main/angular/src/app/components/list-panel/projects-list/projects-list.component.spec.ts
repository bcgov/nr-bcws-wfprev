import { DebugElement, ElementRef, QueryList } from '@angular/core';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import L from 'leaflet';
import { of, throwError } from 'rxjs';
import { CreateNewProjectDialogComponent } from 'src/app/components/create-new-project-dialog/create-new-project-dialog.component';
import { FeaturesResponse } from 'src/app/components/models';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { MapService } from 'src/app/services/map.service';
import { ProjectService } from 'src/app/services/project-services';
import { SharedService } from 'src/app/services/shared-service';
import { ResourcesRoutes } from 'src/app/utils';
import { Messages } from 'src/app/utils/constants';
import { ProjectsListComponent } from './projects-list.component';

describe('ProjectsListComponent', () => {
  let component: ProjectsListComponent;
  let fixture: ComponentFixture<ProjectsListComponent>;
  let debugElement: DebugElement;
  let mockMap: any;
  let mockViewer: any;
  let mockMarkerClusterGroup: any;
  let mockMarker: any;
  let mockPolygon: any;

  const mockFetchProjectsResponse = () => {
    mockProjectService.fetchProjects.and.returnValue(of({
      _embedded: {
        project: mockProjectList
      }
    }));
  };

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
  let mockMapService: jasmine.SpyObj<MapService>;
  let mockSharedService: any;

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', ['fetchProjects', 'getFeatures', 'downloadProjects']);
    mockProjectService.fetchProjects.and.returnValue(of({
      _embedded: {
        project: mockProjectList,
      },
    }));
    mockProjectService.getFeatures.and.returnValue(of({ projects: [] }));

    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    mockCodeTableService.fetchCodeTable.and.callFake((name: string) => {
      const mockData: any = {
        programAreaCodes: { _embedded: { programArea: [{ programAreaGuid: 'guid1', programAreaName: 'Area 1' }] } },
        forestRegionCodes: { _embedded: { forestRegionCode: [{ orgUnitId: 101, orgUnitName: 'Region 1' }] } },
        forestDistrictCodes: { _embedded: { forestDistrictCode: [] } },
        bcParksRegionCodes: { _embedded: { bcParksRegionCode: [] } },
        bcParksSectionCodes: { _embedded: { bcParksSectionCode: [] } },
        planFiscalStatusCodes: { _embedded: { planFiscalStatusCode: [] } },
        activityCategoryCodes: { _embedded: { activityCategoryCode: [] } },
        projectTypeCodes: { _embedded: { projectTypeCode: [] } },
        wildfireOrgUnits: { _embedded: { wildfireOrgUnit: [] } },
      };
      return of(mockData[name]);
    });

    mockMapService = jasmine.createSpyObj('MapService', ['destroySMK', 'getMapIndex', 'setMapIndex', 'setContainerId']);

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

    mockSharedService = {
      filters$: of(null),
      selectedProject$: of(null),
      updateFilters: jasmine.createSpy('updateFilters'),
      updateDisplayedProjects: () => { },
      selectProject: () => { },
      triggerMapCommand: () => { },
      _currentFilters: null,
      get currentFilters() { return this._currentFilters; }
    };

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
        { provide: ActivatedRoute, useValue: {} },
        { provide: MapService, useValue: mockMapService },
        { provide: SharedService, useValue: mockSharedService }
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
    component.displayedProjects = mockProjectList;
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
    mockFetchProjectsResponse();
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
    expect(component.displayedProjects).toEqual([]);
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
    expect(unknownDescription).toBe('');
  });

  it('should handle sort change correctly', () => {
    const mockEvent = { value: 'ascending' };
    component.onSortChange(mockEvent);
    expect(component.selectedSort).toBe('ascending');
  });

  it('should navigate to the edit project route with correct parameters', async () => {
    const mockEvent = jasmine.createSpyObj('Event', ['stopPropagation']);
    const project = { projectGuid: 'test-guid' };

    await component.editProject(project, mockEvent);

    expect(mockRouter.navigate).toHaveBeenCalledWith(
      [ResourcesRoutes.EDIT_PROJECT],
      { queryParams: { projectGuid: project.projectGuid } }
    );
    expect(mockEvent.stopPropagation).toHaveBeenCalled();
  });

  // Map Function Tests
  describe('loadCoordinatesOnMap', () => {
    beforeEach(() => {
      component.displayedProjects = mockProjectList;
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
      component.displayedProjects = [
        ...mockProjectList,
        {
          projectNumber: 3,
          projectName: 'Project 3',
          latitude: null,
          longitude: -123.1207,
          projectGuid: 'guid-z'
        }
      ];

      component.loadCoordinatesOnMap();
      tick();

      expect(L.marker).toHaveBeenCalledTimes(2);
    }));

    it('should not create markers when projectList is empty', fakeAsync(() => {
      component.displayedProjects = [];
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

  it('should highlight project polygons', () => {
    const mockMarkerWithLatLng = { getLatLng: () => ({ lat: 1, lng: 2 }), setIcon: jasmine.createSpy('setIcon') };
    const mockPolygon = { setStyle: jasmine.createSpy('setStyle') };
    component.markerPolygons.set(mockMarkerWithLatLng as any, [mockPolygon as any]);
    spyOn(component, 'highlightProjectPolygons').and.callThrough();
    component.highlightProjectPolygons({ latitude: 1, longitude: 2 });
    expect(component.highlightProjectPolygons).toHaveBeenCalledWith({ latitude: 1, longitude: 2 });
  });

  it('should strip suffix from a string', () => {
    const result = component.stripSuffix('Test Forest Region', ' Forest Region');
    expect(result).toBe('Test');
  });

  it('should return original string if stripSuffix does not find suffix', () => {
    const result = component.stripSuffix('Test', 'NotFound');
    expect(result).toBe('Test');
  });

  it('should generate a secure random number', () => {
    const num = component.getSecureRandomNumber();
    expect(typeof num).toBe('number');
  });

  it('should sort projects in descending order', () => {
    const mockProjects = [
      { projectName: 'B' },
      { projectName: 'A' },
    ];

    mockProjectService.getFeatures.and.returnValue(
      of({ projects: mockProjects, totalItems: 2 } as unknown as FeaturesResponse)
    );

    const mockEvent = { value: 'descending' };
    component.onSortChange(mockEvent);

    expect(component.allProjects.length).toBe(2);
    expect(component.allProjects[0].projectName).toBe('B');
  });


  it('should format fiscal activity display correctly', () => {
    expect(component.getFiscalYearDisplay(2023)).toBe('2023/24');
    expect(component.getFiscalYearDisplay(1999)).toBe('1999/00');
    expect(component.getFiscalYearDisplay(undefined)).toBeNull();
    expect(component.getFiscalYearDisplay(null)).toBeNull();
  });

  it('should return plan fiscal status description from code table', () => {
    component.planFiscalStatusCode = [{ planFiscalStatusCode: 'PS1', description: 'Planned' }];
    const result = component.getDescription('planFiscalStatusCode', 'PS1');
    expect(result).toBe('Planned');
  });

  it('should return activity category description from code table', () => {
    component.activityCategoryCode = [{ activityCategoryCode: 'AC1', description: 'Clearing' }];
    const result = component.getDescription('activityCategoryCode', 'AC1');
    expect(result).toBe('Clearing');
  });

  it('should call getDescription for each code table type', () => {
    component.programAreaCode = [{ programAreaGuid: 'guid1', programAreaName: 'Area 1' }];
    component.forestRegionCode = [{ orgUnitId: 101, orgUnitName: 'Region 1' }];
    component.forestDistrictCode = [{ orgUnitId: 201, orgUnitName: 'District 1' }];
    component.bcParksRegionCode = [{ orgUnitId: 301, orgUnitName: 'Parks Region 1' }];
    component.bcParksSectionCode = [{ orgUnitId: 401, orgUnitName: 'Parks Section 1' }];
    component.planFiscalStatusCode = [{ planFiscalStatusCode: 'PS1', description: 'Planned' }];
    component.activityCategoryCode = [{ activityCategoryCode: 'AC1', description: 'Clearing' }];

    expect(component.getDescription('programAreaCode', 'guid1')).toBe('Area 1');
    expect(component.getDescription('forestRegionCode', 101)).toBe('Region 1');
    expect(component.getDescription('forestDistrictCode', 201)).toBe('District 1');
    expect(component.getDescription('bcParksRegionCode', 301)).toBe('Parks Region 1');
    expect(component.getDescription('bcParksSectionCode', 401)).toBe('Parks Section 1');
    expect(component.getDescription('planFiscalStatusCode', 'PS1')).toBe('Planned');
    expect(component.getDescription('activityCategoryCode', 'AC1')).toBe('Clearing');
    expect(component.getDescription('unknownTable', 'x')).toBe('');
  });

  it('should call stripSuffix and return correct values', () => {
    expect(component.stripSuffix('Test Forest Region', ' Forest Region')).toBe('Test');
    expect(component.stripSuffix('Test', 'NotFound')).toBe('Test');
    expect(component.stripSuffix('', 'Suffix')).toBe('');
  });

  it('should call getFiscalYearDisplay and return correct values', () => {
    expect(component.getFiscalYearDisplay(2023)).toBe('2023/24');
    expect(component.getFiscalYearDisplay(1999)).toBe('1999/00');
    expect(component.getFiscalYearDisplay(undefined)).toBeNull();
    expect(component.getFiscalYearDisplay(null)).toBeNull();
  });

  it('should call onSortChange and sort ascending/descending', () => {
    const mockProjects = [
      { projectName: 'B' },
      { projectName: 'A' },
    ];

    mockProjectService.getFeatures.and.returnValue(
      of({ projects: mockProjects, totalItems: 2 } as unknown as FeaturesResponse)
    );

    component.onSortChange({ value: 'ascending' });
    expect(component.allProjects[0].projectName).toBe('B');
    expect(component.allProjects[1].projectName).toBe('A');

    mockProjectService.getFeatures.and.returnValue(
      of({ projects: mockProjects, totalItems: 2 } as unknown as FeaturesResponse)
    );

    component.onSortChange({ value: 'descending' });
    expect(component.allProjects[0].projectName).toBe('B');
    expect(component.allProjects[1].projectName).toBe('A');
  });

  it('should call createNewProject and open dialog', () => {
    spyOn(component, 'loadProjects');
    component.createNewProject();
    expect(mockDialog.open).toHaveBeenCalled();
    expect(component.loadProjects).toHaveBeenCalled();
  });

  it('should call getSecureRandomNumber and return a number', () => {
    const num = component.getSecureRandomNumber();
    expect(typeof num).toBe('number');
    expect(num).toBeGreaterThanOrEqual(0);
    expect(num).toBeLessThan(1);
  });

  it('should process project response and update displayedProjects correctly', () => {
    const mockData = {
      projects: [
        { projectName: 'Z Project' },
        { projectName: 'A Project' }
      ]
    };

    spyOn(component.sharedService, 'updateDisplayedProjects');

    component.processProjectsResponse(mockData);

    expect(component.allProjects.length).toBe(2);
    expect(component.allProjects[0].projectName).toBe('A Project');
    expect(component.displayedProjects.length).toBe(2);
    expect(component.displayedProjects[0].projectName).toBe('A Project');
    expect(component.isLoading).toBeFalse();
    expect(component.sharedService.updateDisplayedProjects).toHaveBeenCalledWith(component.displayedProjects);
  });

  it('should handle project error and clear project lists', () => {
    const mockError = new Error('Network error');

    spyOn(console, 'error');

    component.handleProjectError(mockError);

    expect(console.error).toHaveBeenCalledWith('Error fetching features:', mockError);
    expect(component.allProjects).toEqual([]);
    expect(component.displayedProjects).toEqual([]);
    expect(component.isLoading).toBeFalse();
  });


  it('should return null fiscal activity  range for missing data', () => {
    const result = component.getProjectFiscalYearRange(null);
    expect(result).toBeNull();
  });

  it('should return correct single fiscal activity  display', () => {
    const result = component.getProjectFiscalYearRange({
      projectFiscals: [{ fiscalYear: 2021 }]
    });
    expect(result).toBe('2021/22');
  });

  it('should return correct fiscal activity  range display', () => {
    const result = component.getProjectFiscalYearRange({
      projectFiscals: [{ fiscalYear: 2019 }, { fiscalYear: 2021 }]
    });
    expect(result).toBe('2019/20 - 2021/22');
  });

  it('should handle empty fiscal activity array', () => {
    const result = component.getProjectFiscalYearRange({ projectFiscals: [] });
    expect(result).toBeNull();
  });

  it('should handle invalid year types gracefully', () => {
    const result = component.getProjectFiscalYearRange({
      projectFiscals: [{ fiscalYear: 'abc' }]
    });
    expect(result).toBeNull();
  });

  it('should respond to filters$ updates and update project list accordingly', () => {
    mockFetchProjectsResponse();
    const mockFilters = { searchText: 'fuel' };
    const mockProjects = [
      {
        projectGuid: 'guid-z',
        projectName: 'Alpha',
        projectNumber: 1,
        bcParksRegionOrgUnitId: 10,
        bcParksSectionOrgUnitId: 11,
        closestCommunityName: 'Community A',
        fireCentreOrgUnitId: 12,
        isMultiFiscalYearProj: false,
        programAreaGuid: 'abc123',
        projectDescription: 'Test project',
        projectLead: 'John Doe',
        projectLeadEmailAddress: 'john@example.com',
        siteUnitName: 'Site 1',
        totalActualAmount: 100,
        totalAllocatedAmount: 120,
        totalFundingRequestAmount: 20,
        totalPlannedCostPerHectare: 10,
        totalPlannedProjectSizeHa: 5,
        forestDistrictOrgUnitId: 13,
        forestRegionOrgUnitId: 14
      }
    ];

    component.sharedService.filters$ = of(mockFilters);
    mockProjectService.getFeatures.and.returnValue(of({ projects: mockProjects }));

    spyOn(component.sharedService, 'updateDisplayedProjects');

    component.ngOnInit();

    expect(component.isLoading).toBeFalse();
    expect(component.allProjects.length).toBe(1);
    expect(component.displayedProjects.length).toBe(1);
    expect(component.allProjects[0].projectName).toBe('Alpha');
    expect(component.sharedService.updateDisplayedProjects).toHaveBeenCalledWith(component.displayedProjects);
  });

  it('should load and process projects on loadProjects()', () => {
    const mockProjects = [
      {
        projectGuid: 'guid-z',
        projectName: 'Z Project',
        bcParksRegionOrgUnitId: 1,
        bcParksSectionOrgUnitId: 2,
        closestCommunityName: 'Town A',
        fireCentreOrgUnitId: 3,
        isMultiFiscalYearProj: false,
        programAreaGuid: 'guid1',
        projectDescription: 'Desc Z',
        projectLead: 'Alice',
        projectLeadEmailAddress: 'alice@example.com',
        projectNumber: 100,
        siteUnitName: 'Unit Z',
        totalActualAmount: 1000,
        totalAllocatedAmount: 800,
        totalFundingRequestAmount: 200,
        totalPlannedCostPerHectare: 20,
        totalPlannedProjectSizeHa: 50,
        forestDistrictOrgUnitId: 4,
        forestRegionOrgUnitId: 5
      },
      {
        projectGuid: 'guid-z2',
        projectName: 'A Project',
        bcParksRegionOrgUnitId: 1,
        bcParksSectionOrgUnitId: 2,
        closestCommunityName: 'Town B',
        fireCentreOrgUnitId: 3,
        isMultiFiscalYearProj: false,
        programAreaGuid: 'guid2',
        projectDescription: 'Desc A',
        projectLead: 'Bob',
        projectLeadEmailAddress: 'bob@example.com',
        projectNumber: 101,
        siteUnitName: 'Unit A',
        totalActualAmount: 900,
        totalAllocatedAmount: 700,
        totalFundingRequestAmount: 250,
        totalPlannedCostPerHectare: 30,
        totalPlannedProjectSizeHa: 60,
        forestDistrictOrgUnitId: 6,
        forestRegionOrgUnitId: 7
      }
    ];

    const mockResponse = {
      projects: mockProjects
    } as FeaturesResponse;

    mockProjectService.getFeatures.and.returnValue(of(mockResponse));
    spyOn(component.sharedService, 'updateDisplayedProjects');

    component.loadProjects();

    expect(component.isLoading).toBeFalse();
    expect(component.allProjects.length).toBe(2);
    expect(component.allProjects[0].projectName).toBe('A Project');
    expect(component.displayedProjects.length).toBe(2);
    expect(component.sharedService.updateDisplayedProjects).toHaveBeenCalledWith(component.displayedProjects);
  });

  it('should handle error in loadProjects()', () => {
    const error = new Error('Network failure');
    spyOn(console, 'error');
    mockProjectService.getFeatures.and.returnValue(throwError(() => error));

    component.loadProjects();

    expect(console.error).toHaveBeenCalledWith('Error fetching features:', error);
    expect(component.allProjects).toEqual([]);
    expect(component.displayedProjects).toEqual([]);
    expect(component.isLoading).toBeFalse();
  });

  it('should return projectFiscals sorted in descending fiscalYear order', () => {
    const project = {
      projectFiscals: [
        { fiscalYear: 2021 },
        { fiscalYear: 2023 },
        { fiscalYear: 2022 }
      ]
    };

    const result = component.getSortedProjectFiscalsDesc(project);

    expect(result.length).toBe(3);
    expect(result[0].fiscalYear).toBe(2023);
    expect(result[1].fiscalYear).toBe(2022);
    expect(result[2].fiscalYear).toBe(2021);
  });

  it('should return empty array if projectFiscals is missing or empty', () => {
    expect(component.getSortedProjectFiscalsDesc(undefined)).toEqual([]);
    expect(component.getSortedProjectFiscalsDesc({})).toEqual([]);
    expect(component.getSortedProjectFiscalsDesc({ projectFiscals: [] })).toEqual([]);
  });


  it('should toggle marker active state and update polygon styles on marker click', fakeAsync(() => {
    const mockPolygon1 = jasmine.createSpyObj('polygon', ['setStyle']);
    const mockPolygon2 = jasmine.createSpyObj('polygon', ['setStyle']);
    const mockClickedMarker = jasmine.createSpyObj('marker', ['setIcon', 'on', 'getLatLng']);

    mockClickedMarker.getLatLng.and.returnValue({ lat: 49.2827, lng: -123.1207 });

    (L.marker as jasmine.Spy).and.returnValue(mockClickedMarker);

    (L.polygon as jasmine.Spy).and.callFake(() => {
      return [mockPolygon1, mockPolygon2][(L.polygon as jasmine.Spy).calls.count() - 1] || mockPolygon1;
    });

    component.displayedProjects = [{
      latitude: 49.2827,
      longitude: -123.1207,
      projectName: 'Test Project',
      projectGuid: 'guid-z'
    }];

    component.loadCoordinatesOnMap();
    tick();

    const clickHandler = (mockClickedMarker.on as jasmine.Spy).calls.mostRecent().args[1];
    component.markerPolygons.set(mockClickedMarker, [mockPolygon1, mockPolygon2]);

    clickHandler();

    expect(mockClickedMarker.setIcon).toHaveBeenCalledWith(jasmine.objectContaining({
      options: jasmine.objectContaining({ iconUrl: '/assets/active-pin-drop.svg' })
    }));
    expect(mockPolygon1.setStyle).toHaveBeenCalledWith({ weight: 5 });
    expect(mockPolygon2.setStyle).toHaveBeenCalledWith({ weight: 5 });

    clickHandler();

    expect(mockClickedMarker.setIcon).toHaveBeenCalledWith(jasmine.objectContaining({
      options: jasmine.objectContaining({ iconUrl: '/assets/blue-pin-drop.svg' })
    }));
    expect(mockPolygon1.setStyle).toHaveBeenCalledWith({ weight: 2 });
    expect(mockPolygon2.setStyle).toHaveBeenCalledWith({ weight: 2 });
  }));

  it('should reset active markers and polygon styles on map click', fakeAsync(() => {
    const mockPolygon = jasmine.createSpyObj('polygon', ['setStyle']);
    const mockActiveMarker = jasmine.createSpyObj('marker', ['setIcon', 'getLatLng']);
    mockActiveMarker.getLatLng.and.returnValue({ lat: 49.2827, lng: -123.1207 });

    const markerStates = new Map<L.Marker, boolean>();
    markerStates.set(mockActiveMarker, true);

    const createMarkerIconSpy = jasmine.createSpy().and.returnValue('reset-icon');
    (component as any).getActiveMap = () => ({
      $viewer: {
        map: {
          on: (event: string, cb: Function) => {
            if (event === 'click') {
              cb(); // simulate map click
            }
          },
          addLayer: () => { }
        }
      }
    });

    component.markerPolygons.set(mockActiveMarker, [mockPolygon]);

    const oldLoad = (component as any).loadCoordinatesOnMap;
    (component as any).loadCoordinatesOnMap = function () {
      const map = this.getActiveMap();
      const internalMarkerStates = markerStates;

      map.$viewer.map.on('click', () => {
        internalMarkerStates.forEach((isActive, marker) => {
          if (isActive) {
            marker.setIcon(L.icon({
              iconUrl: '/assets/blue-pin-drop.svg',
              iconSize: [30, 50],
              iconAnchor: [12, 41],
              popupAnchor: [1, -34],
            }));
            internalMarkerStates.set(marker, false);
            const associatedPolygons = this.markerPolygons.get(marker);
            associatedPolygons?.forEach((polygon: any) => polygon.setStyle({ weight: 2 }));
          }
        });
      });
    };

    component.loadCoordinatesOnMap();
    tick();

    expect(mockActiveMarker.setIcon).toHaveBeenCalledWith(jasmine.objectContaining({
      options: jasmine.objectContaining({
        iconUrl: '/assets/blue-pin-drop.svg'
      })
    }));

    expect(mockPolygon.setStyle).toHaveBeenCalledWith({ weight: 2 });
  }));

  describe('onHeaderClick', () => {
    it('should call onListItemClick if not clicking inside .custom-indicator', () => {
      const mockProject = { projectGuid: 'guid-x' };
      const event = {
        target: document.createElement('div'),
        stopPropagation: jasmine.createSpy()
      } as unknown as MouseEvent;

      spyOn(component, 'onListItemClick');

      component.onHeaderClick(event, mockProject);

      expect(event.stopPropagation).toHaveBeenCalled();
      expect(component.onListItemClick).toHaveBeenCalledWith(mockProject);
    });

    it('should not call onListItemClick if clicking inside .custom-indicator', () => {
      const customIndicator = document.createElement('div');
      customIndicator.classList.add('custom-indicator');

      const event = {
        target: customIndicator,
        stopPropagation: jasmine.createSpy()
      } as unknown as MouseEvent;

      spyOn(component, 'onListItemClick');

      component.onHeaderClick(event, {});

      expect(event.stopPropagation).not.toHaveBeenCalled();
      expect(component.onListItemClick).not.toHaveBeenCalled();
    });
  });

  describe('onListItemClick', () => {
    const project = {
      projectGuid: 'guid-y',
      bcParksRegionOrgUnitId: 1,
      bcParksSectionOrgUnitId: 2,
      closestCommunityName: 'Town A',
      fireCentreOrgUnitId: 3,
      isMultiFiscalYearProj: false,
      programAreaGuid: 'guid1',
      projectDescription: 'Some description',
      projectLead: 'John Doe',
      projectLeadEmailAddress: 'john@example.com',
      projectName: 'Test Project',
      projectNumber: 123,
      siteUnitName: 'Unit 1',
      totalActualAmount: 100,
      totalAllocatedAmount: 150,
      totalFundingRequestAmount: 50,
      totalPlannedCostPerHectare: 5,
      totalPlannedProjectSizeHa: 10,
      forestDistrictOrgUnitId: 4,
      forestRegionOrgUnitId: 5,
      projectFiscals: [],
      latitude: 49.2827,
      longitude: -123.1207
    };


    beforeEach(() => {
      spyOn(component.sharedService, 'selectProject');
      spyOn(component.sharedService, 'triggerMapCommand');
    });

    it('should deselect the project if it is already selected', () => {
      component.selectedProjectGuid = 'guid-y';

      component.onListItemClick(project);

      expect(component.selectedProjectGuid).toBeNull();
      expect(component.sharedService.selectProject).toHaveBeenCalledWith();
      expect(component.sharedService.triggerMapCommand).toHaveBeenCalledWith('close', project);
    });

    it('should select the project if it is not selected', () => {
      component.selectedProjectGuid = 'guid-z';

      component.onListItemClick(project);

      expect(component.selectedProjectGuid).toBe('guid-y');
      expect(component.sharedService.selectProject).toHaveBeenCalledWith(project);
      expect(component.sharedService.triggerMapCommand).not.toHaveBeenCalled();
    });
  });

  describe('downloadProjects', () => {
    let mockSnackBar: jasmine.SpyObj<any>;

    beforeEach(() => {
      mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
      mockSnackBar.open.and.returnValue({ dismiss: jasmine.createSpy('dismiss') });
      (component as any).snackbarService = mockSnackBar;
    });

    it('should download projects successfully when filters are applied', fakeAsync(() => {
      mockSharedService._currentFilters = { someFilter: 'value' };
      // displayedProjects state is irrelevant for the current implementation, but we set it to empty for clarity
      component.displayedProjects = [];

      const mockBlob = new Blob(['test data'], { type: 'text/csv' });
      spyOn(window.URL, 'createObjectURL').and.returnValue('blob:url');
      spyOn(document, 'createElement').and.callThrough();
      mockProjectService.downloadProjects.and.returnValue(of(mockBlob));

      component.onDownload('csv');
      tick();

      const bodyArg = mockProjectService.downloadProjects.calls.mostRecent().args[0] as any;
      expect(bodyArg.projects).toBeUndefined();
      expect(bodyArg.projectFilter).toEqual({ someFilter: 'value' });
    }));

    it('should show error message when attempting to download without filters', fakeAsync(() => {
      mockSharedService._currentFilters = null;
      component.displayedProjects = [{ projectGuid: 'guid1' }] as any; // Presence of projects irrelevant

      component.onDownload('csv');
      tick();

      expect(mockProjectService.downloadProjects).not.toHaveBeenCalled();
      expect(mockSnackBar.open).toHaveBeenCalledWith(Messages.fileDownloadRequiresFilter, 'Close', jasmine.any(Object));
    }));

    it('should show failure message when download service fails', fakeAsync(() => {
      mockSharedService._currentFilters = { someFilter: 'value' };
      component.displayedProjects = [] as any;

      mockProjectService.downloadProjects.and.returnValue(
        throwError(() => new Error('Download failed'))
      );

      component.onDownload('csv');
      tick();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        Messages.fileDownloadInProgress,
        'Close',
        jasmine.any(Object)
      );
      expect(mockSnackBar.open).toHaveBeenCalledWith(
        Messages.fileDownloadFailure,
        'Close',
        jasmine.any(Object)
      );
    }));
  });

  describe('getDisplayedFiscalYears', () => {
    beforeEach(() => {
      component.resultCount = 2;
    });

    it('returns the top N fiscal years in descending order (numbers only)', () => {
      const project = {
        projectFiscals: [
          { fiscalYear: 2021 },
          { fiscalYear: 2023 },
          { fiscalYear: 2022 },
          { fiscalYear: 'not-a-number' },
          { fiscalYear: null },
        ],
      };

      const years = component.getDisplayedFiscalYears(project);
      expect(years).toEqual([2023, 2022]);
    });

    it('returns empty array when there are no valid fiscal years', () => {
      const project = {
        projectFiscals: [{ fiscalYear: undefined }, { fiscalYear: 'x' }],
      };
      expect(component.getDisplayedFiscalYears(project)).toEqual([]);
    });

    it('handles missing projectFiscals gracefully', () => {
      expect(component.getDisplayedFiscalYears({} as any)).toEqual([]);
      expect(component.getDisplayedFiscalYears(null as any)).toEqual([]);
    });
  });

  describe('getDisplayedProjectFiscalGuids', () => {
    beforeEach(() => {
      component.resultCount = 2;
    });

    it('returns guids only for displayed years and only when guid exists', () => {
      const project = {
        projectFiscals: [
          { fiscalYear: 2023, projectPlanFiscalGuid: 'pf-a' }, // included (top 2)
          { fiscalYear: 2022, projectPlanFiscalGuid: 'pf-b' }, // included (top 2)
          { fiscalYear: 2021, projectPlanFiscalGuid: 'pf-c' }, // NOT included (not in top 2)
          { fiscalYear: 2020 },                                // no guid -> ignored
        ],
      };

      const guids = component.getDisplayedProjectFiscalGuids(project);
      expect(guids).toEqual(['pf-a', 'pf-b']);
    });

    it('returns empty array when there are no matching displayed years', () => {
      component.resultCount = 0; // show 0 years
      const project = {
        projectFiscals: [
          { fiscalYear: 2023, projectPlanFiscalGuid: 'pf-a' },
        ],
      };
      expect(component.getDisplayedProjectFiscalGuids(project)).toEqual([]);
    });

    it('handles missing projectFiscals', () => {
      expect(component.getDisplayedProjectFiscalGuids({} as any)).toEqual([]);
      expect(component.getDisplayedProjectFiscalGuids(null as any)).toEqual([]);
    });
  });

  describe('buildProjectsPayloadFromDisplayed', () => {
    beforeEach(() => {
      component.resultCount = 3;
    });

    it('coalesces by projectGuid and unions displayed fiscal guids', () => {
      component.displayedProjects = [
        {
          projectGuid: 'guid1',
          projectFiscals: [
            { fiscalYear: 2023, projectPlanFiscalGuid: 'pf-1a' },
            { fiscalYear: 2022, projectPlanFiscalGuid: 'pf-1b' },
          ],
        },
        {
          projectGuid: 'guid1',
          projectFiscals: [
            { fiscalYear: 2022, projectPlanFiscalGuid: 'pf-1b' },
            { fiscalYear: 2021, projectPlanFiscalGuid: 'pf-1c' },
          ],
        },
        {
          projectGuid: 'guid2',
          projectFiscals: [],
        },
        {
          projectFiscals: [{ fiscalYear: 2024, projectPlanFiscalGuid: 'pf-x' }],
        },
      ] as any;

      const payload = component.buildProjectsPayloadFromDisplayed();

      expect(payload).toEqual(jasmine.arrayContaining([
        {
          projectGuid: 'guid1',
          projectFiscalGuids: jasmine.arrayContaining(['pf-1a', 'pf-1b', 'pf-1c']),
        },
        { projectGuid: 'guid2' },
      ]));

      const guid1 = payload.find(p => p.projectGuid === 'guid1')!;
      const unique = new Set(guid1.projectFiscalGuids);
      expect(unique.size).toBe(guid1.projectFiscalGuids!.length);
    });

    it('omits projectFiscalGuids key when none are displayed for that project', () => {
      component.displayedProjects = [
        { projectGuid: 'g1', projectFiscals: [] },
        { projectGuid: 'g2' },
      ] as any;

      const payload = component.buildProjectsPayloadFromDisplayed();

      expect(payload).toEqual(jasmine.arrayContaining([
        { projectGuid: 'g1' },
        { projectGuid: 'g2' },
      ]));

      expect((payload.find(p => p.projectGuid === 'g1') as any).projectFiscalGuids).toBeUndefined();
      expect((payload.find(p => p.projectGuid === 'g2') as any).projectFiscalGuids).toBeUndefined();
    });

    it('respects resultCount when deciding which fiscals are included', () => {
      component.resultCount = 1;
      component.displayedProjects = [
        {
          projectGuid: 'g1',
          projectFiscals: [
            { fiscalYear: 2023, projectPlanFiscalGuid: 'top' },
            { fiscalYear: 2022, projectPlanFiscalGuid: 'lower' },
          ],
        },
      ] as any;

      const payload = component.buildProjectsPayloadFromDisplayed();
      expect(payload).toEqual([
        { projectGuid: 'g1', projectFiscalGuids: ['top'] },
      ]);
    });
  });

  it('should call loadProjects on scroll near bottom', () => {
    spyOn(component, 'loadProjects');
    component.isLoading = false;
    component.hasMore = true;

    const scrollable = {
      scrollHeight: 1000,
      scrollTop: 960,
      clientHeight: 50
    } as any;

    const event = { target: scrollable } as Partial<Event> as Event;
    component.onScroll(event);
    expect(component.loadProjects).toHaveBeenCalledWith(false);
  });

  it('should clear selected project when selectedProject$ emits null', fakeAsync(() => {
    const mockEl = {
      nativeElement: {
        classList: { remove: jasmine.createSpy('remove') }
      }
    };

    const queryList = new QueryList<ElementRef>();
    queryList.reset([mockEl as unknown as ElementRef]);
    component.panelElements = queryList;

    component.selectedProjectGuid = 'old-guid';

    const detectChangesSpy = spyOn((component as any).cdr, 'detectChanges');

    component.sharedService.selectedProject$ = of(null);

    component.ngOnInit();
    tick();

    expect(component.selectedProjectGuid).toBeNull();
    expect(detectChangesSpy).toHaveBeenCalled();
    expect(mockEl.nativeElement.classList.remove).toHaveBeenCalledWith('selected-project');
  }));

  it('should update selected project and scroll element into view when selectedProject$ emits a project', fakeAsync(() => {
    const mockProject = { projectGuid: 'guid-123', projectName: 'Test Project' };

    const addSpy = spyOn(component as any, 'addProjectToDisplayedList');
    const detectChangesSpy = spyOn((component as any).cdr, 'detectChanges');

    const scrollSpy = jasmine.createSpy('scrollIntoView');
    const addClassSpy = jasmine.createSpy('add');

    const mockEl = {
      nativeElement: {
        dataset: { guid: 'guid-123' },
        scrollIntoView: scrollSpy,
        classList: { add: addClassSpy },
      },
    };
    const queryList = new QueryList<ElementRef>();
    queryList.reset([mockEl as unknown as ElementRef]);
    component.panelElements = queryList;

    component.displayedProjects = [];

    component.sharedService.selectedProject$ = of(mockProject);

    component.ngOnInit();
    tick();
    tick();

    expect(component.selectedProjectGuid).toBe('guid-123');
    expect(addSpy).toHaveBeenCalledWith(mockProject);
    expect(detectChangesSpy).toHaveBeenCalled();
    expect(scrollSpy).toHaveBeenCalledWith({ behavior: 'smooth', block: 'center' });
    expect(addClassSpy).toHaveBeenCalledWith('selected-project');
  }));

  describe('addProjectToDisplayedList', () => {
    let detectChangesSpy: jasmine.Spy;
    let updateDisplayedProjectsSpy: jasmine.Spy;

    beforeEach(() => {
      detectChangesSpy = spyOn((component as any).cdr, 'detectChanges');
      updateDisplayedProjectsSpy = spyOn(component.sharedService, 'updateDisplayedProjects');
    });

    it('should add a new project when not already present', () => {
      component.displayedProjects = [
        { projectGuid: 'existing', projectName: 'A Project' }
      ];
      const newProject = { projectGuid: 'new', projectName: 'B Project' };

      (component as any).addProjectToDisplayedList(newProject);

      expect(component.displayedProjects.length).toBe(2);
      expect(component.displayedProjects.some(p => p.projectGuid === 'new')).toBeTrue();
      expect(updateDisplayedProjectsSpy).toHaveBeenCalledWith(component.displayedProjects);
      expect(component.totalItems).toBe(2);
      expect(detectChangesSpy).toHaveBeenCalled();
    });

    it('should not add if project already exists', () => {
      component.displayedProjects = [{ projectGuid: 'dup', projectName: 'Same Project' }];
      const newProject = { projectGuid: 'dup', projectName: 'Same Project' };

      (component as any).addProjectToDisplayedList(newProject);

      expect(component.displayedProjects.length).toBe(1);
      expect(updateDisplayedProjectsSpy).not.toHaveBeenCalled();
    });

    it('should sort ascending when selectedSort is "ascending"', () => {
      component.selectedSort = 'ascending';
      component.displayedProjects = [
        { projectGuid: '1', projectName: 'Z Project' }
      ];
      const newProject = { projectGuid: '2', projectName: 'A Project' };

      (component as any).addProjectToDisplayedList(newProject);

      expect(component.displayedProjects[0].projectName).toBe('A Project');
    });

    it('should sort descending when selectedSort is "descending"', () => {
      component.selectedSort = 'descending';
      component.displayedProjects = [
        { projectGuid: '1', projectName: 'A Project' }
      ];
      const newProject = { projectGuid: '2', projectName: 'Z Project' };

      (component as any).addProjectToDisplayedList(newProject);

      expect(component.displayedProjects[0].projectName).toBe('Z Project');
    });

    it('should not sort if selectedSort is empty', () => {
      component.selectedSort = '';
      component.displayedProjects = [
        { projectGuid: '1', projectName: 'B Project' }
      ];
      const newProject = { projectGuid: '2', projectName: 'A Project' };

      (component as any).addProjectToDisplayedList(newProject);

      // Should just append, not reorder
      expect(component.displayedProjects[0].projectName).toBe('B Project');
      expect(component.displayedProjects[1].projectName).toBe('A Project');
    });
  });

});
