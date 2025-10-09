import { TextFieldModule } from '@angular/cdk/text-field';
import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { EvaluationCriteriaCodeModel, EvaluationCriteriaSummaryModel, Project, WuiRiskClassCodeModel } from 'src/app/components/models';
import { SlideToggleComponent } from 'src/app/components/shared/slide-toggle/slide-toggle.component';
import { TextareaComponent } from 'src/app/components/shared/textarea/textarea.component';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { EvaluationCriteriaSectionCodes, Messages, ModalMessages, ModalTitles, ProjectTypes } from 'src/app/utils/constants';
@Component({
  selector: 'wfprev-evaluation-criteria-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TextFieldModule, SlideToggleComponent, TextareaComponent],
  templateUrl: './evaluation-criteria-dialog.component.html',
  styleUrl: './evaluation-criteria-dialog.component.scss'
})
export class EvaluationCriteriaDialogComponent implements OnInit {
  codeTables = [
    { name: 'evaluationCriteriaCodes', embeddedKey: 'evaluationCriteriaCode' },
    { name: 'wuiRiskClassCodes', embeddedKey: 'wuiRiskClassCode' }
  ];
  messages = Messages;
  criteriaForm!: FormGroup;
  evaluationCriteriaCode: EvaluationCriteriaCodeModel[] = [];
  wuiRiskClassCode: WuiRiskClassCodeModel[] = [];
  mediumFilters: EvaluationCriteriaCodeModel[] = [];
  fineFilters: EvaluationCriteriaCodeModel[] = [];
  riskClassLocationFilters: EvaluationCriteriaCodeModel[] = [];
  selectedMedium: Set<string> = new Set();
  selectedFine: Set<string> = new Set();
  selectedCoarse: Set<string> = new Set();
  isCulturalPrescribedFire = false;
  isOutsideOfWuiOn = false;
  coarseTotal = 0;
  mediumTotal = 0;
  fineTotal = 0;
  isSaving = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialog: MatDialog,
    private readonly dialogRef: MatDialogRef<EvaluationCriteriaDialogComponent>,
    private readonly codeTableServices: CodeTableServices,
    private readonly projectService: ProjectService,
    private readonly snackbarService: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: {
      project: Project,
      evaluationCriteriaSummary?: EvaluationCriteriaSummaryModel;
    }
  ) {
  }

  ngOnInit(): void {
    this.initializeForm();
    const type = this.data.project.projectTypeCode?.projectTypeCode;
    this.isCulturalPrescribedFire = (type === ProjectTypes.CULTURAL_PRESCRIBED_FIRE);
    if (type) {
      this.setupValueChangeHandlers();
      this.loadCodeTablesAndPrefill();
    }

    if (this.isOutsideOfWuiOn) {
      this.criteriaForm.get('wuiRiskClassCode')?.disable();
      this.criteriaForm.get('localWuiRiskClassCode')?.disable();
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

        if (this.isOutsideOfWuiOn) {
          this.updateCoarseTotalFromCheckboxes();
        } else {
          this.updateCoarseTotal();
        }
      },
      error: (err) => {
        console.error('Error loading code tables', err);
      }
    });
  }

  assignCodeTableData(key: string, data: any): void {
    switch (key) {
      case 'evaluationCriteriaCode': {
        const allCriteria = data._embedded.evaluationCriteriaCode ?? [];
        const type = this.data.project.projectTypeCode?.projectTypeCode;

        this.evaluationCriteriaCode = allCriteria.filter(
          (c: EvaluationCriteriaCodeModel) => c.projectTypeCode === type
        );

        if (type === ProjectTypes.FUEL_MANAGEMENT) {
          this.mediumFilters = this.evaluationCriteriaCode
            .filter(c => (c.weightedRank ?? 0) >= 1)
            .sort((a, b) => (b.weightedRank ?? 0) - (a.weightedRank ?? 0));

          this.fineFilters = this.evaluationCriteriaCode
            .filter(c => (c.weightedRank ?? 0) < 1)
            .sort((a, b) => (b.weightedRank ?? 0) - (a.weightedRank ?? 0));
        }

        if (type === ProjectTypes.CULTURAL_PRESCRIBED_FIRE) {
          this.mediumFilters = this.evaluationCriteriaCode.filter(c => c.evalCriteriaSectCode === EvaluationCriteriaSectionCodes.BURN_DEVELOPMENT_FEASIBILITY);
          this.fineFilters = this.evaluationCriteriaCode.filter(c => c.evalCriteriaSectCode === EvaluationCriteriaSectionCodes.COLLECTIVE_IMPACT);
          this.riskClassLocationFilters = this.evaluationCriteriaCode
            .filter(c => c.evalCriteriaSectCode === EvaluationCriteriaSectionCodes.RISK_CLASS_LOCATION)
            .sort((a, b) => (a.criteriaLabel ?? '').localeCompare(b.criteriaLabel ?? ''))
        }
        break;
      }
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
    this.criteriaForm.markAsDirty();
  }

  toggleFine(guid: string, event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.selectedFine.add(guid);
    } else {
      this.selectedFine.delete(guid);
    }
    this.calculateFineTotal();
    this.criteriaForm.markAsDirty();
  }

  toggleCoarse(guid: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;

    if (checked) {
      this.selectedCoarse.add(guid);
    } else {
      this.selectedCoarse.delete(guid);
    }

    this.updateCoarseTotalFromCheckboxes();
    this.criteriaForm.markAsDirty();
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
    if (this.isSaving) return;
    if (!this.criteriaForm.valid) {
      console.warn("Form is invalid, not saving.");
      return;
    }
    this.isSaving = true;
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
        this.isSaving = false;
        this.dialogRef.close(result);
      },
      error: (err) => {
        console.error("Failed to create Evaluation Criteria Summary", err);
        this.snackbarService.open(
          Messages.evaluationCriteriaCreatedFailure,
          'OK',
          { duration: 5000, panelClass: 'snackbar-error' }
        );
        this.isSaving = false;
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
        this.isSaving = false;
        this.dialogRef.close(result);
      },
      error: (err) => {
        console.error("Failed to update Evaluation Criteria Summary", err);
        this.snackbarService.open(
          Messages.evaluationCriteriaUpdatedFailure,
          'OK',
          { duration: 5000, panelClass: 'snackbar-error' }
        );
        this.isSaving = false;
      }
    });
  }

  buildEvaluationCriteriaSummaryModel(existingSummary?: EvaluationCriteriaSummaryModel, isCreate = false): EvaluationCriteriaSummaryModel {
    const wuiRiskClassCodeValue = this.criteriaForm.get('wuiRiskClassCode')?.value;
    const localWuiRiskClassCodeValue = this.criteriaForm.get('localWuiRiskClassCode')?.value;

    const coarseSectionCode = this.isCulturalPrescribedFire ? EvaluationCriteriaSectionCodes.RISK_CLASS_LOCATION : EvaluationCriteriaSectionCodes.COARSE_FILTER;
    const mediumSectionCode = this.isCulturalPrescribedFire ? EvaluationCriteriaSectionCodes.BURN_DEVELOPMENT_FEASIBILITY : EvaluationCriteriaSectionCodes.MEDIUM_FILTER;
    const fineSectionCode = this.isCulturalPrescribedFire ? EvaluationCriteriaSectionCodes.COLLECTIVE_IMPACT : EvaluationCriteriaSectionCodes.FINE_FILTER;

    const coarseSection = existingSummary?.evaluationCriteriaSectionSummaries?.find(
      s => s.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode ===  coarseSectionCode
    );

    const mediumSection = existingSummary?.evaluationCriteriaSectionSummaries?.find(
      s => s.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode === mediumSectionCode
    );
    const fineSection = existingSummary?.evaluationCriteriaSectionSummaries?.find(
      s => s.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode === fineSectionCode
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
      isOutsideWuiInd: this.isOutsideOfWuiOn,
      totalFilterScore: this.coarseTotal + this.mediumTotal + this.fineTotal,
      lastUpdatedTimestamp: new Date().toISOString(),
      evaluationCriteriaSectionSummaries: [
        {
          evaluationCriteriaSectionSummaryGuid: isCreate ? undefined : coarseSection?.evaluationCriteriaSectionSummaryGuid,
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: coarseSectionCode },
          evaluationCriteriaSummaryGuid: existingSummary?.evaluationCriteriaSummaryGuid,
          filterSectionScore: this.coarseTotal,
          filterSectionComment: this.criteriaForm.get('localWuiRiskClassRationale')?.value,
          evaluationCriteriaSelected: this.riskClassLocationFilters.map(f => {
            const existing = coarseSection?.evaluationCriteriaSelected?.find(s => s.evaluationCriteriaGuid === f.evaluationCriteriaGuid);
            return {
              evaluationCriteriaSelectedGuid: isCreate ? undefined : existing?.evaluationCriteriaSelectedGuid,
              evaluationCriteriaGuid: f.evaluationCriteriaGuid,
              evaluationCriteriaSectionSummaryGuid: isCreate ? undefined : coarseSection?.evaluationCriteriaSectionSummaryGuid,
              isEvaluationCriteriaSelectedInd: this.selectedCoarse.has(f.evaluationCriteriaGuid ?? '')
            };
          })
        },
        {
          evaluationCriteriaSectionSummaryGuid: isCreate ? undefined : mediumSection?.evaluationCriteriaSectionSummaryGuid,
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: mediumSectionCode },
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
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: fineSectionCode },
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
        title: ModalTitles.CONFIRM_CANCEL_TITLE,
        message: ModalMessages.CONFIRM_CANCEL_MESSAGE
      },
      width: '600px',
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result) {
        this.dialogRef.close(); // Close the "Create New Project" dialog
      }
    });
  }

  formatCodeLabel(code: string | undefined): string {
    return code ? code.replaceAll(/_/g, ' ') : '';
  }

  prefillFromEvaluationCriteriaSummary(): void {
    const summary = this.data.evaluationCriteriaSummary;
    if (!summary) return;

    this.isOutsideOfWuiOn = summary.isOutsideWuiInd ?? false;
    if (this.isOutsideOfWuiOn) {
      this.criteriaForm.get('wuiRiskClassCode')?.disable();
      this.criteriaForm.get('localWuiRiskClassCode')?.disable();
    }

    this.setRiskClass('wuiRiskClassCode', summary.wuiRiskClassCode?.wuiRiskClassCode);
    this.setRiskClass('localWuiRiskClassCode', summary.localWuiRiskClassCode?.wuiRiskClassCode);

    this.criteriaForm.patchValue({
      localWuiRiskClassRationale: summary.localWuiRiskClassRationale ?? ''
    });

    for (const section of summary.evaluationCriteriaSectionSummaries ?? []) {
      const code = section.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode;
      if (!code) continue;

      if (code === EvaluationCriteriaSectionCodes.MEDIUM_FILTER || code === EvaluationCriteriaSectionCodes.BURN_DEVELOPMENT_FEASIBILITY) {
        this.handleSection(section, this.selectedMedium, 'mediumFilterComments');
        this.mediumTotal = section.filterSectionScore ?? 0;
      }

      if (code === EvaluationCriteriaSectionCodes.FINE_FILTER || code === EvaluationCriteriaSectionCodes.COLLECTIVE_IMPACT) {
        this.handleSection(section, this.selectedFine, 'fineFilterComments');
        this.fineTotal = section.filterSectionScore ?? 0;
      }
    }

    const rclSection = summary.evaluationCriteriaSectionSummaries?.find(
      s => s.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode === 'RCL'
    );

    this.selectedCoarse = new Set(
      rclSection?.evaluationCriteriaSelected
        ?.filter(sel => sel.isEvaluationCriteriaSelectedInd && sel.evaluationCriteriaGuid !== undefined)
        .map(sel => sel.evaluationCriteriaGuid as string)
    );

    this.updateCoarseTotal();
  }

  setRiskClass(controlName: string, code?: string): void {
    if (!code) return;
    const matching = this.wuiRiskClassCode.find(c => c.wuiRiskClassCode === code);
    this.criteriaForm.patchValue({ [controlName]: matching?.weightedRank });
  }

  handleSection(section: any, targetSet: Set<string>, commentControl: string): void {
    for (const selected of section.evaluationCriteriaSelected ?? []) {
      if (selected.isEvaluationCriteriaSelectedInd && selected.evaluationCriteriaGuid) {
        targetSet.add(selected.evaluationCriteriaGuid);
      }
    }
    this.criteriaForm.patchValue({
      [commentControl]: section.filterSectionComment ?? ''
    });
  }

  get sectionTitles() {
    const type = this.data.project.projectTypeCode?.projectTypeCode;
    const isFuel = type === ProjectTypes.FUEL_MANAGEMENT;

    const titles = {
      section1: isFuel ? 'Coarse Filters' : 'Risk Class & Location',
      section2: isFuel ? 'Medium Filters' : 'Burn Development and Feasibility',
      section3: isFuel ? 'Fine Filters' : 'Collective Impact',
    };

    return {
      ...titles,

      totalLabel: (section: keyof typeof titles) =>
        isFuel ? `Total Point Value for ${titles[section]}:` : 'Total Point Value:',

      commentLabel: (section: keyof typeof titles) => {
        if (section === 'section1') {
          return isFuel
            ? 'Local WUI Risk Class Rationale'
            : 'Additional Comments/Notes on risk class or outside of WUI rationale:';
        }
        return `Additional Comments/Notes on ${titles[section]}`;
      }
    };
  }

  toggleOutsideOfWui(isOutside: boolean): void {
    this.isOutsideOfWuiOn = isOutside;

    const wuiControl = this.criteriaForm.get('wuiRiskClassCode');
    const localWuiControl = this.criteriaForm.get('localWuiRiskClassCode');

    if (isOutside) {
      wuiControl?.disable();
      localWuiControl?.disable();
      wuiControl?.setValue('');
      localWuiControl?.setValue('');
      this.coarseTotal = 0;
    } else {
      wuiControl?.enable();
      localWuiControl?.enable();
      this.updateCoarseTotal(); // recalculate from dropdowns
    }

    this.selectedCoarse.clear(); // always clear coarse checkboxes
    this.updateCoarseTotalFromCheckboxes();
    this.criteriaForm.markAsDirty(); // optional: flag the form as dirty
  }



  updateCoarseTotalFromDropdowns(): void {
    const wui = this.criteriaForm.get('wuiRiskClassCode')?.value || 0;
    const local = this.criteriaForm.get('localWuiRiskClassCode')?.value || 0;
    this.coarseTotal = wui + local;
  }

  updateCoarseTotalFromCheckboxes(): void {
    this.coarseTotal = this.riskClassLocationFilters
      .filter(f => this.selectedCoarse.has(f.evaluationCriteriaGuid ?? ''))
      .reduce((sum, f) => sum + (f.weightedRank ?? 0), 0);
  }

  getControl(name: string): FormControl {
    return this.criteriaForm.get(name) as FormControl;
  }
}
