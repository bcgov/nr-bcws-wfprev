import { TestBed } from '@angular/core/testing';

import { ProjectFiscalsSignalService } from './project-fiscals-signal.service';

describe('ProjectFiscalsSignalService', () => {
  let service: ProjectFiscalsSignalService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectFiscalsSignalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
