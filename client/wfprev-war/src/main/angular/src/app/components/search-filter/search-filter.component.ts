import { Component, OnInit } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { CodeTableServices } from 'src/app/services/code-table-services';

@Component({
  selector: 'app-search-filter',
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
    private readonly codeTableService: CodeTableServices
  ) {}

  ngOnInit(): void {
    this.generateFiscalYearOptions();

    this.sharedCodeTableService.codeTables$.subscribe((tables) => {
      if (!tables) return;

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
    });

    this.codeTableService.fetchFireCentres().subscribe({
      next: (data) => {
        const features = data?.features ?? [];
        this.fireCentreOptions = this.prependAllAndSort(
          features.map((f: any) => ({
            label: f.properties.MOF_FIRE_CENTRE_NAME,
            value: f.properties.MOF_FIRE_CENTRE_ID
          }))
        );
      },
      error: () => {
        this.fireCentreOptions = [];
      }
    });
  }

  searchText: string = '';

  businessAreaOptions: { label: string, value: any }[] = [];
  fiscalYearOptions: { label: string, value: string }[] = [];
  activityOptions: { label: string, value: any }[] = [];
  forestRegionOptions: { label: string, value: any }[] = [];
  forestDistrictOptions: { label: string, value: any }[] = [];
  fireCentreOptions: { label: string, value: string }[] = [];
  fiscalStatusOptions: { label: string, value: string }[] = [];

  selectedBusinessArea: string[] = [];
  selectedFiscalYears: string[] = [];
  selectedActivity: string[] = [];
  selectedForestRegion: string[] = [];
  selectedForestDistrict: string[] = [];
  selectedFireCentre: string[] = [];
  selectedFiscalStatus: string[] = [];

  onSearch() {
    console.log('Searching for:', this.searchText);
  }

  onReset() {
    this.searchText = '';
    this.selectedBusinessArea = [];
    this.selectedFiscalYears = [];
    this.selectedActivity = [];
    this.selectedForestRegion = [];
    this.selectedForestDistrict = [];
    this.selectedFireCentre = [];
    this.selectedFiscalStatus = [];
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

    this.fiscalYearOptions = this.prependAllAndSort(list);
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

  // onSelectAll(model: keyof SearchFilterComponent, options: { label: string; value: any }[]): void {
  //   const values = options
  //     .filter(o => o.value !== '__ALL__')
  //     .map(o => o.value);
  
  //   (this[model] as any[]) = values;
  // }

  onSelectAll(event: any, model: keyof SearchFilterComponent, options: { value: any }[]) {
    const allOptionValue = '__ALL__';
    const selected = (this[model] as any[]) || [];
  
    const allOptionIndex = selected.indexOf(allOptionValue);
    const isAllNowSelected = allOptionIndex > -1;
  
    const allValues = [allOptionValue, ...options.map(o => o.value)];
  
    if (isAllNowSelected) {
      (this[model] as any[]) = allValues;
    } else {
      (this[model] as any[]) = [];
    }
  }
  
  
  
}
