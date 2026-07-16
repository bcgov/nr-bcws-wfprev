import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { YearEndModel, YearEndActivityViewModel } from '../../models';
import { ProjectService } from 'src/app/services/project-services';
import { forkJoin } from 'rxjs';
import { YearEndSummaryActivityListComponent } from './year-end-summary-activity-list/year-end-summary-activity-list.component';
import { TokenService } from 'src/app/services/token.service';

@Component({
  selector: 'wfprev-year-end-summary',
  imports: [CommonModule, YearEndSummaryActivityListComponent],
  templateUrl: './year-end-summary.component.html',
  styleUrl: './year-end-summary.component.scss',
  standalone: true
})
export class YearEndSummaryComponent implements OnInit {
  @Input() projectGuid: string = '';
  @Input() fiscalGuid: string = '';

  summary: YearEndModel | undefined;

  constructor(private readonly projectService: ProjectService, private readonly tokenService: TokenService) { }

  ngOnInit() {
    this.populateYearEndSummary();
  }

  populateYearEndSummary() {
    if (this.projectGuid && this.fiscalGuid) {
      forkJoin({
        closeouts: this.projectService.getAllFiscalCloseouts(this.projectGuid, this.fiscalGuid),
        projectFiscal: this.projectService.getProjectFiscalByProjectPlanFiscalGuid(this.projectGuid, this.fiscalGuid),
        activities: this.projectService.getFiscalActivities(this.projectGuid, this.fiscalGuid)
      }).subscribe({
        next: ({ closeouts, projectFiscal, activities }) => {
          const activityList = activities?._embedded?.activities ?? [];
          activityList.sort((a: any, b: any) => {
            const nameA = (a.activityName || '').toLowerCase();
            const nameB = (b.activityName || '').toLowerCase();
            return nameA.localeCompare(nameB);
          });
          this.summary = {
            closeout: closeouts?._embedded?.fiscalCloseouts[0] ?? undefined,
            projectFiscal,
            activities: activityList.map((a: any): YearEndActivityViewModel => ({
              data: a,
              isExpanded: false
            }))
          };
        },
        error: (err) => {
          console.error('Error fetching year end summary:', err);
        }
      });
    }
  }

  get submittedByName(): string {
    return this.summary?.closeout?.submittedByName || this.tokenService.getUserFullName() || '';
  }
}