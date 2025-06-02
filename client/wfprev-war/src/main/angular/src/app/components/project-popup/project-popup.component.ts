import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { CodeTableKeys } from 'src/app/utils/constants';
import L from 'leaflet';
import { ActivityCategoryCodeModel, PlanFiscalStatusCodeModel, ProgramAreaModel, ProjectTypeCodeModel } from 'src/app/components/models';
import { getFiscalYearDisplay } from 'src/app/utils/tools';

@Component({
  selector: 'app-project-popup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './project-popup.component.html',
  styleUrls: ['./project-popup.component.scss']
})
export class ProjectPopupComponent implements OnInit {
  @Input() project: any;
  @Input() map!: L.Map;

  projectTypeCode: ProjectTypeCodeModel[] = [];
  programAreaCode: ProgramAreaModel[] = [];
  planFiscalStatusCode: PlanFiscalStatusCodeModel[] = [];
  activityCategoryCodes: ActivityCategoryCodeModel[] = [];
  readonly CodeTableKeys = CodeTableKeys;
  getFiscalYearDisplay = getFiscalYearDisplay
  constructor(private readonly codeTableService: CodeTableServices) {}

  ngOnInit(): void {
    this.loadCodeTables();
  }

  loadCodeTables(): void {
    const codeTables = [
      { name: 'projectTypeCodes', embeddedKey: 'projectTypeCode' },
      { name: 'programAreaCodes', embeddedKey: 'programAreaCode' },
      { name: 'planFiscalStatusCodes', embeddedKey: 'planFiscalStatusCode'},
      { name: 'activityCategoryCodes', embeddedKey: 'activityCategoryCode'}
    ];

    codeTables.forEach(table => {
      this.codeTableService.fetchCodeTable(table.name).subscribe({
        next: (data) => this.assignCodeTableData(table.embeddedKey, data),
        error: (err) => {
          console.error(`Error fetching ${table.name}`, err);
          this.assignCodeTableData(table.embeddedKey, []);
        }
      });
    });
  }

  assignCodeTableData(key: string, data: any): void {
    const embedded = data?._embedded ?? {};

    switch (key) {
      case CodeTableKeys.PROJECT_TYPE_CODE:
        this.projectTypeCode = embedded.projectTypeCode ?? [];
        break;
      case CodeTableKeys.PROGRAM_AREA_CODE:
        this.programAreaCode = embedded.programArea ?? [];
        break;
      case CodeTableKeys.ACTIVITY_CATEGORY_CODE:
        this.activityCategoryCodes = embedded.activityCategoryCode ?? [];
        break;
      case CodeTableKeys.PLAN_FISCAL_STATUS_CODE:
        this.planFiscalStatusCode = embedded.planFiscalStatusCode ?? [];
        break;
    }
  }


  getCodeDescription(controlKey: string, value: any): string | null {
    switch (controlKey) {
      case CodeTableKeys.PROJECT_TYPE_CODE:
        return this.projectTypeCode.find(item => item.projectTypeCode === value)?.description ?? null;

      case CodeTableKeys.PROGRAM_AREA_GUID:
        return this.programAreaCode.find(item => item.programAreaGuid === value)?.programAreaName ?? null;

      case CodeTableKeys.ACTIVITY_CATEGORY_CODE:
        return this.activityCategoryCodes.find(item => item.activityCategoryCode === value)?.description ?? null;
      default:
        return null;
    }
  }

  formatCurrency(val: number | undefined): string {
    return val != null ? `$${val.toLocaleString()}` : 'N/A';
  }

  navigateToProject(): void {
    if (this.project?.projectGuid) {
      const url = `${window.location.origin}/edit-project?projectGuid=${this.project.projectGuid}`;
      window.open(url, '_blank');
    }
  }

  closePopup(): void {
    if (this.map) {
      this.map.closePopup();
    }
  }

  get sortedFiscals() {
    return this.project?.projectFiscals?.slice().sort(
      (a: { fiscalYear?: number }, b: { fiscalYear?: number }) =>
        (b.fiscalYear ?? 0) - (a.fiscalYear ?? 0)
    ) ?? [];
  }
}
