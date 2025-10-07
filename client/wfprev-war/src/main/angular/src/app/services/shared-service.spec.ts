import { TestBed } from '@angular/core/testing';
import { SharedService } from './shared-service';
import { Project } from 'src/app/components/models';

describe('SharedService', () => {
  let service: SharedService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SharedService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should emit new filters when updateFilters is called', (done) => {
    const testFilters = { region: 'North', type: 'Wildfire' };

    service.filters$.subscribe((filters) => {
      if (filters) {
        expect(filters).toEqual(testFilters);
        done();
      }
    });

    service.updateFilters(testFilters);
  });

  it('should emit new displayed projects when updateDisplayedProjects is called', (done) => {
    const testProjects: Project[] = [
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
      },
      {
        bcParksRegionOrgUnitId: 2,
        bcParksSectionOrgUnitId: 102,
        closestCommunityName: 'Community B',
        fireCentreOrgUnitId: 202,
        forestDistrictOrgUnitId: 302,
        forestRegionOrgUnitId: 402,
        isMultiFiscalYearProj: true,
        programAreaGuid: 'program-guid-b',
        projectDescription: 'Description B',
        projectGuid: 'project-guid-b',
        projectLead: 'Lead B',
        projectLeadEmailAddress: 'leadb@example.com',
        projectName: 'Project B',
        projectNumber: 1002,
        siteUnitName: 'Site B',
        totalActualAmount: 20000,
        totalAllocatedAmount: 22000,
        totalFundingRequestAmount: 25000,
        totalPlannedCostPerHectare: 300,
        totalPlannedProjectSizeHa: 70,
        latitude: 49.2,
        longitude: -123.2
      }
    ];

    service.displayedProjects$.subscribe((projects) => {
      if (projects.length) {
        expect(projects).toEqual(testProjects);
        done();
      }
    });

    service.updateDisplayedProjects(testProjects);
  });

  it('should emit empty array by default for displayedProjects$', (done) => {
    service.displayedProjects$.subscribe((projects) => {
      expect(projects).toEqual([]);
      done();
    });
  });

  it('should emit null by default for filters$', (done) => {
    service.filters$.subscribe((filters) => {
      expect(filters).toBeNull();
      done();
    });
  });

  it('should emit selected project when selectProject is called', (done) => {
    const testProject: Project = {
      bcParksRegionOrgUnitId: 3,
      bcParksSectionOrgUnitId: 103,
      closestCommunityName: 'Community C',
      fireCentreOrgUnitId: 203,
      forestDistrictOrgUnitId: 303,
      forestRegionOrgUnitId: 403,
      isMultiFiscalYearProj: false,
      programAreaGuid: 'program-guid-c',
      projectDescription: 'Description C',
      projectGuid: 'project-guid-c',
      projectLead: 'Lead C',
      projectLeadEmailAddress: 'leadc@example.com',
      projectName: 'Project C',
      projectNumber: 1003,
      siteUnitName: 'Site C',
      totalActualAmount: 30000,
      totalAllocatedAmount: 33000,
      totalFundingRequestAmount: 35000,
      totalPlannedCostPerHectare: 400,
      totalPlannedProjectSizeHa: 90,
      latitude: 49.3,
      longitude: -123.3
    };

    service.selectedProject$.subscribe((project) => {
      expect(project).toEqual(testProject);
      done();
    });

    service.selectProject(testProject);
  });

  it('should emit map command when triggerMapCommand is called', (done) => {
    const testProject: Project = {
      bcParksRegionOrgUnitId: 4,
      bcParksSectionOrgUnitId: 104,
      closestCommunityName: 'Community D',
      fireCentreOrgUnitId: 204,
      forestDistrictOrgUnitId: 304,
      forestRegionOrgUnitId: 404,
      isMultiFiscalYearProj: true,
      programAreaGuid: 'program-guid-d',
      projectDescription: 'Description D',
      projectGuid: 'project-guid-d',
      projectLead: 'Lead D',
      projectLeadEmailAddress: 'leadd@example.com',
      projectName: 'Project D',
      projectNumber: 1004,
      siteUnitName: 'Site D',
      totalActualAmount: 40000,
      totalAllocatedAmount: 44000,
      totalFundingRequestAmount: 47000,
      totalPlannedCostPerHectare: 500,
      totalPlannedProjectSizeHa: 120,
      latitude: 49.4,
      longitude: -123.4
    };

    service.mapCommand$.subscribe((command) => {
      expect(command).toEqual({ action: 'open', project: testProject });
      done();
    });

    service.triggerMapCommand('open', testProject);
  });

  it('should return the current filters via currentFilters getter', () => {
    const testFilters = { region: 'South', type: 'Forest' };
    service.updateFilters(testFilters);
    expect(service.currentFilters).toEqual(testFilters);
  });

  it('should return the current displayed projects via currentDisplayedProjects getter', () => {
    const testProjects: Project[] = [
      {
        bcParksRegionOrgUnitId: 10,
        bcParksSectionOrgUnitId: 110,
        closestCommunityName: 'Community X',
        fireCentreOrgUnitId: 210,
        forestDistrictOrgUnitId: 310,
        forestRegionOrgUnitId: 410,
        isMultiFiscalYearProj: false,
        programAreaGuid: 'program-guid-x',
        projectDescription: 'Description X',
        projectGuid: 'project-guid-x',
        projectLead: 'Lead X',
        projectLeadEmailAddress: 'leadx@example.com',
        projectName: 'Project X',
        projectNumber: 1010,
        siteUnitName: 'Site X',
        totalActualAmount: 1000,
        totalAllocatedAmount: 1500,
        totalFundingRequestAmount: 2000,
        totalPlannedCostPerHectare: 100,
        totalPlannedProjectSizeHa: 10,
        latitude: 49.5,
        longitude: -123.5
      }
    ];

    service.updateDisplayedProjects(testProjects);
    expect(service.currentDisplayedProjects).toEqual(testProjects);
  });

  it('should emit undefined when selectProject is called without arguments', (done) => {
    service.selectedProject$.subscribe((project) => {
      expect(project).toBeUndefined();
      done();
    });
    service.selectProject();
  });

});
