import { TestBed } from '@angular/core/testing';
import { SharedCodeTableService } from './shared-code-table.service';

describe('SharedCodeTableService', () => {
  let service: SharedCodeTableService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SharedCodeTableService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should emit an empty object by default', (done) => {
    service.codeTables$.subscribe((value) => {
      expect(value).toEqual({});
      done();
    });
  });

  it('should emit updated code tables when updateCodeTables is called', (done) => {
    const mockTables = {
      programAreaCode: [{ programAreaGuid: 'guid1', programAreaName: 'Area 1' }],
      forestRegionCode: [{ orgUnitId: 101, orgUnitName: 'Region 1' }]
    };

    service.codeTables$.subscribe((value) => {
      if (Object.keys(value).length) {
        expect(value).toEqual(mockTables);
        done();
      }
    });

    service.updateCodeTables(mockTables);
  });
});
