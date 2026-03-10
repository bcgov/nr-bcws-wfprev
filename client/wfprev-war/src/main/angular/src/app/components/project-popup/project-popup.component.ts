import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { CodeTableKeys } from 'src/app/utils/constants';
import L from 'leaflet';
import { ActivityCategoryCodeModel, PlanFiscalStatusCodeModel, ProgramAreaModel, ProjectTypeCodeModel } from 'src/app/components/models';
import { getFiscalYearDisplay } from 'src/app/utils/tools';
import { FiscalCardComponent } from 'src/app/components/shared/fiscal-card/fiscal-card.component';
import { IconButtonComponent } from 'src/app/components/shared/icon-button/icon-button.component';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'wfprev-project-popup',
  standalone: true,
  imports: [CommonModule, FiscalCardComponent, IconButtonComponent, MatTooltip],
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
    this.codeTableService.getProjectTypeCodes().subscribe({
      next: data => { this.projectTypeCode = data; },
      error: err => console.error('Error fetching projectTypeCodes', err),
    });

    this.codeTableService.getProgramAreaCodes().subscribe({
      next: data => { this.programAreaCode = data; },
      error: err => console.error('Error fetching programAreaCodes', err),
    });

    this.codeTableService.getPlanFiscalStatusCodes().subscribe({
      next: data => { this.planFiscalStatusCode = data; },
      error: err => console.error('Error fetching planFiscalStatusCodes', err),
    });

    this.codeTableService.getActivityCategoryCodes().subscribe({
      next: data => { this.activityCategoryCodes = data; },
      error: err => console.error('Error fetching activityCategoryCodes', err),
    });
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
