import { TestBed } from '@angular/core/testing';

import { ProjectFilterStateService } from './project-filter-state.service';

describe('ProjectFilterStateService', () => {
  let service: ProjectFilterStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectFilterStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
