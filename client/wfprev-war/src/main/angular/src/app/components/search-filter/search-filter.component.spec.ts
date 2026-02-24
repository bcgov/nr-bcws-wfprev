import { signal } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { MatOptionSelectionChange } from '@angular/material/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectFilterStateService } from 'src/app/services/project-filter-state.service';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { SharedService } from 'src/app/services/shared-service';
import { SearchFilterComponent } from './search-filter.component';

describe('SearchFilterComponent', () => {
  let component: SearchFilterComponent;
  let fixture: ComponentFixture<SearchFilterComponent>;
  let mockSharedService: jasmine.SpyObj<SharedService>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;
  let mockSharedCodeTableService: jasmine.SpyObj<SharedCodeTableService>;
  let mockCodeTablesSubject: Subject<any>;
  let mockProjectFilterStateService: jasmine.SpyObj<ProjectFilterStateService>;

  beforeEach(async () => {
    mockSharedService = jasmine.createSpyObj('SharedService', ['updateFilters']);
    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchFireCentres']);

    mockCodeTablesSubject = new Subject<any>();

    mockSharedCodeTableService = {
      get codeTables$() {
        return mockCodeTablesSubject.asObservable();
      }
    } as any;

    mockProjectFilterStateService = jasmine.createSpyObj('ProjectFilterStateService', ['update']);
    (mockProjectFilterStateService as any).filters = signal({});

    await TestBed.configureTestingModule({
      imports: [SearchFilterComponent, BrowserAnimationsModule],
      providers: [
        { provide: SharedService, useValue: mockSharedService },
        { provide: CodeTableServices, useValue: mockCodeTableService },
        { provide: SharedCodeTableService, useValue: mockSharedCodeTableService },
        { provide: ProjectFilterStateService, useValue: mockProjectFilterStateService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              url: []
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchFilterComponent);
    component = fixture.componentInstance;

    mockCodeTablesSubject.next({
      businessAreas: [{ programAreaName: 'Area A', programAreaGuid: 'guid-a' }],
      forestRegions: [{ orgUnitName: 'Region 1', orgUnitId: 'r1' }],
      forestDistricts: [{ orgUnitName: 'District 1', orgUnitId: 'd1', parentOrgUnitId: 'r1' }],
      activityCategoryCode: [{ description: 'Activity A', activityCategoryCode: 'a1' }],
      planFiscalStatusCode: [{ description: 'Approved', planFiscalStatusCode: 'P' }],
      wildfireOrgUnit: []
    });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should generate fiscal activity options', () => {
    component.generateFiscalYearOptions();
    expect(component.fiscalYearOptions.length).toBeGreaterThan(0);
    expect(component.fiscalYearOptions[0].label).toBe('All');
  });

  it('should call emitFilters when onSearch is triggered', () => {
    component.onSearch();
    expect(mockSharedService.updateFilters).toHaveBeenCalled();
  });

  it('should include "null" in fiscalYear query param when "All" is selected', () => {
    component.fiscalYearOptions = [
      { label: 'All', value: '__ALL__' },
      { label: '2025/26', value: '2025' },
      { label: '2024/25', value: '2024' },
      { label: '2023/24', value: '2023' }
    ];

    component.selectedFiscalYears = ['__ALL__'];

    component.emitFilters();

    expect(mockSharedService.updateFilters).toHaveBeenCalledWith(
      jasmine.objectContaining({
        fiscalYears: ['2025', '2024', '2023', 'null']
      })
    );
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

  it('should filter wildfireOrgUnit for only fire centres (FRC)', () => {
    mockCodeTablesSubject.next({
      wildfireOrgUnit: [
        {
          orgUnitName: 'Kamloops Fire Centre',
          orgUnitIdentifier: 'fc1',
          wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: 'FRC' }
        },
        {
          orgUnitName: 'Non-Fire Centre',
          orgUnitIdentifier: 'fc2',
          wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: 'OTHER' }
        }
      ]
    });

    fixture.detectChanges();

    const fireCentreLabels = component.fireCentreOptions.map(opt => opt.label);
    expect(fireCentreLabels).toContain('Kamloops Fire Centre');
    expect(fireCentreLabels).not.toContain('Non-Fire Centre');
  });

  it('should select current fiscal year and "No Year Assigned" on load', () => {
    // Simulate today’s date before or after April 1st
    const today = new Date();
    const expectedFiscalYearStart =
      today.getMonth() >= 3 ? today.getFullYear() : today.getFullYear() - 1;
    const expectedFiscalYearValue = expectedFiscalYearStart.toString();

    component.fiscalYearOptions = [
      { label: 'All', value: '__ALL__' },
      { label: `${expectedFiscalYearStart}/${(expectedFiscalYearStart + 1).toString().slice(-2)}`, value: expectedFiscalYearValue },
      { label: 'No Year Assigned', value: 'null' }
    ];

    spyOn(component, 'emitFilters');

    component.assignDefaultFiscalYear();

    expect(component.selectedFiscalYears).toContain(expectedFiscalYearValue);
    expect(component.selectedFiscalYears).toContain('null');
    expect(component.emitFilters).toHaveBeenCalled();
  });

  it('assignDefaultFiscalYear: selects current fiscal year and "No Year Assigned" when both exist and emits', () => {
    const today = new Date();
    const fyStart = today.getMonth() >= 3 ? today.getFullYear() : today.getFullYear() - 1;
    const fyValue = fyStart.toString();

    component.fiscalYearOptions = [
      { label: 'All', value: '__ALL__' },
      { label: `${fyStart}/${(fyStart + 1).toString().slice(-2)}`, value: fyValue },
      { label: 'No Year Assigned', value: 'null' }
    ];

    spyOn(component, 'emitFilters');

    component.assignDefaultFiscalYear();

    expect(component.selectedFiscalYears).toContain(fyValue);
    expect(component.selectedFiscalYears).toContain('null');
    expect(component.emitFilters).toHaveBeenCalled();
  });

  it('assignDefaultFiscalYear: selects only current fiscal year when "No Year Assigned" is missing', () => {
    const today = new Date();
    const fyStart = today.getMonth() >= 3 ? today.getFullYear() : today.getFullYear() - 1;
    const fyValue = fyStart.toString();

    component.fiscalYearOptions = [
      { label: `${fyStart}/${(fyStart + 1).toString().slice(-2)}`, value: fyValue }
    ];

    spyOn(component, 'emitFilters');

    component.assignDefaultFiscalYear();

    expect(component.selectedFiscalYears).toEqual([fyValue]);
    expect(component.emitFilters).toHaveBeenCalled();
  });

  it('assignDefaultFiscalYear: selects only "No Year Assigned" when current fiscal year is missing', () => {
    component.fiscalYearOptions = [
      { label: 'No Year Assigned', value: 'null' },
      { label: 'Some Other FY', value: '1999' }
    ];

    spyOn(component, 'emitFilters');

    component.assignDefaultFiscalYear();

    expect(component.selectedFiscalYears).toEqual(['null']);
    expect(component.emitFilters).toHaveBeenCalled();
  });

  it('assignDefaultFiscalYear: selects nothing when neither option exists and still emits', () => {
    component.fiscalYearOptions = [
      { label: 'Random FY', value: '1999' }
    ];

    spyOn(component, 'emitFilters');

    component.assignDefaultFiscalYear();

    expect(component.selectedFiscalYears).toEqual([]);
    expect(component.emitFilters).toHaveBeenCalled();
  });

  it('assignDefaultFiscalYear: does not emit when emit=false', () => {
    const today = new Date();
    const fyStart = today.getMonth() >= 3 ? today.getFullYear() : today.getFullYear() - 1;
    const fyValue = fyStart.toString();

    component.fiscalYearOptions = [
      { label: `${fyStart}/${(fyStart + 1).toString().slice(-2)}`, value: fyValue },
      { label: 'No Year Assigned', value: 'null' }
    ];

    spyOn(component, 'emitFilters');

    component.assignDefaultFiscalYear(false);

    expect(component.selectedFiscalYears).toEqual([fyValue, 'null']);
    expect(component.emitFilters).not.toHaveBeenCalled();
  });


});
