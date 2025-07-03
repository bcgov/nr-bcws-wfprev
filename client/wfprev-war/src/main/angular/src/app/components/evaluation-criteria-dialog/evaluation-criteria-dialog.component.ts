import { TextFieldModule } from '@angular/cdk/text-field';
import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { EvaluationCriteriaCodeModel, Project, WuiRiskClassCodeModel } from 'src/app/components/models';
import { CodeTableServices } from 'src/app/services/code-table-services';

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
  criteriaForm!: FormGroup;
  evaluationCriteriaCode: EvaluationCriteriaCodeModel[] = [];
  wuiRiskClassCode: WuiRiskClassCodeModel[] = [];
  mediumFilters: any[] = [];
  fineFilters: any[] = [];
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
    @Inject(MAT_DIALOG_DATA) public data: { project: Project }
  ){
  }

  ngOnInit(): void {
    if (this.data.project.projectTypeCode?.projectTypeCode === this.fuelManagement) {
      // Create the form here
      this.criteriaForm = this.fb.group({
        wuiRiskClassCode: [''],
        localWuiRiskClassCode: [''],
        localWuiRiskClassRationale: [''],
        mediumNotes: [''],
        fineNotes: [''],
        mediumSelections: [[]],
        mediumFilterComments: [''],
        fineSelections: [[]],
        fineFilterComments: [''],
        wuiRiskClass: [null],
        localWuiRiskClass: [null],
      });

      this.criteriaForm.get('wuiRiskClassCode')?.valueChanges.subscribe(() => {
        this.updateCoarseTotal();
      });
      this.criteriaForm.get('localWuiRiskClassCode')?.valueChanges.subscribe(() => {
        this.updateCoarseTotal();
      });


      for (const table of this.codeTables) {
        this.codeTableServices.fetchCodeTable(table.name).subscribe({
          next: (data) => {
            this.assignCodeTableData(table.embeddedKey, data);
          },
          error: (err) => {
            console.error(`Error fetching ${table.name}`, err);
            this.assignCodeTableData(table.embeddedKey, []); // Assign empty array on error
          },
        });
      }
    }
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
      .filter(f => this.selectedMedium.has(f.evaluationCriteriaGuid))
      .reduce((sum, f) => sum + (f.weightedRank ?? 0), 0);
  }

  calculateFineTotal() {
    this.fineTotal = this.fineFilters
      .filter(f => this.selectedFine.has(f.evaluationCriteriaGuid))
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

  onSave(){

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
}
