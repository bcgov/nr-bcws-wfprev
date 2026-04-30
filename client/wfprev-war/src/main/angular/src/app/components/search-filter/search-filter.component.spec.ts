import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ProjectFilterStateService } from 'src/app/services/project-filter-state.service';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { SharedService } from 'src/app/services/shared-service';
import { SearchFilterComponent } from './search-filter.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { signal } from '@angular/core';

describe('SearchFilterComponent', () => {
  let component: SearchFilterComponent;
  let fixture: ComponentFixture<SearchFilterComponent>;

  let codeTables$: BehaviorSubject<any>;
  let sharedCodeTableService: jasmine.SpyObj<SharedCodeTableService>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let projectFilterStateService: jasmine.SpyObj<ProjectFilterStateService>;
  let filtersSignal: any;


  beforeEach(async () => {
    filtersSignal = signal<any>(null);

    codeTables$ = new BehaviorSubject<any>(null);

    sharedCodeTableService = jasmine.createSpyObj(
      'SharedCodeTableService',
      [],
      { codeTables$: codeTables$.asObservable() }
    );

    sharedService = jasmine.createSpyObj('SharedService', ['updateFilters']);

    projectFilterStateService = jasmine.createSpyObj(
      'ProjectFilterStateService',
      ['update']
    );

    (projectFilterStateService as any).filters = filtersSignal;

    await TestBed.configureTestingModule({
      imports: [
        SearchFilterComponent,
        NoopAnimationsModule
      ],
      providers: [
        { provide: SharedCodeTableService, useValue: sharedCodeTableService },
        { provide: SharedService, useValue: sharedService },
        { provide: ProjectFilterStateService, useValue: projectFilterStateService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { url: [] } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchFilterComponent);
    component = fixture.componentInstance;
  });

  const mockTables = {
    projectTypeCode: [{ description: 'PT1', projectTypeCode: '1' }],
    businessAreas: [{ programAreaName: 'BA1', programAreaGuid: '10' }],
    activityCategoryCode: [{ description: 'AC1', activityCategoryCode: '20' }],
    forestRegions: [
      { orgUnitName: 'Region1', orgUnitId: 'R1' },
      { orgUnitName: 'Region2', orgUnitId: 'R2' }
    ],
    forestDistricts: [
      { orgUnitName: 'District1', orgUnitId: 'D1', parentOrgUnitId: 'R1' },
      { orgUnitName: 'District1', orgUnitId: 'D2', parentOrgUnitId: 'R1' },
      { orgUnitName: 'District1', orgUnitId: 'D3', parentOrgUnitId: 'R2' }
    ],
    wildfireOrgUnit: [
      {
        orgUnitName: 'FireCentre1',
        orgUnitIdentifier: 'FC1',
        wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: 'FIRE_CENTRE' }
      }
    ],
    planFiscalStatusCode: [{ description: 'Active', planFiscalStatusCode: 'A' }]
  };

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize selects when code tables load', () => {
    codeTables$.next(mockTables);
    fixture.detectChanges();

    expect(component.selects.length).toBe(8);
    expect(component.selects.find(s => s.model === 'selectedProjectType')).toBeDefined();
  });

  it('should assign default fiscal year on init', () => {
    codeTables$.next(mockTables);
    fixture.detectChanges();

    expect(component.selectedFiscalYears.length).toBe(2);
  });

  it('should emit filters after search debounce', fakeAsync(() => {
    codeTables$.next(mockTables);
    fixture.detectChanges();

    component.searchControl.setValue('fire');
    
    tick(1100); 
    fixture.detectChanges();

    expect(sharedService.updateFilters).toHaveBeenCalled();
  }));

  it('should select ALL when all options selected', () => {
    codeTables$.next(mockTables);
    fixture.detectChanges();

    component.selectedProjectType = ['1'];
    component.onMultiSelectChange('selectedProjectType');

    expect(component.selectedProjectType).toContain(component.ALL);
  });

  it('should clear selection when ALL is deselected', () => {
    codeTables$.next(mockTables);
    fixture.detectChanges();

    component.selectedBusinessArea = [component.ALL, '10'];
    component['previousSelections'].set('selectedBusinessArea', [component.ALL, '10']);

    component.selectedBusinessArea = [];
    component.onMultiSelectChange('selectedBusinessArea');

    expect(component.selectedBusinessArea.length).toBe(0);
  });

  it('should filter forest districts by selected region', () => {
    codeTables$.next(mockTables);
    fixture.detectChanges();

    component.selectedForestRegion = ['R1'];
    component.onMultiSelectChange('selectedForestRegion');

    const districtSelect = component.selects.find(
      s => s.model === 'selectedForestDistrict'
    );

    expect(districtSelect?.options.length).toBe(2);
    expect(districtSelect?.options[0].value).toBe('D1');
    expect(districtSelect?.options[1].value).toBe('D2');
  });

  it('should reset all filters', () => {
    component.selectedProjectType = ['1'];
    component.searchControl.setValue('test');

    component.onReset();

    expect(component.searchControl.value).toBe('');
    expect(component.selectedProjectType.length).toBe(0);
  });

  it('should reset all filters', () => {
    component.searchControl.setValue('test');

    component.clearSearch();

    expect(component.searchControl.value).toBe('');
    expect(sharedService.updateFilters).toHaveBeenCalled();
  });

  it('should sanitize ALL before emitting filters', () => {
    component.selectedProjectType = [component.ALL, '1'];

    component.emitFilters();

    const call = sharedService.updateFilters.calls.mostRecent().args[0];
    expect(call.projectTypeCodes).toEqual(['1']);
  });

  it('should reset forest districts selection and options', () => {
    // ARRANGE
    (component as any).allForestDistricts = [
      { orgUnitName: 'District A', orgUnitId: 'D1' },
      { orgUnitName: 'District B', orgUnitId: 'D2' }
    ];

    component.selectedForestDistrict = ['D1'];
    (component as any).previousSelections.set('selectedForestDistrict', ['D1']);

    const districtSelect: any = {
      model: 'selectedForestDistrict',
      name: 'Forest District',
      allValue: component.ALL,
      options: [] as { label: string; value: string }[]
    };

    // ACT
    (component as any).resetForestDistricts(districtSelect);

    // ASSERT
    expect(component.selectedForestDistrict).toEqual([]);
    expect(
      (component as any).previousSelections.get('selectedForestDistrict')
    ).toEqual([]);

    expect(districtSelect.options).toEqual([
      { label: 'District A', value: 'D1' },
      { label: 'District B', value: 'D2' }
    ]);
  });

  it('should restore filters when saved filters exist (effect)', () => {
    // ARRANGE
    const savedFilters = {
      searchText: 'test',
      projectTypeCodes: ['PT1'],
      programAreaGuids: ['BA1'],
      fiscalYears: ['2024'],
      activityCategoryCodes: ['AC1'],
      forestRegionOrgUnitIds: ['R1'],
      forestDistrictOrgUnitIds: ['D1'],
      fireCentreOrgUnitIds: ['FC1'],
      planFiscalStatusCodes: ['ACTIVE']
    };

    // ACT → update signal (this triggers effect)
    filtersSignal.set(savedFilters);
    codeTables$.next(mockTables);
    fixture.detectChanges();

    // ASSERT
    expect(component.searchControl.value).toBe('test');
    expect(component.selectedProjectType).toEqual(['PT1']);
    expect(component.selectedBusinessArea).toEqual(['BA1']);
    expect(component.selectedFiscalYears).toEqual(['2024']);
    expect(component.selectedActivityCategory).toEqual(['AC1']);
    expect(component.selectedForestRegion).toEqual(['R1']);
    expect(component.selectedForestDistrict).toEqual(['D1']);
    expect(component.selectedFireCentre).toEqual(['FC1']);
    expect(component.selectedFiscalStatus).toEqual(['ACTIVE']);
  });

  it('should restore filters when saved filters exist (effect), empty value case', () => {
    // ARRANGE
    const savedFilters = {};

    // ACT → update signal (this triggers effect)
    filtersSignal.set(savedFilters);
    codeTables$.next(mockTables);
    fixture.detectChanges();

    // ASSERT
    expect(component.searchControl.value).toBe('');
    expect(component.selectedProjectType).toEqual([]);
    expect(component.selectedBusinessArea).toEqual([]);
    expect(component.selectedFiscalYears).toEqual([]);
    expect(component.selectedActivityCategory).toEqual([]);
    expect(component.selectedForestRegion).toEqual([]);
    expect(component.selectedForestDistrict).toEqual([]);
    expect(component.selectedFireCentre).toEqual([]);
    expect(component.selectedFiscalStatus).toEqual([]);
  });

  it('should not restore filters when saved filters are null', () => {

    filtersSignal.set(null);
    fixture.detectChanges();

    fixture = TestBed.createComponent(SearchFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.searchControl.value).toBe('');
    expect(component.selectedProjectType).toEqual([]);
  });

  it('ngOnInit should restore saved filters when savedFilters exists', () => {
    const savedFilters = {
      searchText: 'abc',
      projectTypeCodes: ['PT1'],
      programAreaGuids: ['BA1'],
      fiscalYears: ['2024'],
      activityCategoryCodes: ['AC1'],
      forestRegionOrgUnitIds: ['R1'],
      forestDistrictOrgUnitIds: ['D1'],
      fireCentreOrgUnitIds: ['FC1'],
      planFiscalStatusCodes: ['ACTIVE']
    };

    filtersSignal.set(savedFilters);

    codeTables$.next(mockTables);

    fixture = TestBed.createComponent(SearchFilterComponent);
    component = fixture.componentInstance;

    component.ngOnInit();

    expect(component.searchControl.value).toBe('abc');
    expect(component.selectedProjectType).toEqual(['PT1']);
    expect(component.selectedForestRegion).toEqual(['R1']);
    expect(component.selectedForestDistrict).toEqual(['D1']);
  });

  it('ngOnInit should restore saved filters when savedFilters exists, empty value case', () => {
    const savedFilters = {};

    filtersSignal.set(savedFilters);

    codeTables$.next(mockTables);

    fixture = TestBed.createComponent(SearchFilterComponent);
    component = fixture.componentInstance;

    component.ngOnInit();

    expect(component.searchControl.value).toBe('');
    expect(component.selectedProjectType).toEqual([]);
    expect(component.selectedForestRegion).toEqual([]);
    expect(component.selectedForestDistrict).toEqual([]);
  });

  it('resolveSelectionChange should remove ALL when current includes ALL', () => {
    const current = [component.ALL, '1'];
    const previous = [component.ALL, '1', '2'];
    const allOptions = ['1', '2'];

    const result = (component as any).resolveSelectionChange(
      current,
      previous,
      allOptions
    );

    expect(result).toEqual(['1']);
  });

  it('resolveSelectionChange should remove updated list only', () => {
    const current = ['1'];
    const previous = ['1', '2'];
    const allOptions = ['1', '2'];

    const result = (component as any).resolveSelectionChange(
      current,
      previous,
      allOptions
    );

    expect(result).toEqual(['1']);
  });

  it('updateForestDistricts should reset districts when no regions selected', () => {
    (component as any).allForestDistricts = [
      { orgUnitName: 'D1', orgUnitId: '1' },
      { orgUnitName: 'D2', orgUnitId: '2' }
    ];

    component.selects = [
      {
        model: 'selectedForestDistrict',
        name: 'Forest District',
        allValue: component.ALL,
        options: []
      }
    ];

    component.selectedForestDistrict = ['1'];

    (component as any).updateForestDistricts([]);

    expect(component.selectedForestDistrict).toEqual([]);

    const select = component.selects[0];
    expect(select.options.length).toBe(2);
  });

  it('syncSelectedDistricts should prepend ALL when all options selected', () => {
    component.selectedForestDistrict = ['1', '2'];

    const options = [
      { label: 'D1', value: '1' },
      { label: 'D2', value: '2' }
    ];

    (component as any).syncSelectedDistricts(options);

    expect(component.selectedForestDistrict).toEqual([
      component.ALL,
      '1',
      '2'
    ]);

    expect(
      (component as any).previousSelections.get('selectedForestDistrict')
    ).toEqual([component.ALL, '1', '2']);
  });
});
