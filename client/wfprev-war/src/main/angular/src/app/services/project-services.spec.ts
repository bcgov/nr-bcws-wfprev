import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import { Project } from 'src/app/components/models';
import { ProjectService } from 'src/app/services/project-services';
import { HttpEventType } from '@angular/common/http';

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
      acronym: 'TEST', // Add a mock acronym
      version: '1.0.0', // Add a mock version
      environment: 'test', // Add a mock environment
    },
    webade: {
      oauth2Url: 'http://mock-oauth-url.com',
      clientId: 'mock-client-id',
      authScopes: 'mock-scope',
      checkTokenUrl: 'http://mock-check-token-url.com',
      enableCheckToken: false,
    }
  };
  
  

  beforeEach(() => {
    mockAppConfigService = jasmine.createSpyObj('AppConfigService', ['getConfig']);
    mockTokenService = jasmine.createSpyObj('TokenService', ['getOauthToken']);

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
        expect(error.message).toBe('Failed to update project');
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
    const mockFiscal = { fiscalYear: 2023, projectFiscalName: 'New Fiscal' };
  
    service.createProjectFiscal(projectGuid, mockFiscal).subscribe((response) => {
      expect(response).toEqual(mockFiscal);
    });
  
    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    expect(req.request.body).toEqual(mockFiscal);
    req.flush(mockFiscal);
  });
  
it('should handle errors when creating a project fiscal', () => {
  const projectGuid = '12345';
  const mockFiscal = { fiscalYear: 2023, projectFiscalName: 'New Fiscal' };

  service.createProjectFiscal(projectGuid, mockFiscal).subscribe({
    next: () => fail('Should have failed with an error'),
    error: (error) => {
      expect(error.message).toBe('Failed to create project fiscal'); // ✅ Now correctly matches the service error
    }
  });

  const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals`);
  req.flush('Error', { status: 500, statusText: 'Server Error' });
});
  
  it('should update a project fiscal', () => {
    const projectGuid = '12345';
    const projectPlanFiscalGuid = 'fiscal-6789';
    const updatedFiscal = { fiscalYear: 2024, projectFiscalName: 'Updated Fiscal' };
  
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
    const updatedFiscal = { fiscalYear: 2024, projectFiscalName: 'Updated Fiscal' };
  
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
  
  it('should fetch activity boundaries', () => {
    const projectGuid = 'project-123';
    const projectPlanFiscalGuid = 'fiscal-456';
    const activityGuid = 'activity-789';
    const mockBoundaries = { boundaryId: 'boundary-001', coordinates: [1, 2] };
  
    service.getActivityBoundaries(projectGuid, projectPlanFiscalGuid, activityGuid)
      .subscribe((boundaries) => {
        expect(boundaries).toEqual(mockBoundaries);
      });
  
    const req = httpMock.expectOne(
      `http://mock-api.com/wfprev-api/projects/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}/activityBoundary`
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockBoundaries);
  });
  
  it('should fetch project boundaries', () => {
    const projectGuid = 'project-123';
    const mockProjectBoundary = { boundaryId: 'boundary-002', coordinates: [3, 4] };
  
    service.getProjectBoundaries(projectGuid)
      .subscribe((boundaries) => {
        expect(boundaries).toEqual(mockProjectBoundary);
      });
  
    const req = httpMock.expectOne(
      `http://mock-api.com/wfprev-api/projects/${projectGuid}/projectBoundary`
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockProjectBoundary);
  });
  
  it('should handle errors when fetching activity boundaries', () => {
    const projectGuid = 'project-123';
    const projectPlanFiscalGuid = 'fiscal-456';
    const activityGuid = 'activity-789';
  
    service.getActivityBoundaries(projectGuid, projectPlanFiscalGuid, activityGuid)
      .subscribe({
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
  
  it('should handle errors when fetching project boundaries', () => {
    const projectGuid = 'project-123';
  
    service.getProjectBoundaries(projectGuid)
      .subscribe({
        next: () => fail('Should have failed with an error'),
        error: (error) => {
          expect(error.message).toBe('Failed to fetch project boundaries');
        }
      });
  
    const req = httpMock.expectOne(
      `http://mock-api.com/wfprev-api/projects/${projectGuid}/projectBoundary`
    );
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });
  
  
});
