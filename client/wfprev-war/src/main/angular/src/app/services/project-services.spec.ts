import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import { EvaluationCriteriaSummaryModel, FeaturesResponse, Project, ProjectBoundary, ProjectFiscal, ReportRequest } from 'src/app/components/models';
import { ProjectService } from 'src/app/services/project-services';
import { HttpEventType } from '@angular/common/http';
import { of } from 'rxjs';
import { Position } from 'geojson';

describe('ProjectService', () => {
  let service: ProjectService;
  let httpMock: HttpTestingController;
  let mockAppConfigService: jasmine.SpyObj<AppConfigService>;
  let mockTokenService: jasmine.SpyObj<TokenService>;

  const mockConfig = {
    rest: {
      wfprev: 'http://mock-api.com',
      wfdm: 'http://mock-wfdm-api.com'
    },
    application: {
      lazyAuthenticate: true,
      enableLocalStorageToken: true,
      localStorageTokenKey: 'oauth',
      allowLocalExpiredToken: false,
      baseUrl: 'http://mock-base-url.com',
      acronym: 'TEST',
      version: '1.0.0',
      environment: 'test',
      remiPlannerEmailAddress: 'test@example.com'
    },
    webade: {
      oauth2Url: 'http://mock-oauth-url.com',
      clientId: 'mock-client-id',
      authScopes: 'mock-scope',
      checkTokenUrl: 'http://mock-check-token-url.com',
      enableCheckToken: false,
    },
    mapServices: {
      geoserverApiBaseUrl: 'http://geoserver.test',
      wfnewsApiBaseUrl: 'http://wfnews.test',
      wfnewsApiKey: 'fake-api-key'
    }
  };

  const mockProjectBoundary: ProjectBoundary = {
    projectGuid: "some-guid",
    systemStartTimestamp: new Date().toISOString(),
    systemEndTimestamp: new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString(),
    collectionDate: new Date().toISOString().split('T')[0],
    collectorName: "Test User",
    boundarySizeHa: 100,
    boundaryGeometry: {
      type: "MultiPolygon",
      coordinates: [
        [
          [
            [-124, 49],
            [-125, 50],
            [-126, 49],
            [-124, 49],
          ]
        ]
      ] as Position[][][]
    },
    locationGeometry: [-124, 49],
  };
  const projectGuid = '12345';
  const projectPlanFiscalGuid = 'fiscal-6789';

  const updatedFiscal: ProjectFiscal = {
    projectGuid,
    projectPlanFiscalGuid,
    fiscalYear: 2024,
    projectFiscalName: 'Updated Fiscal',
    activityCategoryCode: 'RX_DEV',
    planFiscalStatusCode: { planFiscalStatusCode: 'COMPLETE' },
    projectPlanStatusCode: 'ACTIVE',
    proposalTypeCode: 'NEW',
    isApprovedInd: false,
    isDelayedInd: false,
    totalCostEstimateAmount: 0,
    fiscalPlannedProjectSizeHa: 0,
    fiscalPlannedCostPerHaAmt: 0,
    fiscalReportedSpendAmount: 0,
    fiscalActualAmount: 0,
    fiscalActualCostPerHaAmt: 0,
    firstNationsDelivPartInd: false,
    firstNationsEngagementInd: false
  };


  beforeEach(() => {
    mockAppConfigService = jasmine.createSpyObj('AppConfigService', ['getConfig']);
    mockTokenService = jasmine.createSpyObj('TokenService', ['getOauthToken'], { credentialsEmitter: of({ userGuid: 'mock-user-guid' }) });

    mockAppConfigService.getConfig.and.returnValue(mockConfig);
    mockTokenService.getOauthToken.and.returnValue('mock-token');

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ProjectService,
        { provide: AppConfigService, useValue: mockAppConfigService },
        { provide: TokenService, useValue: mockTokenService },
      ]
    });

    service = TestBed.inject(ProjectService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch projects', () => {
    const mockProjects = [{ projectName: 'Project 1' }];
    service.fetchProjects().subscribe((projects) => {
      expect(projects).toEqual(mockProjects);
    });

    const req = httpMock.expectOne('http://mock-api.com/wfprev-api/projects');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockProjects);
  });

  it('should create a project', () => {
    const mockProject = { projectName: 'New Project' };
    service.createProject(mockProject).subscribe((response) => {
      expect(response).toEqual(mockProject);
    });

    const req = httpMock.expectOne('http://mock-api.com/wfprev-api/projects');
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(mockProject);
    req.flush(mockProject);
  });

  it('should fetch project by project GUID', () => {
    const mockProject = { projectName: 'Project 1' };
    const projectGuid = '12345';
    service.getProjectByProjectGuid(projectGuid).subscribe((project) => {
      expect(project).toEqual(mockProject);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockProject);
  });

  it('should update a project', () => {
    const projectGuid = '12345';
    const updatedProject: Project = {
      projectGuid: 'project-guid-123',
      projectName: 'Updated Project',
      bcParksRegionOrgUnitId: 1,
      bcParksSectionOrgUnitId: 2,
      closestCommunityName: 'Community',
      fireCentreOrgUnitId: 3,
      isMultiFiscalYearProj: false,
      programAreaGuid: 'program-guid',
      projectDescription: 'Description',
      projectLead: 'Lead',
      projectLeadEmailAddress: 'email@example.com',
      projectNumber: 100,
      siteUnitName: 'Site Unit',
      totalActualAmount: 1000,
      totalAllocatedAmount: 500,
      totalFundingRequestAmount: 200,
      totalPlannedCostPerHectare: 10,
      totalPlannedProjectSizeHa: 20,
      forestDistrictOrgUnitId: 0,
      forestRegionOrgUnitId: 0
    };

    service.updateProject(projectGuid, updatedProject).subscribe((response) => {
      expect(response).toEqual(updatedProject);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(updatedProject);
    req.flush(updatedProject);
  });

  it('should handle errors when fetching projects', () => {
    service.fetchProjects().subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to fetch projects');
      }
    });

    const req = httpMock.expectOne('http://mock-api.com/wfprev-api/projects');
    expect(req.request.method).toBe('GET');
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should handle errors when fetching project details', () => {
    const projectGuid = '12345';
    service.getProjectByProjectGuid(projectGuid).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to fetch project details');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should handle errors when updating a project', () => {
    const projectGuid = '12345';
    const updatedProject: Project = {
      projectGuid: 'project-guid-123',
      projectName: 'Updated Project',
      bcParksRegionOrgUnitId: 1,
      bcParksSectionOrgUnitId: 2,
      closestCommunityName: 'Community',
      fireCentreOrgUnitId: 3,
      isMultiFiscalYearProj: false,
      programAreaGuid: 'program-guid',
      projectDescription: 'Description',
      projectLead: 'Lead',
      projectLeadEmailAddress: 'email@example.com',
      projectNumber: 100,
      siteUnitName: 'Site Unit',
      totalActualAmount: 1000,
      totalAllocatedAmount: 500,
      totalFundingRequestAmount: 200,
      totalPlannedCostPerHectare: 10,
      totalPlannedProjectSizeHa: 20,
      forestDistrictOrgUnitId: 0,
      forestRegionOrgUnitId: 0
    };

    service.updateProject(projectGuid, updatedProject).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.status).toBe(500);
        expect(error.message).toContain('500 Server Error');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should fetch project fiscals by project GUID', () => {
    const projectGuid = '12345';
    const mockFiscals = { _embedded: { projectFiscals: [{ fiscalYear: 2023, projectFiscalName: 'Test Fiscal' }] } };

    service.getProjectFiscalsByProjectGuid(projectGuid).subscribe((fiscals) => {
      expect(fiscals).toEqual(mockFiscals);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockFiscals);
  });

  it('should handle errors when fetching project fiscals', () => {
    const projectGuid = '12345';

    service.getProjectFiscalsByProjectGuid(projectGuid).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to fetch project fiscals');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should create a project fiscal', () => {
    const projectGuid = '12345';

    service.createProjectFiscal(projectGuid, updatedFiscal).subscribe((response) => {
      expect(response).toEqual(updatedFiscal);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(updatedFiscal);
    req.flush(updatedFiscal);
  });

  it('should handle errors when creating a project fiscal', () => {
    const projectGuid = '12345';

    service.createProjectFiscal(projectGuid, updatedFiscal).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to create project fiscal');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should update a project fiscal', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';

    service.updateProjectFiscal(projectGuid, projectPlanFiscalGuid, updatedFiscal).subscribe((response) => {
      expect(response).toEqual(updatedFiscal);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(updatedFiscal);
    req.flush(updatedFiscal);
  });

  it('should handle errors when updating a project fiscal', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';

    service.updateProjectFiscal(projectGuid, projectPlanFiscalGuid, updatedFiscal).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to update project fiscal');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should delete a project fiscal', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';

    service.deleteProjectFiscalByProjectPlanFiscalGuid(projectGuid, projectPlanFiscalGuid).subscribe((response) => {
      expect(response).toBeTruthy(); // Expect a success response
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}`);
    expect(req.request.method).toBe('DELETE'); // Ensure DELETE method is used
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token'); // Check Authorization header
    req.flush({}); // Simulate successful deletion response
  });

  it('should handle errors when deleting a project fiscal', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';

    service.deleteProjectFiscalByProjectPlanFiscalGuid(projectGuid, projectPlanFiscalGuid).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to delete project fiscals'); // Check error message
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}`);
    req.flush('Error', { status: 500, statusText: 'Server Error' }); // Simulate server error response
  });

  it('should fetch fiscal activities', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const mockActivities = { _embedded: { activities: [{ activityName: 'Test Activity' }] } };

    service.getFiscalActivities(projectGuid, projectPlanFiscalGuid).subscribe((activities) => {
      expect(activities).toEqual(mockActivities);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockActivities);
  });

  it('should handle errors when fetching fiscal activities', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';

    service.getFiscalActivities(projectGuid, projectPlanFiscalGuid).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to fetch activities');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should create a fiscal activity', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const newActivity = { activityName: 'New Activity' };

    service.createFiscalActivity(projectGuid, projectPlanFiscalGuid, newActivity).subscribe((response) => {
      expect(response).toEqual(newActivity);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(newActivity);
    req.flush(newActivity);
  });

  it('should handle errors when creating a fiscal activity', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const newActivity = { activityName: 'New Activity' };

    service.createFiscalActivity(projectGuid, projectPlanFiscalGuid, newActivity).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to create activities');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should update a fiscal activity', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const activityGuid = 'activity-001';
    const updatedActivity = { activityName: 'Updated Activity' };

    service.updateFiscalActivities(projectGuid, projectPlanFiscalGuid, activityGuid, updatedActivity).subscribe((response) => {
      expect(response).toEqual(updatedActivity);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(updatedActivity);
    req.flush(updatedActivity);
  });

  it('should handle errors when updating a fiscal activity', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const activityGuid = 'activity-001';
    const updatedActivity = { activityName: 'Updated Activity' };

    service.updateFiscalActivities(projectGuid, projectPlanFiscalGuid, activityGuid, updatedActivity).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to update activities');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should delete an activity', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const activityGuid = 'activity-001';

    service.deleteActivity(projectGuid, projectPlanFiscalGuid, activityGuid).subscribe((response) => {
      expect(response).toBeTruthy();
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}`);
    expect(req.request.method).toBe('DELETE');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush({});
  });

  it('should handle errors when deleting an activity', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const activityGuid = 'activity-001';

    service.deleteActivity(projectGuid, projectPlanFiscalGuid, activityGuid).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to delete activity');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should upload a document with progress tracking', (done) => {
    const mockFile = new File(['dummy content'], 'test-file.txt', { type: 'text/plain' });

    const mockResponse = { success: true, filePath: '/mock/path/test-file.txt' };

    const uploadProgressSpy = jasmine.createSpy('onProgress');

    service.uploadDocument({
      file: mockFile,
      onProgress: uploadProgressSpy
    }).subscribe({
      next: (event) => {
        if (event) {
          expect(event).toEqual(mockResponse);
          expect(uploadProgressSpy).toHaveBeenCalled();
          done();
        }
      },
      error: () => fail('Should not fail during upload')
    });

    const req = httpMock.expectOne(`${mockConfig.rest.wfdm}/documents`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toBeInstanceOf(FormData);

    // Simulating upload progress event
    req.event({ type: HttpEventType.UploadProgress, loaded: 500, total: 1000 });

    // Simulating successful upload response
    req.flush(mockResponse);
  });

  it('should fetch project boundaries by project GUID', () => {
    const projectGuid = '12345';
    const mockBoundaries = { type: 'FeatureCollection', features: [] }; // Example GeoJSON response

    service.getProjectBoundaries(projectGuid).subscribe((boundaries) => {
      expect(boundaries).toEqual(mockBoundaries);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectBoundary`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockBoundaries);
  });

  it('should handle errors when fetching project boundaries', () => {
    const projectGuid = '12345';

    service.getProjectBoundaries(projectGuid).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to fetch project boundaries');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectBoundary`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should create a project boundary', () => {
    const projectGuid = '12345';

    service.createProjectBoundary(projectGuid, mockProjectBoundary).subscribe((response) => {
      expect(response).toEqual(mockProjectBoundary);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectBoundary`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(mockProjectBoundary);
    req.flush(mockProjectBoundary);
  });

  it('should handle errors when creating a project boundary', () => {
    const projectGuid = '12345';

    service.createProjectBoundary(projectGuid, mockProjectBoundary).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to create project boundary');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectBoundary`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should delete a project boundary', () => {
    const projectGuid = 'project-123';
    const projectBoundaryGuid = 'boundary-456';

    service.deleteProjectBoundary(projectGuid, projectBoundaryGuid).subscribe((response) => {
      expect(response).toBeTruthy();
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectBoundary/${projectBoundaryGuid}`);
    expect(req.request.method).toBe('DELETE');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush({});
  });

  it('should handle errors when deleting a project boundary', () => {
    const projectGuid = 'project-123';
    const projectBoundaryGuid = 'boundary-456';

    service.deleteProjectBoundary(projectGuid, projectBoundaryGuid).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to delete project boundary');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectBoundary/${projectBoundaryGuid}`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should fetch activity boundaries', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const activityGuid = 'activity-001';
    const mockBoundaries = { _embedded: { activityBoundary: [{ id: 'abc' }] } };

    service.getActivityBoundaries(projectGuid, projectPlanFiscalGuid, activityGuid).subscribe((boundaries) => {
      expect(boundaries).toEqual(mockBoundaries);
    });

    const req = httpMock.expectOne(
      `http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}/activityBoundary`
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockBoundaries);
  });

  it('should handle errors when fetching activity boundaries', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const activityGuid = 'activity-001';

    service.getActivityBoundaries(projectGuid, projectPlanFiscalGuid, activityGuid).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to fetch activity boundaries');
      }
    });

    const req = httpMock.expectOne(
      `http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}/activityBoundary`
    );
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should create an activity boundary', () => {
    const projectGuid = 'project-1';
    const fiscalGuid = 'fiscal-1';
    const activityGuid = 'activity-1';
    const mockBoundary = { boundarySizeHa: 12 };

    service.createActivityBoundary(projectGuid, fiscalGuid, activityGuid, mockBoundary).subscribe(response => {
      expect(response).toEqual(mockBoundary);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/activityBoundary`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockBoundary);
    req.flush(mockBoundary);
  });

  it('should handle errors when creating an activity boundary', () => {
    const projectGuid = 'project-1';
    const fiscalGuid = 'fiscal-1';
    const activityGuid = 'activity-1';
    const mockBoundary = { boundarySizeHa: 12 };

    service.createActivityBoundary(projectGuid, fiscalGuid, activityGuid, mockBoundary).subscribe({
      next: () => fail('Should have failed'),
      error: (err) => {
        expect(err.message).toBe('Failed to create activity boundary');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/activityBoundary`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should delete an activity boundary', () => {
    const projectGuid = 'project-1';
    const fiscalGuid = 'fiscal-1';
    const activityGuid = 'activity-1';
    const boundaryGuid = 'boundary-1';

    service.deleteActivityBoundary(projectGuid, fiscalGuid, activityGuid, boundaryGuid).subscribe(response => {
      expect(response).toBeTruthy();
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/activityBoundary/${boundaryGuid}`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('should handle errors when deleting an activity boundary', () => {
    const projectGuid = 'project-1';
    const fiscalGuid = 'fiscal-1';
    const activityGuid = 'activity-1';
    const boundaryGuid = 'boundary-1';

    service.deleteActivityBoundary(projectGuid, fiscalGuid, activityGuid, boundaryGuid).subscribe({
      next: () => fail('Should have failed'),
      error: (err) => {
        expect(err.message).toBe('Failed to delete activity boundary');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/activityBoundary/${boundaryGuid}`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should download a document by file ID', () => {
    const fileId = 'file-123';
    const mockBlob = new Blob(['test content'], { type: 'application/pdf' });

    service.downloadDocument(fileId).subscribe(response => {
      expect(response).toEqual(mockBlob);
    });

    const req = httpMock.expectOne(`http://mock-wfdm-api.com/documents/${fileId}/bytes`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.responseType).toBe('blob');
    req.flush(mockBlob);
  });

  it('should fetch features with query params', () => {
    const mockResponse: FeaturesResponse = {
      projects: [
        {
          bcParksRegionOrgUnitId: 1,
          bcParksSectionOrgUnitId: 101,
          closestCommunityName: 'Community A',
          fireCentreOrgUnitId: 201,
          forestDistrictOrgUnitId: 301,
          forestRegionOrgUnitId: 401,
          isMultiFiscalYearProj: false,
          programAreaGuid: 'program-guid-a',
          projectDescription: 'Description A',
          projectGuid: 'project-guid-a',
          projectLead: 'Lead A',
          projectLeadEmailAddress: 'leada@example.com',
          projectName: 'Project A',
          projectNumber: 1001,
          siteUnitName: 'Site A',
          totalActualAmount: 10000,
          totalAllocatedAmount: 12000,
          totalFundingRequestAmount: 15000,
          totalPlannedCostPerHectare: 200,
          totalPlannedProjectSizeHa: 50,
          latitude: 49.1,
          longitude: -123.1
        }
      ]
    };

    const params = {
      programAreaGuid: ['guid-1'],
      fiscalYear: ['2023'],
      searchText: 'fuel'
    };

    service.getFeatures(params).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne((request) => {
      return request.url === 'http://mock-api.com/wfprev-api/features' &&
        request.params.has('programAreaGuid') &&
        request.params.has('fiscalYear') &&
        request.params.get('searchText') === 'fuel';
    });

    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockResponse);
  });

  it('should handle errors when fetching features', () => {
    service.getFeatures({ searchText: 'fire' }).subscribe({
      next: () => fail('Should have failed'),
      error: (err) => {
        expect(err.message).toBe('Failed to fetch features');
      }
    });

    const req = httpMock.expectOne((request) =>
      request.url === 'http://mock-api.com/wfprev-api/features'
    );
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should fetch evaluation criteria summaries', () => {
    const projectGuid = 'project-1';
    const mockResponse = { summaries: [] };

    service.getEvaluationCriteriaSummaries(projectGuid).subscribe((result) => {
      expect(result).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/evaluationCriteriaSummary`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockResponse);
  });

  it('should handle error when fetching evaluation criteria summaries', () => {
    const projectGuid = 'project-1';

    service.getEvaluationCriteriaSummaries(projectGuid).subscribe({
      next: () => fail('Should have failed'),
      error: (err) => {
        expect(err.message).toBe('Failed to fetch evaluation criteria summaries');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/evaluationCriteriaSummary`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should create evaluation criteria summary', () => {
    const projectGuid = 'project-1';
    const criteriaSummary = { criteria: 'data' } as any;
    const mockResponse = {} as EvaluationCriteriaSummaryModel;

    service.createEvaluationCriteriaSummary(projectGuid, criteriaSummary).subscribe((result) => {
      expect(result).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/evaluationCriteriaSummary`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(criteriaSummary);
    req.flush(mockResponse);
  });

  it('should handle error when creating evaluation criteria summary', () => {
    const projectGuid = 'project-1';
    const criteriaSummary = { criteria: 'data' } as any;

    service.createEvaluationCriteriaSummary(projectGuid, criteriaSummary).subscribe({
      next: () => fail('Should have failed'),
      error: (err) => {
        expect(err.message).toBe('Failed to create evaluation criteria summary');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/evaluationCriteriaSummary`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should update evaluation criteria summary', () => {
    const projectGuid = 'project-1';
    const summaryGuid = 'summary-1';
    const criteriaSummary = { criteria: 'updated' } as any;
    const mockResponse = {} as EvaluationCriteriaSummaryModel;


    service.updateEvaluationCriteriaSummary(projectGuid, summaryGuid, criteriaSummary).subscribe((result) => {
      expect(result).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/evaluationCriteriaSummary/${summaryGuid}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(criteriaSummary);
    req.flush(mockResponse);
  });


  it('should handle error when updating evaluation criteria summary', () => {
    const projectGuid = 'project-1';
    const summaryGuid = 'summary-1';
    const criteriaSummary = { criteria: 'updated' } as any;

    service.updateEvaluationCriteriaSummary(projectGuid, summaryGuid, criteriaSummary).subscribe({
      next: () => fail('Should have failed'),
      error: (err) => {
        expect(err.message).toBe('Failed to update evaluation criteria summary');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/evaluationCriteriaSummary/${summaryGuid}`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should download projects and return blob', () => {
    const body: ReportRequest = {
      reportType: 'csv',
      projects: [
        { projectGuid: 'guid1' },
        { projectGuid: 'guid2' }
      ]
    };

    const mockBlob = new Blob(['test data'], { type: 'text/csv' });

    service.downloadProjects(body).subscribe((result) => {
      expect(result).toEqual(mockBlob);
    });

    const req = httpMock.expectOne('http://mock-api.com/wfprev-api/reports');
    expect(req.request.method).toBe('POST');
    expect(req.request.responseType).toBe('blob');
    expect(req.request.body).toEqual(body);
    req.flush(mockBlob);
  });

  it('should handle error when downloading projects', () => {
    const body: ReportRequest = {
      reportType: 'csv',
      projects: [
        { projectGuid: 'guid1' },
        { projectGuid: 'guid2' }
      ]
    };

    service.downloadProjects(body).subscribe({
      next: () => fail('Should have failed'),
      error: (err) => {
        expect(err).toBeTruthy();
        expect(err.message).toBe('Failed to download projects');
      }
    });

    const req = httpMock.expectOne('http://mock-api.com/wfprev-api/reports');
    expect(req.request.method).toBe('POST');
    expect(req.request.responseType).toBe('blob'); 
    expect(req.request.body).toEqual(body);
    expect(req.request.headers.get('Accept')).toBe('application/octet-stream');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');

    const errorBlob = new Blob(['Error'], { type: 'text/plain' });
    req.flush(errorBlob, { status: 500, statusText: 'Server Error' });
  });

   it('should delete evaluation criteria summary', () => {
    const projectGuid = 'project-1';
    const summaryGuid = 'summary-1';

    service.deleteEvaluationCriteriaSummary(projectGuid, summaryGuid).subscribe((result) => {
      expect(result).toBeTruthy();
    });

    const req = httpMock.expectOne(
      `http://mock-api.com/wfprev-api/projects/${projectGuid}/evaluationCriteriaSummary/${summaryGuid}`
    );
    expect(req.request.method).toBe('DELETE');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush({});
  });

  it('should handle error when deleting evaluation criteria summary', () => {
    const projectGuid = 'project-1';
    const summaryGuid = 'summary-1';

    service.deleteEvaluationCriteriaSummary(projectGuid, summaryGuid).subscribe({
      next: () => fail('Should have failed'),
      error: (err) => {
        expect(err.message).toBe('Failed to delete evaluation criteria');
      }
    });

    const req = httpMock.expectOne(
      `http://mock-api.com/wfprev-api/projects/${projectGuid}/evaluationCriteriaSummary/${summaryGuid}`
    );
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should fetch project locations with query params', () => {
    const mockResponse = {
      _embedded: {
        project: [
          {
            projectGuid: 'guid-1',
            projectName: 'Fuel Management',
            latitude: 49.1,
            longitude: -123.1
          },
          {
            projectGuid: 'guid-2',
            projectName: 'Fire Prevention',
            latitude: 49.2,
            longitude: -123.2
          }
        ]
      }
    };

    const params = {
      programAreaGuid: ['area-1'],
      fiscalYear: ['2024'],
      searchText: 'fuel'
    };

    service.getProjectLocations(params).subscribe((locations: any[]) => {
      expect(locations.length).toBe(2);
      expect(locations[0].projectName).toBe('Fuel Management');
      expect(locations[1].latitude).toBe(49.2);
    });

    const req = httpMock.expectOne((request) => {
      return (
        request.url === 'http://mock-api.com/wfprev-api/project-locations' &&
        request.params.has('programAreaGuid') &&
        request.params.has('fiscalYear') &&
        request.params.get('searchText') === 'fuel'
      );
    });

    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockResponse);
  });

  it('should handle errors when fetching project locations', () => {
    const params = { searchText: 'fire' };

    service.getProjectLocations(params).subscribe({
      next: () => fail('Should have failed'),
      error: (err) => {
        expect(err).toBeTruthy();
        expect(err.message).toBe('Failed to fetch project locations');
      },
    });

    const req = httpMock.expectOne(
      (request) => request.url === 'http://mock-api.com/wfprev-api/project-locations'
    );

    expect(req.request.method).toBe('GET');
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

    it('should fetch a feature by projectGuid and return the first embedded project', () => {
      const projectGuid = 'project-guid-123';
      const mockProject: Project = {
        projectGuid,
        projectName: 'Feature Project',
        bcParksRegionOrgUnitId: 1,
        bcParksSectionOrgUnitId: 2,
        closestCommunityName: 'Community',
        fireCentreOrgUnitId: 3,
        forestDistrictOrgUnitId: 4,
        forestRegionOrgUnitId: 5,
        isMultiFiscalYearProj: false,
        programAreaGuid: 'program-guid',
        projectDescription: 'Test description',
        projectLead: 'Lead',
        projectLeadEmailAddress: 'lead@example.com',
        projectNumber: 42,
        siteUnitName: 'Site',
        totalActualAmount: 100,
        totalAllocatedAmount: 100,
        totalFundingRequestAmount: 100,
        totalPlannedCostPerHectare: 10,
        totalPlannedProjectSizeHa: 20
      };

      const mockResponse = {
        _embedded: { project: [mockProject] }
      };

      service.getFeatureByProjectGuid(projectGuid).subscribe(result => {
        expect(result).toEqual(mockProject);
      });

      const req = httpMock.expectOne((request) => {
        return request.url === 'http://mock-api.com/wfprev-api/features' &&
              request.params.get('projectGuid') === projectGuid;
      });

      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
      req.flush(mockResponse);
    });

    it('should return null if no project found in embedded', () => {
      const projectGuid = 'nonexistent-guid';
      const mockResponse = { _embedded: { project: [] } };

      service.getFeatureByProjectGuid(projectGuid).subscribe(result => {
        expect(result).toBeNull();
      });

      const req = httpMock.expectOne(
        r => r.url === 'http://mock-api.com/wfprev-api/features'
      );
      req.flush(mockResponse);
    });

    it('should handle error when fetching feature by projectGuid', () => {
      const projectGuid = 'error-guid';

      service.getFeatureByProjectGuid(projectGuid).subscribe({
        next: () => fail('Should have failed'),
        error: (err) => {
          expect(err.message).toBe('Failed to fetch feature by projectGuid');
        }
      });

      const req = httpMock.expectOne(
        r => r.url === 'http://mock-api.com/wfprev-api/features'
      );
      req.flush('Error', { status: 500, statusText: 'Server Error' });
    });


});
