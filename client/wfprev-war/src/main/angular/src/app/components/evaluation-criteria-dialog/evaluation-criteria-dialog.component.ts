import { TextFieldModule } from '@angular/cdk/text-field';
import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { EvaluationCriteriaCodeModel, EvaluationCriteriaSummaryModel, Project, WuiRiskClassCodeModel } from 'src/app/components/models';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { Messages } from 'src/app/utils/constants';
@Component({
  selector: 'wfprev-evaluation-criteria-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TextFieldModule],
  templateUrl: './evaluation-criteria-dialog.component.html',
  styleUrl: './evaluation-criteria-dialog.component.scss'
})
export class EvaluationCriteriaDialogComponent {
  codeTables = [
    { name: 'evaluationCriteriaCodes', embeddedKey: 'evaluationCriteriaCode' },
    { name: 'wuiRiskClassCodes', embeddedKey: 'wuiRiskClassCode'}
  ];
  messages = Messages;
  criteriaForm!: FormGroup;
  evaluationCriteriaCode: EvaluationCriteriaCodeModel[] = [];
  wuiRiskClassCode: WuiRiskClassCodeModel[] = [];
  mediumFilters: EvaluationCriteriaCodeModel[] = [];
  fineFilters: EvaluationCriteriaCodeModel[] = [];
  selectedMedium: Set<string> = new Set();
  selectedFine: Set<string> = new Set();

  coarseTotal = 0;
  mediumTotal = 0;
  fineTotal = 0;

  fuelManagement = 'FUEL_MGMT'
  constructor(
    private readonly fb: FormBuilder,
    private readonly dialog: MatDialog,
    private readonly dialogRef: MatDialogRef<EvaluationCriteriaDialogComponent>,
    private readonly codeTableServices: CodeTableServices,
    private readonly projectService: ProjectService,
    private readonly snackbarService: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { 
      project: Project,
      evaluationCriteriaSummary?: EvaluationCriteriaSummaryModel;}
    ){
    }

  ngOnInit(): void {
    if (this.data.project.projectTypeCode?.projectTypeCode === this.fuelManagement) {
      this.initializeForm();
      this.setupValueChangeHandlers();
      this.loadCodeTablesAndPrefill();
    }
  }

  initializeForm(): void {
    this.criteriaForm = this.fb.group({
      wuiRiskClassCode: [''],
      localWuiRiskClassCode: [''],
      localWuiRiskClassRationale: ['', [Validators.maxLength(4000)]],
      mediumNotes: [''],
      fineNotes: [''],
      mediumSelections: [[]],
      mediumFilterComments: ['', [Validators.maxLength(4000)]],
      fineSelections: [[]],
      fineFilterComments: ['', [Validators.maxLength(4000)]],
      wuiRiskClass: [null],
      localWuiRiskClass: [null],
    });
  }

  setupValueChangeHandlers(): void {
    this.criteriaForm.get('wuiRiskClassCode')?.valueChanges.subscribe(() => {
      this.updateCoarseTotal();
    });
    this.criteriaForm.get('localWuiRiskClassCode')?.valueChanges.subscribe(() => {
      this.updateCoarseTotal();
    });
  }

  loadCodeTablesAndPrefill(): void {
    forkJoin({
      evaluationCriteria: this.codeTableServices.fetchCodeTable('evaluationCriteriaCodes'),
      wuiRiskClasses: this.codeTableServices.fetchCodeTable('wuiRiskClassCodes')
    }).subscribe({
      next: ({ evaluationCriteria, wuiRiskClasses }) => {
        this.assignCodeTableData('evaluationCriteriaCode', evaluationCriteria);
        this.assignCodeTableData('wuiRiskClassCode', wuiRiskClasses);

        this.prefillFromEvaluationCriteriaSummary();
      },
      error: (err) => {
        console.error('Error loading code tables', err);
      }
    });
  }

  assignCodeTableData(key: string, data: any): void {
    switch (key) {
      case 'evaluationCriteriaCode':
        this.evaluationCriteriaCode = (data._embedded.evaluationCriteriaCode ?? [])
          .filter((c:EvaluationCriteriaCodeModel) => c.projectTypeCode === this.fuelManagement);
        // split into Medium and Fine filters:
        this.mediumFilters = this.evaluationCriteriaCode.filter(c => (c.weightedRank ?? 0) >= 1);
        this.fineFilters = this.evaluationCriteriaCode.filter(c => (c.weightedRank ?? 0) < 1);
        break;
      
      case 'wuiRiskClassCode':
        this.wuiRiskClassCode = (data._embedded.wuiRiskClassRank ?? []);
        break;
      }
  }

  toggleMedium(guid: string, event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.selectedMedium.add(guid);
    } else {
      this.selectedMedium.delete(guid);
    }
    this.calculateMediumTotal();
  }

  toggleFine(guid: string, event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.selectedFine.add(guid);
    } else {
      this.selectedFine.delete(guid);
    }
    this.calculateFineTotal();
  }

  calculateMediumTotal() {
    this.mediumTotal = this.mediumFilters
      .filter(f => this.selectedMedium.has(f.evaluationCriteriaGuid ?? ''))
      .reduce((sum, f) => sum + (f.weightedRank ?? 0), 0);
  }

  calculateFineTotal() {
    this.fineTotal = this.fineFilters
      .filter(f => this.selectedFine.has(f.evaluationCriteriaGuid ?? ''))
      .reduce((sum, f) => sum + (f.weightedRank ?? 0), 0);
  }

  updateCoarseTotal() {
    const wuiValue = this.criteriaForm.get('wuiRiskClassCode')?.value;
    const localValue = this.criteriaForm.get('localWuiRiskClassCode')?.value;

    // If Local WUI Risk Class is selected, take that weightedRank
    if (localValue) {
      this.coarseTotal = Number(localValue);
    } else if (wuiValue) {
      this.coarseTotal = Number(wuiValue);
    } else {
      this.coarseTotal = 0;
    }
  }

  onSave(): void {
    if (!this.criteriaForm.valid) {
      console.warn("Form is invalid, not saving.");
      return;
    }

    const summaryGuid = this.data.evaluationCriteriaSummary?.evaluationCriteriaSummaryGuid;

    if (summaryGuid) {
      this.updateEvaluationCriteriaSummary(summaryGuid);
    } else {
      this.createEvaluationCriteriaSummary();
    }
  }

  createEvaluationCriteriaSummary(): void {
    const evaluationCriteriaSummary = this.buildEvaluationCriteriaSummaryModel();

    this.projectService.createEvaluationCriteriaSummary(
      this.data.project.projectGuid,
      evaluationCriteriaSummary
    ).subscribe({
      next: (result) => {
        this.snackbarService.open(
          Messages.evaluationCriteriaCreatedSuccess, 
          'OK',
          { duration: 5000, panelClass: 'snackbar-success' }
        );
        this.dialogRef.close(result);
        },
      error: (err) => {
        console.error("Failed to create Evaluation Criteria Summary", err);
        this.snackbarService.open(
          Messages.evaluationCriteriaCreatedFailure,
          'OK',
          { duration: 5000, panelClass: 'snackbar-error' }
        );
      }
    });
  }

  updateEvaluationCriteriaSummary(summaryGuid: string): void {
    const evaluationCriteriaSummary = this.buildEvaluationCriteriaSummaryModel(this.data.evaluationCriteriaSummary);

    this.projectService.updateEvaluationCriteriaSummary(
      this.data.project.projectGuid,
      summaryGuid,
      evaluationCriteriaSummary
    ).subscribe({
      next: (result) => {
        this.snackbarService.open(
          Messages.evaluationCriteriaUpdatedSuccess, 
          'OK',
          { duration: 5000, panelClass: 'snackbar-success' }
        );
        this.dialogRef.close(result);
      },
      error: (err) => {
        console.error("Failed to update Evaluation Criteria Summary", err);
        this.snackbarService.open(
          Messages.evaluationCriteriaUpdatedFailure,
          'OK',
          { duration: 5000, panelClass: 'snackbar-error' }
        );
      }
    });
  }

  buildEvaluationCriteriaSummaryModel(existingSummary?: EvaluationCriteriaSummaryModel, isCreate = false): EvaluationCriteriaSummaryModel {
    const wuiRiskClassCodeValue = this.criteriaForm.get('wuiRiskClassCode')?.value;
    const localWuiRiskClassCodeValue = this.criteriaForm.get('localWuiRiskClassCode')?.value;

    const mediumSection = existingSummary?.evaluationCriteriaSectionSummaries?.find(
      s => s.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode === 'MEDIUM_FLT'
    );
    const fineSection = existingSummary?.evaluationCriteriaSectionSummaries?.find(
      s => s.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode === 'FINE_FLT'
    );

    return {
      evaluationCriteriaSummaryGuid: existingSummary?.evaluationCriteriaSummaryGuid,
      projectGuid: this.data.project.projectGuid,
      wuiRiskClassCode: wuiRiskClassCodeValue
        ? {
            wuiRiskClassCode: this.wuiRiskClassCode.find(c => c.weightedRank == wuiRiskClassCodeValue)?.wuiRiskClassCode
          }
        : undefined,
      localWuiRiskClassCode: localWuiRiskClassCodeValue
        ? {
            wuiRiskClassCode: this.wuiRiskClassCode.find(c => c.weightedRank == localWuiRiskClassCodeValue)?.wuiRiskClassCode
          }
        : undefined,
      wuiRiskClassComment: '',
      localWuiRiskClassRationale: this.criteriaForm.get('localWuiRiskClassRationale')?.value,
      isOutsideWuiInd: false,
      totalFilterScore: this.coarseTotal + this.mediumTotal + this.fineTotal,
      evaluationCriteriaSectionSummaries: [
        {
          evaluationCriteriaSectionSummaryGuid: isCreate ? undefined : mediumSection?.evaluationCriteriaSectionSummaryGuid,
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'MEDIUM_FLT' },
          evaluationCriteriaSummaryGuid: existingSummary?.evaluationCriteriaSummaryGuid,
          filterSectionScore: this.mediumTotal,
          filterSectionComment: this.criteriaForm.get('mediumFilterComments')?.value,
          evaluationCriteriaSelected: this.mediumFilters.map(f => {
            const existing = mediumSection?.evaluationCriteriaSelected?.find(s => s.evaluationCriteriaGuid === f.evaluationCriteriaGuid);
            return {
              evaluationCriteriaSelectedGuid: isCreate ? undefined : existing?.evaluationCriteriaSelectedGuid,
              evaluationCriteriaGuid: f.evaluationCriteriaGuid,
              evaluationCriteriaSectionSummaryGuid: isCreate ? undefined : mediumSection?.evaluationCriteriaSectionSummaryGuid,
              isEvaluationCriteriaSelectedInd: this.selectedMedium.has(f.evaluationCriteriaGuid ?? '')
            };
          })
        },
        {
          evaluationCriteriaSectionSummaryGuid: isCreate ? undefined : fineSection?.evaluationCriteriaSectionSummaryGuid,
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'FINE_FLT' },
          evaluationCriteriaSummaryGuid: existingSummary?.evaluationCriteriaSummaryGuid,
          filterSectionScore: this.fineTotal,
          filterSectionComment: this.criteriaForm.get('fineFilterComments')?.value,
          evaluationCriteriaSelected: this.fineFilters.map(f => {
            const existing = fineSection?.evaluationCriteriaSelected?.find(
              s => s.evaluationCriteriaGuid === f.evaluationCriteriaGuid
            );
            return {
              evaluationCriteriaSelectedGuid: isCreate ? undefined : existing?.evaluationCriteriaSelectedGuid,
              evaluationCriteriaGuid: f.evaluationCriteriaGuid,
              evaluationCriteriaSectionSummaryGuid: isCreate ? undefined : fineSection?.evaluationCriteriaSectionSummaryGuid,
              isEvaluationCriteriaSelectedInd: this.selectedFine.has(f.evaluationCriteriaGuid ?? '')
            };
          })
        }
      ]
    };
  }



  onCancel(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        indicator: 'confirm-cancel',
      },
      width: '500px',
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result) {
        this.dialogRef.close(); // Close the "Create New Project" dialog
      }
    });
  }

  formatCodeLabel(code: string | undefined): string {
    return code ? code.replace(/_/g, ' ') : '';
  }

  prefillFromEvaluationCriteriaSummary(): void {
    const summary = this.data.evaluationCriteriaSummary;
    if (!summary) {
      return;
    }

    // WUI Risk Class
    if (summary.wuiRiskClassCode?.wuiRiskClassCode) {
      const matching = this.wuiRiskClassCode.find(
        c => c.wuiRiskClassCode === summary.wuiRiskClassCode?.wuiRiskClassCode
      );
      this.criteriaForm.patchValue({
        wuiRiskClassCode: matching?.weightedRank
      });
    }

    if (summary.localWuiRiskClassCode?.wuiRiskClassCode) {
      const matching = this.wuiRiskClassCode.find(
        c => c.wuiRiskClassCode === summary.localWuiRiskClassCode?.wuiRiskClassCode
      );
      this.criteriaForm.patchValue({
        localWuiRiskClassCode: matching?.weightedRank
      });
    }

    this.criteriaForm.patchValue({
      localWuiRiskClassRationale: summary.localWuiRiskClassRationale ?? ''
    });

    // Section Summaries
    for (const section of summary.evaluationCriteriaSectionSummaries ?? []) {
      const code = section.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode;

      if (code === 'MEDIUM_FLT') {
        // Check all selected criteria
        for (const selected of section.evaluationCriteriaSelected ?? []) {
          if (selected.isEvaluationCriteriaSelectedInd) {
            this.selectedMedium.add(selected.evaluationCriteriaGuid!);
          }
        }
        this.criteriaForm.patchValue({
          mediumFilterComments: section.filterSectionComment ?? ''
        });
        this.mediumTotal = section.filterSectionScore ?? 0;
      }

      if (code === 'FINE_FLT') {
        for (const selected of section.evaluationCriteriaSelected ?? []) {
          if (selected.isEvaluationCriteriaSelectedInd) {
            this.selectedFine.add(selected.evaluationCriteriaGuid!);
          }
        }
        this.criteriaForm.patchValue({
          fineFilterComments: section.filterSectionComment ?? ''
        });
        this.fineTotal = section.filterSectionScore ?? 0;
      }
    }

    // Update the coarse total at the end
    this.updateCoarseTotal();
  }


}
