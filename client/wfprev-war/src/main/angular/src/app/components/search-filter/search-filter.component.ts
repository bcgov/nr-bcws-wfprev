import { Component, effect, OnInit } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { SharedService } from 'src/app/services/shared-service';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { MatOptionSelectionChange } from '@angular/material/core';
import { WildfireOrgUnitTypeCodes } from 'src/app/utils/constants';
import { ActivatedRoute } from '@angular/router';
import { ProjectFilterStateService } from 'src/app/services/project-filter-state.service';
import { ProjectFilter } from '../models';
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
    CommonModule
  ],
  templateUrl: './search-filter.component.html',
  styleUrl: './search-filter.component.scss'
})
export class SearchFilterComponent implements OnInit {
  rawForestDistricts: any[] = [];

  constructor(
    private readonly sharedCodeTableService: SharedCodeTableService,
    private readonly codeTableService: CodeTableServices,
    private sharedService: SharedService,
    private readonly projectFilterStateService: ProjectFilterStateService,
    private route: ActivatedRoute,
  ) {
    effect(() => {
      this.route.snapshot.url;
      const saved = this.projectFilterStateService.filters();
      if (saved) {
        this.searchText = saved.searchText ?? "";
        this.selectedProjectType = saved.projectTypeCode ?? [];
        this.selectedBusinessArea = saved.programAreaGuid ?? [];
        this.selectedFiscalYears = saved.fiscalYear ?? [];
        this.selectedActivity = saved.activityCategoryCode ?? [];
        this.selectedForestRegion =  saved.forestRegionOrgUnitId ?? [];
        this.selectedForestDistrict = saved.forestDistrictOrgUnitId ?? [];
        this.selectedFireCentre = saved.fireCentreOrgUnitId ?? [];
        this.selectedFiscalStatus = saved.planFiscalStatusCode ?? [];
      }

    });
  }

  searchText: string = '';
  searchTextChanged: Subject<string> = new Subject<string>();
  projectTypeOptions: { label: string, value: any }[] = [];
  businessAreaOptions: { label: string, value: any }[] = [];
  fiscalYearOptions: { label: string, value: string }[] = [];
  activityOptions: { label: string, value: any }[] = [];
  forestRegionOptions: { label: string, value: any }[] = [];
  forestDistrictOptions: { label: string, value: any }[] = [];
  fireCentreOptions: { label: string, value: string }[] = [];
  fiscalStatusOptions: { label: string, value: string }[] = [];

  selectedProjectType: string[] = [];
  selectedBusinessArea: string[] = [];
  selectedFiscalYears: string[] = [];
  selectedActivity: string[] = [];
  selectedForestRegion: string[] = [];
  selectedForestDistrict: string[] = [];
  selectedFireCentre: string[] = [];
  selectedFiscalStatus: string[] = [];
  noYearAssigned: string = 'No Year Assigned'

  ngOnInit(): void {
    const savedFilters = this.projectFilterStateService.filters();
    if (savedFilters) {
      this.searchText = savedFilters?.searchText ?? '';
      this.selectedProjectType = savedFilters?.projectTypeCode ?? [];
      this.selectedBusinessArea = savedFilters?.programAreaGuid ?? [];
      this.selectedFiscalYears = savedFilters?.fiscalYear ?? [];
      this.selectedActivity = savedFilters?.activityCategoryCode ?? [];
      this.selectedForestRegion = savedFilters?.forestRegionOrgUnitId ?? [];
      this.selectedForestDistrict = savedFilters?.forestDistrictOrgUnitId ?? [];
      this.selectedFireCentre = savedFilters?.fireCentreOrgUnitId ?? [];
      this.selectedFiscalStatus = savedFilters?.planFiscalStatusCode ?? [];
    }
    this.generateFiscalYearOptions();
    this.setupCodeTableSubscription();
    this.setupSearchDebounce();
  }

  emitFilters() {
    const sanitize = (arr: any[]) => arr.filter(v => v !== '__ALL__');

    // include 'null' in query param for 'ALL' in order to return projects with no fiscals attached
    const resolveFiscalYears = () => {
      if (this.selectedFiscalYears.includes('__ALL__')) {
        const allValues = this.fiscalYearOptions
          .map(opt => opt.value)
          .filter(v => v !== '__ALL__');

        return [...allValues, 'null'];
      }
      return sanitize(this.selectedFiscalYears);
    };


    this.projectFilterStateService.update({
      searchText: this.searchText,
      projectTypeCode: this.selectedProjectType,
      programAreaGuid: this.selectedBusinessArea,
      fiscalYear: this.selectedFiscalYears,
      activityCategoryCode: this.selectedActivity,
      forestRegionOrgUnitId: this.selectedForestRegion,
      forestDistrictOrgUnitId: this.selectedForestDistrict,
      fireCentreOrgUnitId: this.selectedFireCentre,
      planFiscalStatusCode: this.selectedFiscalStatus
    });
    const filterPrarameters: ProjectFilter = {
      searchText: this.searchText,
      projectTypeCode: sanitize(this.selectedProjectType),
      programAreaGuid: sanitize(this.selectedBusinessArea),
      fiscalYear: resolveFiscalYears(),
      activityCategoryCode: sanitize(this.selectedActivity),
      forestRegionOrgUnitId: sanitize(this.selectedForestRegion),
      forestDistrictOrgUnitId: sanitize(this.selectedForestDistrict),
      fireCentreOrgUnitId: sanitize(this.selectedFireCentre),
      planFiscalStatusCode: sanitize(this.selectedFiscalStatus)
    }

    this.sharedService.updateFilters(filterPrarameters);
  }

  onSearch() {
    this.emitFilters();
  }

  onReset() {
    this.searchText = '';
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

  generateFiscalYearOptions(): void {
    const currentYear = new Date().getFullYear();
    const startYear = currentYear - 5;
    const endYear = currentYear + 5;
    const list = [];

    for (let year = endYear; year >= startYear; year--) {
      const nextYear = (year + 1).toString().slice(-2).padStart(2, '0');
      list.push({
        label: `${year}/${nextYear}`,
        value: year.toString()
      });
    }

    this.fiscalYearOptions = this.prependAllAndSortFiscalYears(list);
  }

  onForestRegionChange(): void {
    if (!this.selectedForestRegion.length) {
      this.forestDistrictOptions = this.prependAllAndSort(
        this.rawForestDistricts.map((item: any) => ({
          label: item.orgUnitName,
          value: item.orgUnitId
        }))
      );
      return;
    }

    const filtered = this.rawForestDistricts.filter((district: any) =>
      this.selectedForestRegion.map(String).includes(String(district.parentOrgUnitId))
    );

    this.forestDistrictOptions = this.prependAllAndSort(
      filtered.map((item: any) => ({
        label: item.orgUnitName,
        value: item.orgUnitId
      }))
    );

    this.selectedForestDistrict = this.selectedForestDistrict.filter(id =>
      this.forestDistrictOptions.some(opt => opt.value === id)
    );
  }

  prependAllAndSort(options: { label: string, value: any }[]): { label: string, value: any }[] {
    const sorted = [...options].sort((a, b) => a.label.localeCompare(b.label));
    return [{ label: 'All', value: '__ALL__' }, ...sorted];
  }

  prependAllAndSortFiscalYears(options: { label: string, value: any }[]): { label: string, value: any }[] {
    const sorted = [...options].sort((a, b) => a.label.localeCompare(b.label));
    return [
      { label: 'All', value: '__ALL__' },
      ...sorted,
      { label: this.noYearAssigned, value: 'null' }
    ];
  }

  onOptionToggled(event: any, model: keyof SearchFilterComponent, options: { value: any }[]) {
    const allOptionValue = '__ALL__';
    let selected = (this[model] as any[]) || [];

    const allValues = options
      .map(o => o.value)
      .filter(v => v !== allOptionValue);

    if (selected.includes(allOptionValue)) {
      (this[model] as any[]) = [allOptionValue, ...allValues];
    } else if (event?.source?.value === allOptionValue) {
      (this[model] as any[]) = [];
    } else {
      selected = selected.filter(v => v !== allOptionValue);
      if (selected.length === allValues.length) {
        (this[model] as any[]) = [allOptionValue, ...allValues];
      } else {
        (this[model] as any[]) = selected;
      }
    }
    this.emitFilters();
  }

  syncAllWithItemToggle(
    event: MatOptionSelectionChange,
    value: string,
    model: keyof SearchFilterComponent,
    options: { value: string }[]
  ) {
    if (!event.isUserInput) return;
    const allOptionValue = '__ALL__';
    const allIndividualValues = options
      .map(o => o.value)
      .filter(v => v !== allOptionValue);

    const currentSelected = [...(this[model] as string[])];

    // Case 1: User deselects "All" => clear all
    if (value === allOptionValue && !event.source.selected) {
      setTimeout(() => {
        (this[model] as string[]) = [];
        this.emitFilters();
      }, 0);
      return;
    }

    // Case 2: User deselects individual item while "All" is selected => remove both
    const isUncheckingIndividual = value !== allOptionValue && !event.source.selected;
    if (isUncheckingIndividual && currentSelected.includes(allOptionValue)) {
      setTimeout(() => {
        (this[model] as string[]) = currentSelected.filter(v => v !== allOptionValue && v !== value);
        this.emitFilters();
      }, 0);
      return;
    }

    // Case 3: If all individual items are selected, "ALL" should also be selected
    if (value !== allOptionValue && event.source.selected) {
      const updated = new Set([...currentSelected, value]);
      const hasAllIndividuals = allIndividualValues.every(v => updated.has(v));
      if (hasAllIndividuals && !updated.has(allOptionValue)) {
        setTimeout(() => {
          (this[model] as string[]) = [allOptionValue, ...allIndividualValues];
          this.emitFilters();
        }, 0);
        return;
      }
    }

    //Case 4: User selects "All" => select all
    if (value === allOptionValue && event.source.selected) {
      setTimeout(() => {
          (this[model] as string[]) = [allOptionValue, ...allIndividualValues];
          this.emitFilters();
        }, 0);
    }

  }


  setupCodeTableSubscription(): void {
    this.sharedCodeTableService.codeTables$.subscribe((tables) => {
      if (!tables) return;
      this.projectTypeOptions = this.prependAllAndSort(
        (tables.projectTypeCode ?? []).map((item: any) => ({
          label: item.description,
          value: item.projectTypeCode
        }))
      );

      this.businessAreaOptions = this.prependAllAndSort(
        (tables.businessAreas ?? []).map((item: any) => ({
          label: item.programAreaName,
          value: item.programAreaGuid
        }))
      );

      this.forestRegionOptions = this.prependAllAndSort(
        (tables.forestRegions ?? []).map((item: any) => ({
          label: item.orgUnitName,
          value: item.orgUnitId
        }))
      );

      this.rawForestDistricts = tables.forestDistricts ?? [];

      this.forestDistrictOptions = this.prependAllAndSort(
        this.rawForestDistricts.map((item: any) => ({
          label: item.orgUnitName,
          value: item.orgUnitId
        }))
      );

      this.activityOptions = this.prependAllAndSort(
        (tables.activityCategoryCode ?? []).map((item: any) => ({
          label: item.description,
          value: item.activityCategoryCode
        }))
      );

      this.fiscalStatusOptions = this.prependAllAndSort(
        (tables.planFiscalStatusCode ?? []).map((item: any) => ({
          label: item.description,
          value: item.planFiscalStatusCode
        }))
      );

      this.fireCentreOptions = this.prependAllAndSort(
        (tables.wildfireOrgUnit ?? [])
          .filter((item: any) => item.wildfireOrgUnitTypeCode?.wildfireOrgUnitTypeCode === WildfireOrgUnitTypeCodes.FIRE_CENTRE)
          .map((item: any) => ({
            label: item.orgUnitName,
            value: item.orgUnitIdentifier
          }))
      );

      this.assignDefaultFiscalYear(false);
    });
  }

  setupSearchDebounce(): void {
    this.searchTextChanged
      .pipe(debounceTime(3000)) // 3s debounce time
      .subscribe((value: string) => {
        this.searchText = value;
        this.projectFilterStateService.update({
          searchText: value
        });
        this.onSearch();
      });
    this.onSearch();
  }

  clearSearch(): void {
    this.searchText = '';
    this.projectFilterStateService.update({
      searchText: this.searchText
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
