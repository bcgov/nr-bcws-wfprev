// shared-service.spec.ts
import { TestBed } from '@angular/core/testing';
import { SharedService } from './shared-service';

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
    const testProjects = [
      { projectName: 'Project A', latitude: 49.1 },
      { projectName: 'Project B', latitude: 49.2 }
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
