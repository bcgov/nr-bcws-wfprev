import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { EvaluationCriteriaDialogComponent } from 'src/app/components/evaluation-criteria-dialog/evaluation-criteria-dialog.component';
import { EvaluationCriteriaSummaryModel, Project } from 'src/app/components/models';
import { ExpansionIndicatorComponent } from 'src/app/components/shared/expansion-indicator/expansion-indicator.component';
import { TimestampComponent } from 'src/app/components/shared/timestamp/timestamp.component';
import { ProjectService } from 'src/app/services/project-services';
import { EvaluationCriteriaSectionCodes, ProjectTypes } from 'src/app/utils/constants';

@Component({
  selector: 'wfprev-evaluation-criteria',
  standalone: true,
  imports: [ExpansionIndicatorComponent, MatExpansionModule, MatIconModule, CommonModule, TimestampComponent],
  templateUrl: './evaluation-criteria.component.html',
  styleUrl: './evaluation-criteria.component.scss'
})
export class EvaluationCriteriaComponent implements OnChanges {
  @Input() project!: Project;

  evaluationCriteriaSummary: EvaluationCriteriaSummaryModel | null = null;

  constructor(
    private readonly dialog: MatDialog,
    private readonly projectService: ProjectService
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['project'] && this.project?.projectGuid) {
      this.loadEvaluationCriteriaSummaries();
    }
  }

  loadEvaluationCriteriaSummaries(): void {
    this.projectService
      .getEvaluationCriteriaSummaries(this.project.projectGuid)
      .subscribe({
        next: (response) => {
          this.evaluationCriteriaSummary = response?._embedded?.eval_criteria_summary[0] ?? null;
        },
        error: (err) => {
          console.error('Failed to fetch evaluation criteria summaries', err);
        }
      });
  }

  openEvaluationCriteriaPopUp(): void {


    const dialogRef = this.dialog.open(EvaluationCriteriaDialogComponent, {
      width: '776px',
      disableClose: true,
      hasBackdrop: true,
      data: {
        project: this.project,
        evaluationCriteriaSummary: this.evaluationCriteriaSummary
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadEvaluationCriteriaSummaries();
      }
    });
  }

  getSectionTotal(summary: any, sectionCode: string): number {
    const section = summary.evaluationCriteriaSectionSummaries?.find(
      (s: any) => s.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode === sectionCode
    );
    return section?.filterSectionScore ?? 0;
  }

  getSectionComment(summary: any, sectionCode: string): string | null {
    const section = summary.evaluationCriteriaSectionSummaries?.find(
      (s: any) => s.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode === sectionCode
    );

    const comment = section?.filterSectionComment;

    return comment && comment.trim() !== "" ? comment : null;
  }

  getCalculatedTotal(summary: any): number {
    const isRx = this.isPrescribedFire;
    if (!summary) return 0;

    const coarse = this.getCoarseTotal(summary);
    const medium = this.getSectionTotal(summary, isRx ? EvaluationCriteriaSectionCodes.BURN_DEVELOPMENT_FEASIBILITY : EvaluationCriteriaSectionCodes.MEDIUM_FILTER);
    const fine = this.getSectionTotal(summary, isRx ? EvaluationCriteriaSectionCodes.COLLECTIVE_IMPACT : EvaluationCriteriaSectionCodes.FINE_FILTER);

    return coarse + medium + fine;
  }
  getCoarseTotal(summary: any): number {
    const isRx = this.isPrescribedFire;

    if (isRx) {
      // Prescribed fire Outside of WUI → use RCL checkboxes total
      return this.getSectionTotal(summary, EvaluationCriteriaSectionCodes.RISK_CLASS_LOCATION);
    }

    // Otherwise use dropdowns
    return (
      summary?.localWuiRiskClassCode?.weightedRank ??
      summary?.wuiRiskClassCode?.weightedRank ??
      0
    );
  }
  formatCodeLabel(code: string | undefined): string {
    return code ? code.replace(/_/g, ' ') : '';
  }

  get mediumSectionCode(): string {
    return this.isPrescribedFire
      ? EvaluationCriteriaSectionCodes.BURN_DEVELOPMENT_FEASIBILITY
      : EvaluationCriteriaSectionCodes.MEDIUM_FILTER;
  }

  get fineSectionCode(): string {
    return this.isPrescribedFire
      ? EvaluationCriteriaSectionCodes.COLLECTIVE_IMPACT
      : EvaluationCriteriaSectionCodes.FINE_FILTER;
  }

  get isPrescribedFire(): boolean {
    return this.project?.projectTypeCode?.projectTypeCode === ProjectTypes.CULTURAL_PRESCRIBED_FIRE;
  }

  get isFuelManagement(): boolean {
    return this.project?.projectTypeCode?.projectTypeCode === ProjectTypes.FUEL_MANAGEMENT;
  }

  get evaluationLabels() {
    const isRx = this.isPrescribedFire;

    return {
      coarse: isRx ? 'Risk Class and Location' : 'Coarse Filter Total',
      medium: isRx ? 'Burn Development and Feasibility' : 'Medium Filter Total',
      fine: isRx ? 'Collective Impact' : 'Fine Filter Total',
      comments: {
        medium: isRx ? 'Comments' : 'Medium Filter Comments',
        fine: isRx ? 'Comments' : 'Fine Filter Comments',
        rationale: isRx ? 'Comments' : 'Local WUI Risk Class Rationale'
      }
    };
  }
}
