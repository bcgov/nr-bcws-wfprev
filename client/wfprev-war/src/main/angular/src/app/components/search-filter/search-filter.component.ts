import { Component, effect, OnInit } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormControl, FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { SharedService } from 'src/app/services/shared-service';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { WildfireOrgUnitTypeCodes } from 'src/app/utils/constants';
import { ActivatedRoute } from '@angular/router';
import { ProjectFilterStateService } from 'src/app/services/project-filter-state.service';
import { ProjectFilter } from '../models';
import { ReactiveFormsModule } from '@angular/forms';
import { DestroyRef } from '@angular/core';

export interface Option {
  label: string,
  value: string,
}

export interface SelectWithAllResult<T> {
  current: T[];
  previous: T[];
}

@Component({
  selector: 'wfprev-search-filter',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
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
  // rawForestDistricts: any[] = [];

  constructor(
    private readonly sharedCodeTableService: SharedCodeTableService,
    private sharedService: SharedService,
    private readonly projectFilterStateService: ProjectFilterStateService,
    private route: ActivatedRoute,
    private destroyRef: DestroyRef
  ) {
    effect(() => {
      this.route.snapshot.url;
      const saved = this.projectFilterStateService.filters();
      if (saved) {
        this.searchControl.setValue(saved.searchText ?? "");
        this.selectedProjectType = saved.projectTypeCodes ?? [];
        this.selectedBusinessArea = saved.programAreaGuids ?? [];
        this.selectedFiscalYears = saved.fiscalYears ?? [];
        this.selectedActivity = saved.activityCategoryCodes ?? [];
        this.selectedForestRegion = saved.forestRegionOrgUnitIds ?? [];
        this.selectedForestDistrict = saved.forestDistrictOrgUnitIds ?? [];
        this.selectedFireCentre = saved.fireCentreOrgUnitIds ?? [];
        this.selectedFiscalStatus = saved.planFiscalStatusCodes ?? [];
      }

    });
  }

  public searchControl = new FormControl<string>('', { nonNullable: true });

  public projectTypeOptions: Option[] = [];
  public businessAreaOptions: Option[] = [];
  public fiscalYearOptions: Option[] = [];
  public activityOptions: Option[] = [];
  public forestRegionOptions: Option[] = [];
  public forestDistrictOptions: Option[] = [];
  public fireCentreOptions: Option[] = [];
  public fiscalStatusOptions: Option[] = [];

  public selectedProjectType: string[] = [];
  public lastSelectedProjectType: string[] = [];

  public selectedBusinessArea: string[] = [];
  public lastSelectedBusinessArea: string[] = [];

  public selectedFiscalYears: string[] = [];
  public lastSelectedFiscalYears: string[] = [];

  public selectedActivity: string[] = [];
  public lastSelectedActivity: string[] = [];

  public selectedForestRegion: string[] = [];
  public lastSelectedForestRegion: string[] = [];

  public selectedForestDistrict: string[] = [];
  public lastSelectedForestDistrict: string[] = [];

  public selectedFireCentre: string[] = [];
  public lastSelectedFireCentre: string[] = [];

  public selectedFiscalStatus: string[] = [];
  public lastSelectedFiscalStatus: string[] = [];

  public readonly all: string = '__ALL__';

  ngOnInit(): void {
    const savedFilters = this.projectFilterStateService.filters();
    if (savedFilters) {
      this.searchControl.setValue(savedFilters?.searchText ?? '');
      this.selectedProjectType = savedFilters?.projectTypeCodes ?? [];
      this.selectedBusinessArea = savedFilters?.programAreaGuids ?? [];
      this.selectedFiscalYears = savedFilters?.fiscalYears ?? [];
      this.selectedActivity = savedFilters?.activityCategoryCodes ?? [];
      this.selectedForestRegion = savedFilters?.forestRegionOrgUnitIds ?? [];
      this.selectedForestDistrict = savedFilters?.forestDistrictOrgUnitIds ?? [];
      this.selectedFireCentre = savedFilters?.fireCentreOrgUnitIds ?? [];
      this.selectedFiscalStatus = savedFilters?.planFiscalStatusCodes ?? [];
    }
    this.generateFiscalYearOptions();
    this.setupCodeTableSubscription();
    this.setupSearch();
  }

  generateFiscalYearOptions(): void {
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

    this.fiscalYearOptions = [...list.sort((a, b) => a.label.localeCompare(b.label)), {label: 'No Year Assigned', value: 'null'}];
    this.assignDefaultFiscalYear(false);
  }

  private mapToOptions<T>(
    items: T[] | null | undefined,
    labelKey: keyof T,
    valueKey: keyof T,
    sortByLabel = false
  ): Option[] {
    const options = (items ?? []).map(item => ({
      label: String(item[labelKey]),
      value: item[valueKey]
    })) as Option[];

    return sortByLabel
      ? options.sort((a, b) => a.label.localeCompare(b.label))
      : options;
  }

  private setupCodeTableSubscription(): void {
    this.sharedCodeTableService.codeTables$
      .subscribe(tables => {
        if (!tables) return;

        this.projectTypeOptions = this.mapToOptions(
          tables.projectTypeCode,
          'description',
          'projectTypeCode',
          true
        );

        this.businessAreaOptions = this.mapToOptions(
          tables.businessAreas,
          'programAreaName',
          'programAreaGuid',
          true
        );

        this.forestRegionOptions = this.mapToOptions(
          tables.forestRegions,
          'orgUnitName',
          'orgUnitId',
          true
        );

        this.forestDistrictOptions = this.mapToOptions(
          tables.forestDistricts,
          'orgUnitName',
          'orgUnitId',
          true
        );

        this.activityOptions = this.mapToOptions(
          tables.activityCategoryCode,
          'description',
          'activityCategoryCode',
          true
        );

        this.fiscalStatusOptions = this.mapToOptions(
          tables.planFiscalStatusCode,
          'description',
          'planFiscalStatusCode',
          true
        );

        this.fireCentreOptions = this.mapToOptions(
          tables.wildfireOrgUnit?.filter(
            (item: any) =>
              item.wildfireOrgUnitTypeCode?.wildfireOrgUnitTypeCode ===
              WildfireOrgUnitTypeCodes.FIRE_CENTRE
          ),
          'orgUnitName',
          'orgUnitIdentifier',
          true
        );
      });
  }

  setupSearch(): void {
  this.searchControl.valueChanges
    .pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    )
    .subscribe(value => {
      this.projectFilterStateService.update({ searchText: value });
      this.onSearch();
    });
}

  private updateSelectionWithAll<T>(
    current: T[],
    previous: T[],
    allValue: T,
    allOptions: T[]
  ): SelectWithAllResult<T> {
    const hasAllNow = current.includes(allValue);
    const hadAllBefore = previous.includes(allValue);

    const isAdding = current.length > previous.length;
    const isRemoving = current.length < previous.length;

    const fullSelection = [allValue, ...allOptions];

    // ADDING ITEMS
    if (isAdding) {
      const allOptionsSelected =
        current.length === allOptions.length && !hasAllNow;

      const allJustSelected =
        hasAllNow && !hadAllBefore;

      const firstSelectionIsAll =
        hasAllNow && previous.length === 0;

      if (allOptionsSelected || allJustSelected || firstSelectionIsAll) {
        return {
          current: fullSelection,
          previous: fullSelection
        };
      }
    }

    // REMOVING ITEMS
    if (isRemoving) {
      const removedAll =
        !hasAllNow && hadAllBefore;

      if (removedAll) {
        return {
          current: [],
          previous: []
        };
      }

      if (hasAllNow) {
        const withoutAll = current.filter(v => v !== allValue);
        return {
          current: withoutAll,
          previous: withoutAll
        };
      }
    }

    // DEFAULT
    return {
      current: [...current],
      previous: [...current]
    };
  }

  onMultiSelectChange<T>(
    currentKey: keyof SearchFilterComponent,
    previousKey: keyof SearchFilterComponent,
    allValue: T,
    allOptions: T[]
  ): void {
    const result = this.updateSelectionWithAll(
      this[currentKey] as T[],
      this[previousKey] as T[],
      allValue,
      allOptions
    );

    (this[currentKey] as string[]) = result.current as string[];
    (this[previousKey] as string[]) = result.previous as string[];
  }

  onProjectTypeChange(): void {
    this.onMultiSelectChange(
      'selectedProjectType',
      'lastSelectedProjectType',
      this.all,
      this.projectTypeOptions.map(o => o.value)
    );

    this.emitFilters();
    console.log('onProjectTypeChange: selectedProjectType => ', this.selectedProjectType);
    console.log('onProjectTypeChange: lastSelectedProjectType => ', this.lastSelectedProjectType);
  }

  onBusinessAreaChange(): void {
    this.onMultiSelectChange(
      'selectedBusinessArea',
      'lastSelectedBusinessArea',
      this.all,
      this.businessAreaOptions.map(o => o.value)
    );

    this.emitFilters();
    console.log('onBusinessAreaChange: selectedBusinessArea => ', this.selectedBusinessArea);
    console.log('onBusinessAreaChange: lastSelectedBusinessArea => ', this.lastSelectedBusinessArea);
  }
  
  onFiscalYearsChange(): void {
    this.onMultiSelectChange(
      'selectedFiscalYears',
      'lastSelectedFiscalYears',
      this.all,
      this.fiscalYearOptions.map(o => o.value)
    );

    this.emitFilters();
    console.log('onFiscalYearChange: selectedFiscalYears => ', this.selectedFiscalYears);
    console.log('onFiscalYearChange: lastSelectedFiscalYears => ', this.lastSelectedFiscalYears);
  }

  onActivityCategoryChange(): void {
    this.onMultiSelectChange(
      'selectedActivity',
      'lastSelectedActivity',
      this.all,
      this.activityOptions.map(o => o.value)
    );

    this.emitFilters();
    console.log('onActivityCategoryChange: selectedActivity => ', this.selectedActivity);
    console.log('onActivityCategoryChange: lastSelectedActivity => ', this.lastSelectedActivity);
  }

  onForestRegionChange(): void {
    this.onMultiSelectChange(
      'selectedForestRegion',
      'lastSelectedForestRegion',
      this.all,
      this.forestRegionOptions.map(o => o.value)
    );

    this.emitFilters();
    console.log('onForestRegionChange: selectedForestRegion => ', this.selectedForestRegion);
    console.log('onForestRegionChange: lastSelectedForestRegion => ', this.lastSelectedForestRegion);
  }
  
  onForestDistrictCange(): void {
    this.onMultiSelectChange(
      'selectedForestDistrict',
      'lastSelectedForestDistrict',
      this.all,
      this.forestDistrictOptions.map(o => o.value)
    );

    this.emitFilters();
    console.log('onForestDistrictCange: selectedForestDistrict => ', this.selectedForestDistrict);
    console.log('onForestDistrictCange: lastSelectedForestDistrict => ', this.lastSelectedForestDistrict);
  }
  
  onFireCentreChange(): void {
    this.onMultiSelectChange(
      'selectedFireCentre',
      'lastSelectedFireCentre',
      this.all,
      this.fireCentreOptions.map(o => o.value)
    );

    this.emitFilters();
    console.log('onFireCentreChange: selectedFireCentre => ', this.selectedFireCentre);
    console.log('onFireCentreChange: lastSelectedFireCentre => ', this.lastSelectedFireCentre);
  }
  
  onFiscalStatusChange(): void {
    this.onMultiSelectChange(
      'selectedFiscalStatus',
      'lastSelectedFiscalStatus',
      this.all,
      this.fiscalStatusOptions.map(o => o.value)
    );

    this.emitFilters();
    console.log('onFiscalStatusChange: selectedFiscalStatus => ', this.selectedFiscalStatus);
    console.log('onFiscalStatusChange: lastSelectedFiscalStatus => ', this.lastSelectedFiscalStatus);
  }



  emitFilters() {
    const sanitize = (arr: any[]) => arr.filter(v => v !== this.all);

    // include 'null' in query param for 'ALL' in order to return projects with no fiscals attached
    const resolveFiscalYears = () => {
      if (this.selectedFiscalYears.includes(this.all)) {
        const allValues = this.fiscalYearOptions
          .map(opt => opt.value)
          .filter(v => v !== this.all);

        return [...allValues, 'null'];
      }
      return sanitize(this.selectedFiscalYears);
    };


    this.projectFilterStateService.update({
      searchText: this.searchControl.value,
      projectTypeCodes: this.selectedProjectType,
      programAreaGuids: this.selectedBusinessArea,
      fiscalYears: this.selectedFiscalYears,
      activityCategoryCodes: this.selectedActivity,
      forestRegionOrgUnitIds: this.selectedForestRegion,
      forestDistrictOrgUnitIds: this.selectedForestDistrict,
      fireCentreOrgUnitIds: this.selectedFireCentre,
      planFiscalStatusCodes: this.selectedFiscalStatus
    });
    const filterPrarameters: ProjectFilter = {
      searchText: this.searchControl.value,
      projectTypeCodes: sanitize(this.selectedProjectType),
      programAreaGuids: sanitize(this.selectedBusinessArea),
      fiscalYears: resolveFiscalYears(),
      activityCategoryCodes: sanitize(this.selectedActivity),
      forestRegionOrgUnitIds: sanitize(this.selectedForestRegion),
      forestDistrictOrgUnitIds: sanitize(this.selectedForestDistrict),
      fireCentreOrgUnitIds: sanitize(this.selectedFireCentre),
      planFiscalStatusCodes: sanitize(this.selectedFiscalStatus)
    }

    console.log("emitFilters => ", filterPrarameters);

    this.sharedService.updateFilters(filterPrarameters);
  }

  onSearch() {
    this.emitFilters();
  }

  onReset() {
    this.searchControl.setValue('');
    this.selectedProjectType = [];
    this.selectedBusinessArea = [];
    this.selectedFiscalYears = [];
    this.selectedActivity = [];
    this.selectedForestRegion = [];
    this.selectedForestDistrict = [];
    this.selectedFireCentre = [];
    this.selectedFiscalStatus = [];
    this.assignDefaultFiscalYear(true);
  }

  clearSearch(): void {
    this.searchControl.setValue('');
    this.projectFilterStateService.update({
      searchText: this.searchControl.value
    });
    this.emitFilters();
  }

  assignDefaultFiscalYear(emit: boolean = true): void {
    const today = new Date();
    // April has an index of 3
    const fiscalYearStart = today.getMonth() >= 3 ? today.getFullYear() : today.getFullYear() - 1;
    const fiscalYearValue = fiscalYearStart.toString();

    const currentFiscalExists = this.fiscalYearOptions.some(opt => opt.value === fiscalYearValue);
    const noYearAssignedExists = this.fiscalYearOptions.some(opt => opt.value === 'null');

    // automatically assign current fiscal year and 'No Year Assigned'
    this.selectedFiscalYears = [
      ...(currentFiscalExists ? [fiscalYearValue] : []),
      ...(noYearAssignedExists ? ['null'] : [])
    ];

    if (emit) this.emitFilters();
  }
}
