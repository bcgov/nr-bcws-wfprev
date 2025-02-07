import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute } from '@angular/router';
import { ProjectService } from 'src/app/services/project-services';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatExpansionModule } from '@angular/material/expansion';
import { ProjectFiscal } from 'src/app/components/models';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Messages } from 'src/app/utils/messages';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { MatMenuModule } from '@angular/material/menu';
import { Observable } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { CanComponentDeactivate } from 'src/app/services/util/can-deactive.guard';
import { ActivitiesComponent } from 'src/app/components/edit-project/activities/activities.component';

@Component({
  selector: 'app-project-fiscals',
  templateUrl: './project-fiscals.component.html',
  styleUrls: ['./project-fiscals.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTabsModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatExpansionModule,
    CurrencyPipe,
    MatMenuModule,
    ActivitiesComponent
  ]
})
export class ProjectFiscalsComponent implements OnInit, CanComponentDeactivate  {
  projectGuid = '';
  projectFiscals: any[] = [];
  fiscalForms: FormGroup[] = [];
  fiscalYears: string[] = [];
  selectedTabIndex = 0;
  currentFiscalGuid = '';
  messages = Messages;
  activityCategoryCode: any[] = [];
  planFiscalStatusCode: any[] = [];
  ancillaryFundingSourceCode: any[] = [];
  originalFiscalValues: any[] = []
  constructor(
    private route: ActivatedRoute,
    private projectService: ProjectService,
    private codeTableService: CodeTableServices,
    private readonly fb: FormBuilder,
    private readonly snackbarService: MatSnackBar,
    public readonly dialog: MatDialog,
    public cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCodeTables();
    this.generateFiscalYears();
    this.loadProjectFiscals();
  }
  

  canDeactivate(): Observable<boolean> | boolean {
    if (this.isFormDirty()) {
      const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        data: { indicator: 'confirm-unsave' },
        width: '500px',
      });
      return dialogRef.afterClosed();
    }
    return true;
  }

  isFormDirty(): boolean {
    return this.fiscalForms.some(form => form.dirty);
  }

  generateFiscalYears(): void {
    const currentYear = new Date().getFullYear();
    const startYear = currentYear - 5; // 5 years in the past
    const endYear = currentYear + 5;  // 5 years in the future
    this.fiscalYears = Array.from({ length: endYear - startYear + 1 }, (_, i) => {
      const year = startYear + i;
      return `${year}/${(year + 1).toString().slice(-2)}`;
    });
  }
  
  loadCodeTables(): void {
    const codeTables = [
      { name: 'activityCategoryCodes', embeddedKey: 'activityCategoryCode' },
      { name: 'planFiscalStatusCodes', embeddedKey: 'planFiscalStatusCode' },
      { name: 'ancillaryFundingSourceCodes', embeddedKey: 'ancillaryFundingSourceCode' },
    ];
  
    codeTables.forEach((table) => {
      this.fetchData(
        this.codeTableService.fetchCodeTable(table.name),
        (data) => this.assignCodeTableData(table.embeddedKey, data),
        `Error fetching ${table.name}`
      );
    });
  }

  assignCodeTableData(key: string, data: any): void {
    switch (key) {
      case 'activityCategoryCode':
        this.activityCategoryCode = data._embedded?.activityCategoryCode || [];
        break;
      case 'planFiscalStatusCode':
        this.planFiscalStatusCode = data._embedded?.planFiscalStatusCode || [];
        break;
      case 'ancillaryFundingSourceCode':
        this.ancillaryFundingSourceCode = data._embedded?.ancillaryFundingSourceCode || [];
        break;
    }
  }

  createFiscalForm(fiscal?: any): FormGroup {
    const form = this.fb.group({
      fiscalYear: [fiscal?.fiscalYear || '', [Validators.required]],
      projectFiscalName: [fiscal?.projectFiscalName || '', [Validators.required]],
      activityCategoryCode: [fiscal?.activityCategoryCode || '', [Validators.required]],
      proposalType: [fiscal?.proposalType || ''],
      planFiscalStatusCode: [fiscal?.planFiscalStatusCode || '', {}],
      fiscalPlannedProjectSizeHa: [fiscal?.fiscalPlannedProjectSizeHa || ''],
      fiscalCompletedSizeHa: [fiscal?.fiscalCompletedSizeHa ?? ''],
      resultsOpeningId: [fiscal?.resultsOpeningId || ''],
      firstNationsEngagementInd: [fiscal?.firstNationsEngagementInd || false],
      firstNationsDelivPartInd: [fiscal?.firstNationsDelivPartInd || false],
      firstNationsPartner: [fiscal?.firstNationsPartner || ''],
      projectFiscalDescription: [fiscal?.projectFiscalDescription || '', [Validators.required, Validators.maxLength(500)]],
      otherPartner: [fiscal?.otherPartner || ''],
      totalCostEstimateAmount: [fiscal?.totalCostEstimateAmount ?? ''],
      forecastAmount: [fiscal?.forecastAmount ?? ''],
      cfsProjectCode: [fiscal?.cfsProjectCode || ''],
      ancillaryFundingSourceGuid: [fiscal?.ancillaryFundingSourceGuid || ''],
      fiscalAncillaryFundAmount: [fiscal?.fiscalAncillaryFundAmount ?? ''],
      fiscalReportedSpendAmount: [fiscal?.fiscalReportedSpendAmount ?? ''],
      cfsActualSpend: [fiscal?.cfsActualSpend || ''],
      fiscalForecastAmount: [fiscal?.fiscalForecastAmount || ''],
      fiscalActualAmount: [fiscal?.fiscalActualAmount || ''],
      projectPlanFiscalGuid: [fiscal?.projectPlanFiscalGuid || ''],
      isApprovedInd: [fiscal?.isApprovedInd || false]
    });
    
    return form;
    
  }

  loadProjectFiscals(markFormsPristine: boolean = false): void {
    const previousTabIndex = this.selectedTabIndex;
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';
    if (!this.projectGuid) return;
  
    this.fetchData(
      this.projectService.getProjectFiscalsByProjectGuid(this.projectGuid),
      (data) => {
        this.projectFiscals = (data._embedded?.projectFiscals || []).map((fiscal: any) => ({
          ...fiscal,
          fiscalYearFormatted: `${fiscal.fiscalYear}/${(fiscal.fiscalYear + 1).toString().slice(-2)}`,
        })).sort((a: { fiscalYear: number }, b: { fiscalYear: number }) => a.fiscalYear - b.fiscalYear);
        
        this.originalFiscalValues = JSON.parse(JSON.stringify(this.projectFiscals));
        
        this.fiscalForms = this.projectFiscals.map((fiscal) => {
          const form = this.createFiscalForm(fiscal);
          if (markFormsPristine) {
            form.markAsPristine();
          }
          return form;
        });
        this.updateCurrentFiscalGuid();
        this.selectedTabIndex = previousTabIndex < this.projectFiscals.length ? previousTabIndex : 0;
      },
      'Error fetching project details'
    );
  }
  
  updateCurrentFiscalGuid(): void {
    if (this.projectFiscals.length > 0 && this.projectFiscals[this.selectedTabIndex]) {
      this.currentFiscalGuid = this.projectFiscals[this.selectedTabIndex].projectPlanFiscalGuid || '';
    } else {
      this.currentFiscalGuid = ''; // Reset if no fiscal is selected
    }
    this.cd.detectChanges();
  }

  onTabChange(index: number): void {
    this.selectedTabIndex = index;
    this.updateCurrentFiscalGuid();
  }

  addNewFiscal(): void {
    const newFiscalData = { fiscalYear: '', projectFiscalName: '', projectGuid: this.projectGuid };
    this.projectFiscals.push(newFiscalData);
    this.fiscalForms.push(this.createFiscalForm(newFiscalData));
    this.selectedTabIndex = this.projectFiscals.length - 1; // Navigate to the newly added tab
  }

  onCancelFiscal(index: number): void {
    if (!this.fiscalForms[index]) return; // Ensure form exists
  
    const isNewEntry = !this.projectFiscals[index]?.projectPlanFiscalGuid;
  
    if (isNewEntry) {
      // Case 1: New entry → Clear all fields
      this.fiscalForms[index].reset();
    } else {
      // Case 2: Existing entry → Revert to original API values
      const originalData = this.originalFiscalValues[index];
      this.fiscalForms[index].patchValue(originalData);
  
      // Mark form as pristine (not dirty)
      this.fiscalForms[index].markAsPristine();
      this.fiscalForms[index].markAsUntouched();
    }
  }

  fetchData<T>(fetchFn: Observable<T>, assignFn: (data: T) => void, errorMessage: string): void {
    fetchFn.subscribe({
      next: (data) => assignFn(data),
      error: (err) => {
        console.error(errorMessage, err);
        assignFn({} as T); // Assign default empty data
      },
    });
  }

  onSaveFiscal(index: number): void {
      const originalData = this.projectFiscals[index];
      const formData = this.fiscalForms[index]?.value;
      const updatedData = {
        ...originalData, // Include all original data and overwrite with form data
        ...formData,
      };
  
      const isUpdate = this.projectFiscals[index]?.projectPlanFiscalGuid;
      const projectFiscal: ProjectFiscal = {
        projectGuid: updatedData.projectGuid,
        projectPlanFiscalGuid: updatedData.projectPlanFiscalGuid,
        activityCategoryCode: updatedData.activityCategoryCode,
        fiscalYear: updatedData.fiscalYear ? parseInt(updatedData.fiscalYear, 10) : undefined,
        projectPlanStatusCode: isUpdate ? updatedData.projectPlanStatusCode : "ACTIVE",
        planFiscalStatusCode: updatedData.planFiscalStatusCode,
        projectFiscalName: updatedData.projectFiscalName,
        projectFiscalDescription: updatedData.projectFiscalDescription,
        businessAreaComment: updatedData.businessAreaComment,
        estimatedClwrrAllocAmount: updatedData.estimatedClwrrAllocAmount,
        fiscalAncillaryFundAmount: updatedData.fiscalAncillaryFundAmount,
        fiscalPlannedProjectSizeHa: updatedData.fiscalPlannedProjectSizeHa,
        fiscalPlannedCostPerHaAmt: updatedData.fiscalPlannedCostPerHaAmt,
        fiscalReportedSpendAmount: updatedData.fiscalReportedSpendAmount,
        fiscalActualAmount: updatedData.fiscalActualAmount,
        fiscalCompletedSizeHa: updatedData.fiscalCompletedSizeHa,
        fiscalActualCostPerHaAmt: updatedData.fiscalActualCostPerHaAmt,
        firstNationsDelivPartInd: updatedData.firstNationsDelivPartInd,
        firstNationsEngagementInd: updatedData.firstNationsEngagementInd,
        firstNationsPartner: updatedData.firstNationsPartner,
        resultsNumber: updatedData.resultsNumber,
        resultsOpeningId: updatedData.resultsOpeningId,
        resultsContactEmail: updatedData.resultsContactEmail,
        submittedByName: updatedData.submittedByName,
        submittedByUserGuid: updatedData.submittedByUserGuid,
        submittedByUserUserid: updatedData.submittedByUserUserid,
        submissionTimestamp: updatedData.submissionTimestamp,
        isApprovedInd: isUpdate ? updatedData.isApprovedInd : false,
        isDelayedInd: isUpdate ? updatedData.isDelayedInd : false,
        fiscalForecastAmount: updatedData.fiscalForecastAmount,
        totalCostEstimateAmount: updatedData.totalCostEstimateAmount,
        cfsProjectCode: updatedData.cfsProjectCode,
        ancillaryFundingSourceGuid: updatedData.ancillaryFundingSourceGuid,
        otherPartner: updatedData.otherPartner
      };
      if (isUpdate) {
        // update the existing fiscal
        this.projectService.updateProjectFiscal(this.projectGuid, updatedData.projectPlanFiscalGuid, projectFiscal).subscribe({
          next: (response) => {
            this.snackbarService.open(
              this.messages.projectFiscalUpdatedSuccess,
              'OK',
              { duration: 5000, panelClass: 'snackbar-success' },
            );
            this.loadProjectFiscals(true);
          },
          error: () => {
            this.snackbarService.open(
              this.messages.projectFiscalUpdatedFailure,
              'OK',
              { duration: 5000, panelClass: 'snackbar-error' }
            );
          }
        })
      }
      else {
        // create new fiscal
        this.projectService.createProjectFiscal(this.projectGuid, projectFiscal).subscribe({
          next: (response) => {
            this.snackbarService.open(
              this.messages.projectFiscalCreatedSuccess,
              'OK',
              { duration: 5000, panelClass: 'snackbar-success' },
            );
            this.loadProjectFiscals(true);
          },
          error: () =>{
              this.snackbarService.open(
                this.messages.projectFiscalCreatedFailure,
                'OK',
                { duration: 5000, panelClass: 'snackbar-error' }
              );
            }
        });
      }
  }

  deleteFiscalYear(form: any) {
    const formData = form.value;
    if (formData.projectPlanFiscalGuid) {
      const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        data: { indicator: 'confirm-delete' },
        width: '500px',
      });
  
      dialogRef.afterClosed().subscribe((confirmed: boolean) => {
        if (confirmed) {
          this.projectService.deleteProjectFiscalByProjectPlanFiscalGuid(this.projectGuid, formData.projectPlanFiscalGuid)
            .subscribe({
              next: () => {
                this.snackbarService.open(
                  this.messages.projectFiscalDeletedSuccess,
                  'OK',
                  { duration: 5000, panelClass: 'snackbar-success' }
                );
                this.loadProjectFiscals(true);
              },
              error: () => {
                this.snackbarService.open(
                  this.messages.projectFiscalDeletedFailure,
                  'OK',
                  { duration: 5000, panelClass: 'snackbar-error' }
                );
              }
            });
        }
      });
    } else{
      this.loadProjectFiscals(true);
    }
  }
  
  isUndeletable(form: any): boolean {
    return !!form?.value?.isApprovedInd;
  }
}
