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
});
