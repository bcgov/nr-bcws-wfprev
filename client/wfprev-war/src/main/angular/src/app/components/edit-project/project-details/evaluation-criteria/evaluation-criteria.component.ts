import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { EvaluationCriteriaDialogComponent } from 'src/app/components/evaluation-criteria-dialog/evaluation-criteria-dialog.component';
import { EvaluationCriteriaSummaryModel, Project } from 'src/app/components/models';
import { ExpansionIndicatorComponent } from 'src/app/components/shared/expansion-indicator/expansion-indicator.component';
import { ProjectService } from 'src/app/services/project-services';

@Component({
  selector: 'wfprev-evaluation-criteria',
  standalone: true,
  imports: [ExpansionIndicatorComponent, MatExpansionModule, MatIconModule, CommonModule],
  templateUrl: './evaluation-criteria.component.html',
  styleUrl: './evaluation-criteria.component.scss'
})
export class EvaluationCriteriaComponent implements OnChanges {
  @Input() project!: Project;

  evaluationCriteriaSummary: EvaluationCriteriaSummaryModel | null = null;

  constructor(
    private readonly dialog: MatDialog,
    private readonly projectService: ProjectService
  ) {}

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
          console.log('Fetched evaluation criteria summaries:', response);
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
    if (!summary) return 0;

    const coarse =
      summary.localWuiRiskClassCode?.weightedRank ??
      summary.wuiRiskClassCode?.weightedRank ??
      0;

    const medium = this.getSectionTotal(summary, 'MEDIUM_FLT');
    const fine = this.getSectionTotal(summary, 'FINE_FLT');

    return coarse + medium + fine;
  }

  formatCodeLabel(code: string | undefined): string {
    return code ? code.replace(/_/g, ' ') : '';
  }
}
