import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { SearchFilterComponent } from './search-filter.component';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { SharedService } from 'src/app/services/shared-service';
import { of, Subject } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatOptionSelectionChange } from '@angular/material/core';

describe('SearchFilterComponent', () => {
  let component: SearchFilterComponent;
  let fixture: ComponentFixture<SearchFilterComponent>;
  let mockSharedService: jasmine.SpyObj<SharedService>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;
  let mockSharedCodeTableService: jasmine.SpyObj<SharedCodeTableService>;

  beforeEach(async () => {
    mockSharedService = jasmine.createSpyObj('SharedService', ['updateFilters']);
    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchFireCentres']);
    mockSharedCodeTableService = jasmine.createSpyObj('SharedCodeTableService', [], {
      codeTables$: of({
        businessAreas: [{ programAreaName: 'Area A', programAreaGuid: 'guid-a' }],
        forestRegions: [{ orgUnitName: 'Region 1', orgUnitId: 'r1' }],
        forestDistricts: [{ orgUnitName: 'District 1', orgUnitId: 'd1', parentOrgUnitId: 'r1' }],
        activityCategoryCode: [{ description: 'Activity A', activityCategoryCode: 'a1' }],
        planFiscalStatusCode: [{ description: 'Approved', planFiscalStatusCode: 'P' }]
      })
    });

    mockCodeTableService.fetchFireCentres.and.returnValue(of({
      features: [
        {
          properties: {
            MOF_FIRE_CENTRE_NAME: 'Centre 1',
            MOF_FIRE_CENTRE_ID: 'fc1'
          }
        }
      ]
    }));

    await TestBed.configureTestingModule({
      imports: [SearchFilterComponent, BrowserAnimationsModule],
      providers: [
        { provide: SharedService, useValue: mockSharedService },
        { provide: CodeTableServices, useValue: mockCodeTableService },
        { provide: SharedCodeTableService, useValue: mockSharedCodeTableService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should generate fiscal year options', () => {
    component.generateFiscalYearOptions();
    expect(component.fiscalYearOptions.length).toBeGreaterThan(0);
    expect(component.fiscalYearOptions[0].label).toBe('All');
  });

  it('should call emitFilters when onSearch is triggered', () => {
    component.onSearch();
    expect(mockSharedService.updateFilters).toHaveBeenCalled();
  });

  it('should handle onReset correctly', () => {
    component.selectedBusinessArea = ['x'];
    component.searchText = 'test';
    component.onReset();
    expect(component.searchText).toBe('');
    expect(component.selectedBusinessArea).toEqual([]);
    expect(mockSharedService.updateFilters).toHaveBeenCalled();
  });

  it('should prepend All and sort options', () => {
    const result = component.prependAllAndSort([
      { label: 'B', value: 'b' },
      { label: 'A', value: 'a' }
    ]);
    expect(result[0].label).toBe('All');
    expect(result[1].label).toBe('A');
  });

  it('should call updateFilters in onOptionToggled', () => {
    const options = [
      { value: '1' },
      { value: '2' }
    ];
    component.selectedActivity = ['__ALL__'];
    component.onOptionToggled({ source: { value: '__ALL__' } }, 'selectedActivity', options);
    expect(mockSharedService.updateFilters).toHaveBeenCalled();
  });

  it('should debounce search and call onSearch', fakeAsync(() => {
    spyOn(component, 'onSearch');
    component.searchTextChanged.next('debounced');
    tick(3000);
    expect(component.onSearch).toHaveBeenCalled();
  }));

  it('should filter forest districts on forest region change', () => {
    component.selectedForestRegion = ['r1'];
    component.rawForestDistricts = [
      { orgUnitId: 'd1', orgUnitName: 'District 1', parentOrgUnitId: 'r1' },
      { orgUnitId: 'd2', orgUnitName: 'District 2', parentOrgUnitId: 'r2' }
    ];
    component.onForestRegionChange();
    expect(component.forestDistrictOptions.length).toBeGreaterThan(0);
  });

  it('should reset districts if no forest region selected', () => {
    component.rawForestDistricts = [
      { orgUnitId: 'd1', orgUnitName: 'District 1', parentOrgUnitId: 'r1' }
    ];
    component.selectedForestRegion = [];
    component.onForestRegionChange();
    expect(component.forestDistrictOptions.length).toBeGreaterThan(0);
  });

  it('should load fire centres', () => {
    component.loadFireCentres();
    expect(mockCodeTableService.fetchFireCentres).toHaveBeenCalled();
  });

  it('should clear selection when "All" is deselected', fakeAsync(() => {
    component.selectedBusinessArea = ['__ALL__', 'guid-a'];
    const event = {
      isUserInput: true,
      source: {
        selected: false,
        value: '__ALL__'
      }
    } as MatOptionSelectionChange;

    component.syncAllWithItemToggle(event, '__ALL__', 'selectedBusinessArea', [
      { value: '__ALL__' },
      { value: 'guid-a' }
    ]);
    
    tick();
    expect(component.selectedBusinessArea).toEqual([]);
    expect(mockSharedService.updateFilters).toHaveBeenCalled();
  }));

  it('should remove individual and "__ALL__" when individual is deselected', fakeAsync(() => {
    component.selectedBusinessArea = ['__ALL__', 'guid-a', 'guid-b'];
    const event = {
      isUserInput: true,
      source: {
        selected: false,
        value: 'guid-a'
      }
    } as MatOptionSelectionChange;

    component.syncAllWithItemToggle(event, 'guid-a', 'selectedBusinessArea', [
      { value: '__ALL__' },
      { value: 'guid-a' },
      { value: 'guid-b' }
    ]);
    
    tick();
    expect(component.selectedBusinessArea).toEqual(['guid-b']);
    expect(mockSharedService.updateFilters).toHaveBeenCalled();
  }));

  it('should add "__ALL__" when all individuals are selected', fakeAsync(() => {
    component.selectedBusinessArea = ['guid-a'];
    const event = {
      isUserInput: true,
      source: {
        selected: true,
        value: 'guid-b'
      }
    } as MatOptionSelectionChange;

    component.syncAllWithItemToggle(event, 'guid-b', 'selectedBusinessArea', [
      { value: '__ALL__' },
      { value: 'guid-a' },
      { value: 'guid-b' }
    ]);
    
    tick();
    expect(component.selectedBusinessArea).toEqual(['__ALL__', 'guid-a', 'guid-b']);
    expect(mockSharedService.updateFilters).toHaveBeenCalled();
  }));

});
