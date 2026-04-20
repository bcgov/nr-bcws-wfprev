import { Component, effect, OnInit } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { FormControl, FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { SharedService } from 'src/app/services/shared-service';
import { debounceTime, distinctUntilChanged, filter, map, take, tap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { WildfireOrgUnitTypeCodes } from 'src/app/utils/constants';
import { ProjectFilterStateService } from 'src/app/services/project-filter-state.service';
import { ForestDistrictCodeModel, ProjectFilter } from '../models';
import { ReactiveFormsModule } from '@angular/forms';
import { DestroyRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

type MultiSelectKey = 'selectedProjectType' | 'selectedBusinessArea' | 'selectedForestRegion' | 'selectedForestDistrict' | 'selectedActivityCategory' | 'selectedFiscalStatus' | 'selectedFireCentre' | 'selectedFiscalYears';
type MultiSelectName = 'Project Type' | 'Business Area' | 'Forest Region' | 'Forest District' | 'Activity Category' | 'Fiscal Status' | 'Fire Centre' | 'Fiscal Year';

type Option = { label: string; value: string };
type MultiSelectConfig = {
  model: MultiSelectKey;
  name: MultiSelectName;
  allValue: string;
  options: Option[];
};

@Component({
  selector: 'wfprev-search-filter',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCheckboxModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './search-filter.component.html',
  styleUrl: './search-filter.component.scss'
})
export class SearchFilterComponent implements OnInit {

  public readonly ALL: string = '__ALL__';

  /** Search input field */
  public searchControl = new FormControl<string>('', { nonNullable: true });

  /** 8 dropdown models */
  public selectedProjectType: string[] = [];
  public selectedBusinessArea: string[] = [];
  public selectedFiscalYears: string[] = [];
  public selectedActivityCategory: string[] = [];
  public selectedForestRegion: string[] = [];
  public selectedForestDistrict: string[] = [];
  public selectedFireCentre: string[] = [];
  public selectedFiscalStatus: string[] = [];

  /** Previous selections to handle "Select All" logic */
  private previousSelections = new Map<MultiSelectKey, string[]>();

  /** Dropdown configuration */
  public selects: MultiSelectConfig[] = [];

  private readonly fiscalYears = this.generateFiscalYearOptions();

  protected allForestDistricts: ForestDistrictCodeModel[] = [];

  constructor(
    private readonly sharedCodeTableService: SharedCodeTableService,
    private sharedService: SharedService,
    private readonly projectFilterStateService: ProjectFilterStateService,
    private route: ActivatedRoute,
    private destroyRef: DestroyRef
  ) {}

  ngOnInit(): void {
    this.setupCodeTableSubscription().subscribe(() => {
      const savedFilters = this.projectFilterStateService.filters();

      if (savedFilters) {
        this.searchControl.setValue(savedFilters.searchText ?? '');
        this.selectedProjectType = savedFilters.projectTypeCodes ?? [];
        this.selectedBusinessArea = savedFilters.programAreaGuids ?? [];
        this.selectedFiscalYears = savedFilters.fiscalYears ?? [];
        this.selectedActivityCategory = savedFilters.activityCategoryCodes ?? [];
        this.selectedForestRegion = savedFilters.forestRegionOrgUnitIds ?? [];
        this.selectedForestDistrict = savedFilters.forestDistrictOrgUnitIds ?? [];
        this.updateForestDistricts(this.selectedForestRegion);
        this.selectedFireCentre = savedFilters.fireCentreOrgUnitIds ?? [];
        this.selectedFiscalStatus = savedFilters.planFiscalStatusCodes ?? [];

        this.previousSelections = new Map([
          ['selectedProjectType', this.selectedProjectType],
          ['selectedBusinessArea', this.selectedBusinessArea],
          ['selectedFiscalYears', this.selectedFiscalYears],
          ['selectedActivityCategory', this.selectedActivityCategory],
          ['selectedForestRegion', this.selectedForestRegion],
          ['selectedForestDistrict', this.selectedForestDistrict],
          ['selectedFireCentre', this.selectedFireCentre],
          ['selectedFiscalStatus', this.selectedFiscalStatus]
        ]);
      }

      this.setupSearch();
      this.emitFilters();
    });
  }

  generateFiscalYearOptions(): Option[] {
    const currentYear = new Date().getFullYear();
    const startYear = currentYear - 5;
    const endYear = currentYear + 5;
    const list: Option[] = [];

    for (let year = endYear; year >= startYear; year--) {
      const nextYear = (year + 1).toString().slice(-2).padStart(2, '0');
      list.push({
        label: `${year}/${nextYear}`,
        value: year.toString(),
      });
    }

    return [...list.sort((a, b) => a.label.localeCompare(b.label)), { label: 'No Year Assigned', value: 'null' }]
  }

  private mapToOptions<T>(
    items: T[] | null | undefined,
    labelKey: keyof T,
    valueKey: keyof T,
    sortByLabel = false
  ): Option[] {
    const options = (items ?? []).map(item => ({
      label: String(item[labelKey]),
      value: String(item[valueKey])
    })) as Option[];
    
    return sortByLabel
      ? options.sort((a, b) => a.label.localeCompare(b.label))
      : options;
  }

  private areAllTablesLoaded(tables: any): boolean {
    return !!(
      tables.projectTypeCode?.length &&
      tables.businessAreas?.length &&
      tables.activityCategoryCode?.length &&
      tables.forestRegions?.length &&
      tables.forestDistricts?.length &&
      tables.wildfireOrgUnit?.length &&
      tables.planFiscalStatusCode?.length
    );
  }

  private setupCodeTableSubscription(): Observable<void> {
    return this.sharedCodeTableService.codeTables$.pipe(
      filter(tables => !!tables),
      filter(tables => this.areAllTablesLoaded(tables)),
      take(1), // now SAFE
      tap(tables => {
        this.allForestDistricts = tables.forestDistricts;

        this.selects = [
          {
            model: 'selectedProjectType',
            name: 'Project Type',
            allValue: this.ALL,
            options: this.mapToOptions(
              tables.projectTypeCode,
              'description',
              'projectTypeCode',
              true
            )
          },
          {
            model: 'selectedBusinessArea',
            name: 'Business Area',
            allValue: this.ALL,
            options: this.mapToOptions(
              tables.businessAreas,
              'programAreaName',
              'programAreaGuid',
              true
            )
          },
          {
            model: 'selectedFiscalYears',
            name: 'Fiscal Year',
            allValue: this.ALL,
            options: this.fiscalYears
          },
          {
            model: 'selectedActivityCategory',
            name: 'Activity Category',
            allValue: this.ALL,
            options: this.mapToOptions(
              tables.activityCategoryCode,
              'description',
              'activityCategoryCode',
              true
            )
          },
          {
            model: 'selectedForestRegion',
            name: 'Forest Region',
            allValue: this.ALL,
            options: this.mapToOptions(
              tables.forestRegions,
              'orgUnitName',
              'orgUnitId',
              true
            )
          },
          {
            model: 'selectedForestDistrict',
            name: 'Forest District',
            allValue: this.ALL,
            options: this.mapToOptions(
              tables.forestDistricts,
              'orgUnitName',
              'orgUnitId',
              true
            )
          },
          {
            model: 'selectedFireCentre',
            name: 'Fire Centre',
            allValue: this.ALL,
            options: this.mapToOptions(
              tables.wildfireOrgUnit.filter(
                (item: any) =>
                  item.wildfireOrgUnitTypeCode?.wildfireOrgUnitTypeCode ===
                  WildfireOrgUnitTypeCodes.FIRE_CENTRE
              ),
              'orgUnitName',
              'orgUnitIdentifier',
              true
            )
          },
          {
            model: 'selectedFiscalStatus',
            name: 'Fiscal Status',
            allValue: this.ALL,
            options: this.mapToOptions(
              tables.planFiscalStatusCode,
              'description',
              'planFiscalStatusCode',
              false
            )
          }
        ];

        this.assignDefaultFiscalYear(false);
      }),
      map(() => void 0)
    );
  }

  setupSearch(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(1000),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(value => {
        this.emitFilters();
      });
  }

  /** Handler for multi-select changes */
  onMultiSelectChange(modelName: MultiSelectKey): void {
    const current = this.getCurrentValues(modelName);
    const previous = this.previousSelections.get(modelName) ?? [];
    const allOptions = this.getAllOptionValues(modelName);

    const updated = this.resolveSelectionChange(current, previous, allOptions);

    this.setModelValues(modelName, updated);
    this.previousSelections.set(modelName, [...updated]);

    if (modelName === 'selectedForestRegion') {
      this.updateForestDistricts(updated);
    }

    this.emitFilters();
  }

  private getCurrentValues(modelName: MultiSelectKey): string[] {
    return ((this as any)[modelName] ?? []).map(String);
  }

  private getAllOptionValues(modelName: MultiSelectKey): string[] {
    const select = this.selects.find(s => s.model === modelName)!;
    return select.options.map(o => String(o.value));
  }

  private resolveSelectionChange(
    current: string[],
    previous: string[],
    allOptions: string[]
  ): string[] {

    const selecting = current.length > previous.length;

    if (selecting) {
      if (
        current.length === allOptions.length ||
        (current.includes(this.ALL) && !previous.includes(this.ALL))
      ) {
        return [this.ALL, ...allOptions];
      }
      return [...current];
    }

    // deselecting
    if (!current.includes(this.ALL) && previous.includes(this.ALL)) {
      return [];
    }

    if (current.includes(this.ALL)) {
      return current.filter(v => v !== this.ALL);
    }

    return [...current];
  }

  private setModelValues(modelName: MultiSelectKey, values: string[]): void {
    (this as any)[modelName] = values;
  }

  private updateForestDistricts(selectedRegions: string[]): void {
    const districtSelect = this.selects.find(s => s.model === 'selectedForestDistrict');
    if (!districtSelect?.options) return;

    if (selectedRegions.length === 0) {
      this.resetForestDistricts(districtSelect);
      return;
    }
    const filteredDistricts = this.allForestDistricts.filter(d =>
      selectedRegions.includes(d.parentOrgUnitId ?? '')
    );

    districtSelect.options = this.mapToOptions(
      filteredDistricts,
      'orgUnitName',
      'orgUnitId',
      true
    );
    this.syncSelectedDistricts(districtSelect.options);
  }

  private resetForestDistricts(select: MultiSelectConfig): void {
    this.selectedForestDistrict = [];
    this.previousSelections.set('selectedForestDistrict', []);

    select.options = this.mapToOptions(
      this.allForestDistricts,
      'orgUnitName',
      'orgUnitId',
      true
    );
  }

  private syncSelectedDistricts(options: Option[]): void {
    const validValues = options.map(o => o.value);
    this.selectedForestDistrict =
      this.selectedForestDistrict.filter(v => validValues.includes(v));
    if (this.selectedForestDistrict.length === options.length) {
      this.selectedForestDistrict = [this.ALL, ...this.selectedForestDistrict];
    }

    this.previousSelections.set(
      'selectedForestDistrict',
      [...this.selectedForestDistrict]
    );
  }

  emitFilters() {
    const sanitize = (arr: any[]) => arr.filter(v => v !== this.ALL);

    this.projectFilterStateService.update({
      searchText: this.searchControl.value,
      projectTypeCodes: this.selectedProjectType,
      programAreaGuids: this.selectedBusinessArea,
      fiscalYears: this.selectedFiscalYears,
      activityCategoryCodes: this.selectedActivityCategory,
      forestRegionOrgUnitIds: this.selectedForestRegion,
      forestDistrictOrgUnitIds: this.selectedForestDistrict,
      fireCentreOrgUnitIds: this.selectedFireCentre,
      planFiscalStatusCodes: this.selectedFiscalStatus
    });
    const filterPrarameters: ProjectFilter = {
      searchText: this.searchControl.value,
      projectTypeCodes: sanitize(this.selectedProjectType),
      programAreaGuids: sanitize(this.selectedBusinessArea),
      fiscalYears: sanitize(this.selectedFiscalYears),
      activityCategoryCodes: sanitize(this.selectedActivityCategory),
      forestRegionOrgUnitIds: sanitize(this.selectedForestRegion),
      forestDistrictOrgUnitIds: sanitize(this.selectedForestDistrict),
      fireCentreOrgUnitIds: sanitize(this.selectedFireCentre),
      planFiscalStatusCodes: sanitize(this.selectedFiscalStatus)
    }

    this.sharedService.updateFilters(filterPrarameters);
  }

  onReset() {
    this.searchControl.setValue('');
    this.selectedProjectType = [];
    this.selectedBusinessArea = [];
    this.selectedFiscalYears = [];
    this.selectedActivityCategory = [];
    this.selectedForestRegion = [];
    this.selectedForestDistrict = [];
    this.selectedFireCentre = [];
    this.selectedFiscalStatus = [];
    this.assignDefaultFiscalYear(true);

    this.previousSelections = new Map([
      ['selectedProjectType', this.selectedProjectType],
      ['selectedBusinessArea', this.selectedBusinessArea],
      ['selectedFiscalYears', this.selectedFiscalYears],
      ['selectedActivityCategory', this.selectedActivityCategory],
      ['selectedForestRegion', this.selectedForestRegion],
      ['selectedForestDistrict', this.selectedForestDistrict],
      ['selectedFireCentre', this.selectedFireCentre],
      ['selectedFiscalStatus', this.selectedFiscalStatus]
    ]);
  }

  clearSearch(): void {
    this.searchControl.setValue('');
    this.emitFilters();
  }

  assignDefaultFiscalYear(emit: boolean = true): void {
    const today = new Date();
    // April has an index of 3
    const fiscalYearStart = today.getMonth() >= 3 ? today.getFullYear() : today.getFullYear() - 1;
    const fiscalYearValue = fiscalYearStart.toString();
    const options = this.selects.find(item => item.model === 'selectedFiscalYears')?.options || [];

    const currentFiscalExists = options.some(opt => opt.value === fiscalYearValue);
    const noYearAssignedExists = options.some(opt => opt.value === 'null');

    // automatically assign current fiscal year and 'No Year Assigned'
    this.selectedFiscalYears = [
      ...(currentFiscalExists ? [fiscalYearValue] : []),
      ...(noYearAssignedExists ? ['null'] : [])
    ];

    if (emit) this.emitFilters();
  }
}
