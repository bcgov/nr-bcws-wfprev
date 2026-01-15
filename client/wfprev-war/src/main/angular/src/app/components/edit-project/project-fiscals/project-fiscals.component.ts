import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatMenuModule } from '@angular/material/menu';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltip, MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { ActivitiesComponent } from 'src/app/components/edit-project/activities/activities.component';
import { FiscalMapComponent } from 'src/app/components/edit-project/fiscal-map/fiscal-map.component';
import { ActivityCategoryCodeModel, ProjectFiscal } from 'src/app/components/models';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { CanComponentDeactivate } from 'src/app/services/util/can-deactive.guard';
import { CodeTableKeys, EndorsementCode, FiscalStatuses, Messages, ModalMessages, ModalTitles, NumericLimits } from 'src/app/utils/constants';
import { ExpansionIndicatorComponent } from '../../shared/expansion-indicator/expansion-indicator.component';
import { IconButtonComponent } from 'src/app/components/shared/icon-button/icon-button.component';
import { SelectFieldComponent } from 'src/app/components/shared/select-field/select-field.component';
import { InputFieldComponent } from 'src/app/components/shared/input-field/input-field.component';
import { getUtcIsoTimestamp, PlanFiscalStatusIcons } from 'src/app/utils/tools';
import { DropdownButtonComponent } from 'src/app/components/shared/dropdown-button/dropdown-button.component';
import { StatusBadgeComponent } from 'src/app/components/shared/status-badge/status-badge.component';
import { EndorsementApprovalComponent } from 'src/app/components/edit-project/endorsement-approval/endorsement-approval.component';
import { TokenService } from 'src/app/services/token.service';
import { TimestampComponent } from 'src/app/components/shared/timestamp/timestamp.component';
import { TextareaComponent } from 'src/app/components/shared/textarea/textarea.component';
import { capitalizeFirstLetter } from 'src/app/utils';
import { PerformanceUpdatesComponent } from "../performance-update/performance-updates.component";

@Component({
  selector: 'wfprev-project-fiscals',
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
    ActivitiesComponent,
    FiscalMapComponent,
    MatTooltipModule,
    ExpansionIndicatorComponent,
    IconButtonComponent,
    SelectFieldComponent,
    MatTooltip,
    InputFieldComponent,
    DropdownButtonComponent,
    StatusBadgeComponent,
    EndorsementApprovalComponent,
    TimestampComponent,
    TextareaComponent,
    PerformanceUpdatesComponent
]
})
export class ProjectFiscalsComponent implements OnInit, CanComponentDeactivate {
  @Input() focusedFiscalId: string | null = null;
  @ViewChild(ActivitiesComponent) activitiesComponent!: ActivitiesComponent;
  @ViewChild('fiscalMapRef') fiscalMapComponent!: FiscalMapComponent;
  @Output() fiscalsUpdated = new EventEmitter<void>();
  currentUser: string = '';
  currentIdir: string = '';
  projectGuid = '';
  projectFiscals: any[] = [];
  fiscalForms: FormGroup[] = [];
  fiscalYears: string[] = [];
  selectedTabIndex = 0;
  currentFiscalGuid = '';
  messages = Messages;
  activityCategoryCode: ActivityCategoryCodeModel[] = [];
  planFiscalStatusCode: any[] = [];
  proposalTypeCode: any[] = [];
  originalFiscalValues: any[] = [];
  readonly CodeTableKeys = CodeTableKeys;
  readonly FiscalStatuses = FiscalStatuses;
  isSavingFiscal: boolean[] = [];

  constructor(
    private route: ActivatedRoute,
    private readonly router: Router,
    private projectService: ProjectService,
    private codeTableService: CodeTableServices,
    private readonly fb: FormBuilder,
    private readonly snackbarService: MatSnackBar,
    public readonly dialog: MatDialog,
    public cd: ChangeDetectorRef,
    private readonly tokenService: TokenService
  ) { }

  ngOnInit(): void {
    this.loadCodeTables();
    this.generateFiscalYears();

    // listen for changes to query params from user clicking browser back/forward button
    this.route.queryParamMap.subscribe(params => {
      const newGuid = params.get('fiscalGuid');
      // if a new fiscalGuid is present and differs from the current one, pdate the selected tab to route towards it
      if (newGuid && newGuid !== this.currentFiscalGuid) {
        const index = this.projectFiscals.findIndex(f => f.projectPlanFiscalGuid === newGuid);
        if (index >= 0) {
          this.selectedTabIndex = index;
          this.updateCurrentFiscalGuid();
        }
      }
    });

    this.loadProjectFiscals();
    const formattedName = this.tokenService.getUserFullName(true);
    if (formattedName) {
      this.currentUser = formattedName;
    }
    const idir = this.tokenService.getIdir();
    if (idir) {
      this.currentIdir = idir
    }
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.isFormDirty()) {
      const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        data: {
          indicator: 'confirm-unsave',
          title: ModalTitles.CONFIRM_UNSAVE_TITLE,
          message: ModalMessages.CONFIRM_UNSAVE_MESSAGE
        },
        width: '600px',
      });
      return dialogRef.afterClosed();
    }
    return true;
  }

  isFormDirty(): boolean {
    const fiscalDirty = this.fiscalForms.some(form => form.dirty);
    const activitiesDirty = this.activitiesComponent?.isFormDirty?.() ?? false;
    return fiscalDirty || activitiesDirty;
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
      { name: 'activityCategoryCodes', embeddedKey: CodeTableKeys.ACTIVITY_CATEGORY_CODE },
      { name: 'planFiscalStatusCodes', embeddedKey: CodeTableKeys.PLAN_FISCAL_STATUS_CODE },
      { name: 'proposalTypeCodes', embeddedKey: CodeTableKeys.PROPOSAL_TYPE_CODE }
    ];

    for (const table of codeTables) {
      this.fetchData(
        this.codeTableService.fetchCodeTable(table.name),
        (data) => this.assignCodeTableData(table.embeddedKey, data),
        `Error fetching ${table.name}`
      );
    }

    this.loadDropdownOptions();
  }

  assignCodeTableData(key: string, data: any): void {
    switch (key) {
      case CodeTableKeys.ACTIVITY_CATEGORY_CODE:
        this.activityCategoryCode = data._embedded?.activityCategoryCode ?? [];
        break;
      case CodeTableKeys.PLAN_FISCAL_STATUS_CODE:
        this.planFiscalStatusCode = data._embedded?.planFiscalStatusCode ?? [];
        break;
      case CodeTableKeys.PROPOSAL_TYPE_CODE:
        this.proposalTypeCode = data._embedded?.proposalTypeCode ?? [];
        break;
    }
  }

  loadDropdownOptions() {
    this.fiscalYears = this.sortArray(this.fiscalYears);
    this.activityCategoryCode = this.sortArray(this.activityCategoryCode, 'description');
    this.planFiscalStatusCode = this.sortArray(this.planFiscalStatusCode, 'description');
    this.proposalTypeCode = this.sortArray(this.proposalTypeCode, 'description');
  }

  sortArray<T>(array: T[], key?: keyof T): T[] {
    if (!array) return [];
    return key
      ? array.sort((a, b) => String(a[key]).localeCompare(String(b[key])))
      : array.sort((a, b) => String(a).localeCompare(String(b)));
  }

  createFiscalForm(fiscal?: any): FormGroup {
    const fiscalYear = (typeof fiscal?.fiscalYear === 'number' && fiscal.fiscalYear > 0)
      ? fiscal.fiscalYear
      : null;
    const form = this.fb.group({
      fiscalYear: [fiscalYear, [Validators.required]],
      projectFiscalName: [fiscal?.projectFiscalName ?? '', [Validators.required, Validators.maxLength(300)]],
      activityCategoryCode: [fiscal?.activityCategoryCode ?? '', [Validators.required]],
      proposalTypeCode: [fiscal?.proposalTypeCode ?? 'NEW', [Validators.required]],
      planFiscalStatusCode: [
        fiscal?.planFiscalStatusCode?.planFiscalStatusCode ?? 'DRAFT',
        [Validators.required]
      ],
      fiscalPlannedProjectSizeHa: [fiscal?.fiscalPlannedProjectSizeHa ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      fiscalCompletedSizeHa: [fiscal?.fiscalCompletedSizeHa ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      resultsOpeningId: [fiscal?.resultsOpeningId ?? '', [Validators.maxLength(11)]],
      firstNationsEngagementInd: [fiscal?.firstNationsEngagementInd ?? false],
      firstNationsDelivPartInd: [fiscal?.firstNationsDelivPartInd ?? false],
      firstNationsPartner: [fiscal?.firstNationsPartner ?? '', [Validators.maxLength(4000)]],
      projectFiscalDescription: [fiscal?.projectFiscalDescription ?? '', [Validators.required, Validators.maxLength(500)]],
      otherPartner: [fiscal?.otherPartner ?? '', [Validators.maxLength(4000)]],
      totalCostEstimateAmount: [fiscal?.totalCostEstimateAmount ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      forecastAmount: [fiscal?.forecastAmount ?? ''],
      cfsProjectCode: [fiscal?.cfsProjectCode ?? '', [Validators.maxLength(25)]],
      ancillaryFundingProvider: [fiscal?.ancillaryFundingProvider ?? '', [Validators.maxLength(100)]],
      fiscalAncillaryFundAmount: [fiscal?.fiscalAncillaryFundAmount ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      fiscalReportedSpendAmount: [fiscal?.fiscalReportedSpendAmount ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      cfsActualSpend: [fiscal?.cfsActualSpend ?? ''],
      fiscalForecastAmount: [fiscal?.fiscalForecastAmount ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      fiscalActualAmount: [fiscal?.fiscalActualAmount ?? '', [Validators.min(0), Validators.max(NumericLimits.MAX_NUMBER)]],
      projectPlanFiscalGuid: [fiscal?.projectPlanFiscalGuid ?? ''],
      isApprovedInd: [fiscal?.isApprovedInd ?? false]
    });
    return form;
  }


  loadProjectFiscals(markFormsPristine: boolean = false, newFiscalGuid?: string): void {
    const previousTabIndex = this.selectedTabIndex;
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') ?? '';
    if (!this.projectGuid) return;

    this.fetchData(
      this.projectService.getProjectFiscalsByProjectGuid(this.projectGuid),
      (data) => {
        this.projectFiscals = (data._embedded?.projectFiscals ?? []).map((fiscal: any) => ({
          ...fiscal,
          fiscalYearFormatted: `${fiscal.fiscalYear}/${(fiscal.fiscalYear + 1).toString().slice(-2)}`,
        }))
          .sort((a: any, b: any) => {
            // Sort by date then by name
            if (a.fiscalYear !== b.fiscalYear) {
              return b.fiscalYear - a.fiscalYear;
            }
            return (a.projectFiscalName ?? '').localeCompare(b.projectFiscalName ?? '', undefined);
          });

        this.originalFiscalValues = structuredClone(this.projectFiscals);

        this.fiscalForms = this.projectFiscals.map((fiscal) => {
          const form = this.createFiscalForm(fiscal);

          // Disable the entire form if status is COMPLETE or CANCELLED
          if (
            [this.FiscalStatuses.COMPLETE, this.FiscalStatuses.CANCELLED].includes(
              fiscal?.planFiscalStatusCode?.planFiscalStatusCode
            )
          ) {
            form.disable();
          }
          // Specifically disable the "Original Cost Estimate" field if status is not DRAFT
          if (fiscal?.planFiscalStatusCode?.planFiscalStatusCode !== this.FiscalStatuses.DRAFT) {
            form.get('totalCostEstimateAmount')?.disable();
          }
          // Mark form as pristine if requested
          if (markFormsPristine) {
            form.markAsPristine();
          }
          return form;
        });

        if (newFiscalGuid) {
          const index = this.projectFiscals.findIndex(f => f.projectPlanFiscalGuid === newFiscalGuid);
          if (index >= 0) this.selectedTabIndex = index;
        }

        // Find fiscal's index if it exists and set it as the active tab
        else if (this.focusedFiscalId) {
          const index = this.projectFiscals.findIndex(f => f.projectPlanFiscalGuid === this.focusedFiscalId);
          if (index !== -1) {
            this.selectedTabIndex = index;
          }
          // if no fiscal default to first tab or direct to previous index 
        } else {
          this.selectedTabIndex = previousTabIndex < this.projectFiscals.length ? previousTabIndex : 0;
        }
        this.updateCurrentFiscalGuid();
      },
      'Error fetching project details'
    );
  }

  updateCurrentFiscalGuid(): void {
    if (this.projectFiscals.length > 0 && this.projectFiscals[this.selectedTabIndex]) {
      this.currentFiscalGuid = this.projectFiscals[this.selectedTabIndex].projectPlanFiscalGuid ?? '';
    } else {
      this.currentFiscalGuid = ''; // Reset if no fiscal is selected
    }
    this.cd.detectChanges();
  }

  onTabChange(index: number): void {
    this.selectedTabIndex = index;
    this.updateCurrentFiscalGuid();

    const fiscalGuid = this.projectFiscals[index]?.projectPlanFiscalGuid;
    if (fiscalGuid) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { ...this.route.snapshot.queryParams, fiscalGuid },
        queryParamsHandling: 'merge'
      });
    }
  }

  get hasUnsavedFiscal(): boolean {
    return this.projectFiscals.some(fiscal => !fiscal.projectPlanFiscalGuid);
  }

  addNewFiscal(): void {
    this.currentFiscalGuid = '';
    // Check if there is already an unsaved fiscal activity
    const hasUnsavedFiscal = this.projectFiscals.some(fiscal => !fiscal.projectPlanFiscalGuid);
    if (hasUnsavedFiscal) {
      // And to prevent adding another unsaved fiscal
      return;
    }
    const newFiscalData = this.getDefaultFiscalData();
    this.projectFiscals.push(newFiscalData);
    this.fiscalForms.push(this.createFiscalForm(newFiscalData));
    this.selectedTabIndex = this.projectFiscals.length - 1; // Navigate to the newly added tab
  }

  onCancelFiscal(index: number): void {
    const form = this.fiscalForms[index];
    if (!form) return;

    const isNewEntry = !this.projectFiscals[index]?.projectPlanFiscalGuid;

    if (isNewEntry) {
      // Case 1: New entry, Clear all fields
      form.reset({
        ...this.getDefaultFiscalData(),
        planFiscalStatusCode: 'DRAFT',
        fiscalYear: ''
      },
        { emitEvent: false });
    } else {
      // Case 2: Existing entry, Revert to original API values
      const original = this.originalFiscalValues[index];

      form.patchValue({
        ...original,
        planFiscalStatusCode: this.patchStatusCode(original?.planFiscalStatusCode)
      },
        { emitEvent: false });

      const statusCode = this.patchStatusCode(original?.planFiscalStatusCode);
      const isLocked = [this.FiscalStatuses.COMPLETE, this.FiscalStatuses.CANCELLED].includes(statusCode as any);
      if (isLocked) {
        form.disable({ emitEvent: false });
      } else {
        form.enable({ emitEvent: false });
        if (statusCode !== this.FiscalStatuses.DRAFT) {
          form.get('totalCostEstimateAmount')?.disable({ emitEvent: false });
        }
      }
    }

    form.markAsPristine();
    form.markAsUntouched();
    form.updateValueAndValidity({ emitEvent: false });
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
    if (this.isSavingFiscal[index]) return;
    this.isSavingFiscal[index] = true;
    const originalData = this.projectFiscals[index];
    const formData = this.fiscalForms[index]?.value;
    const updatedData = {
      ...originalData, // Include all original data and overwrite with form data
      ...formData,
    };
    const isUpdate = this.projectFiscals[index]?.projectPlanFiscalGuid;
    const isUpdateToDraft = updatedData.planFiscalStatusCode === 'DRAFT';
    const payload: ProjectFiscal = this.buildProjectFiscal(updatedData, originalData, isUpdate, isUpdateToDraft);

    const fiscalToSave = isUpdate
      ? this.projectService.updateProjectFiscal(this.projectGuid, updatedData.projectPlanFiscalGuid, payload)
      : this.projectService.createProjectFiscal(this.projectGuid, payload);

    fiscalToSave.subscribe({
      next: (res: any) => {
        if (isUpdate) {
          this.snackbarService.open(this.messages.projectFiscalUpdatedSuccess, 'OK', {
            duration: 5000, panelClass: 'snackbar-success'
          });
          this.loadProjectFiscals(true, updatedData.projectPlanFiscalGuid);
          this.fiscalsUpdated.emit();
        } else {
          const newFiscalGuid = res?.projectPlanFiscalGuid;
          this.snackbarService.open(this.messages.projectFiscalCreatedSuccess, 'OK', {
            duration: 5000, panelClass: 'snackbar-success'
          });
          this.loadProjectFiscals(true, newFiscalGuid);
        }
      },
      error: () => {
        const msg = isUpdate
          ? this.messages.projectFiscalUpdatedFailure
          : this.messages.projectFiscalCreatedFailure;
        this.snackbarService.open(msg, 'OK', {
          duration: 5000, panelClass: 'snackbar-error'
        });
      },
      complete: () => {
        this.isSavingFiscal[index] = false;
      }
    });
  }

  buildProjectFiscal(
    updated: any,
    original: any,
    isUpdate: boolean,
    isDraft: boolean
  ): ProjectFiscal {
    return {
      projectGuid: updated.projectGuid,
      projectPlanFiscalGuid: updated.projectPlanFiscalGuid,
      activityCategoryCode: updated.activityCategoryCode,
      fiscalYear: updated.fiscalYear ? Number.parseInt(updated.fiscalYear, 10) : 0,
      projectPlanStatusCode: isUpdate ? updated.projectPlanStatusCode : 'ACTIVE',
      planFiscalStatusCode: { planFiscalStatusCode: updated.planFiscalStatusCode },
      projectFiscalName: updated.projectFiscalName,
      projectFiscalDescription: updated.projectFiscalDescription,
      businessAreaComment: isDraft ? undefined : updated.businessAreaComment,
      estimatedClwrrAllocAmount: updated.estimatedClwrrAllocAmount,
      fiscalAncillaryFundAmount: updated.fiscalAncillaryFundAmount,
      fiscalPlannedProjectSizeHa: updated.fiscalPlannedProjectSizeHa,
      fiscalPlannedCostPerHaAmt: updated.fiscalPlannedCostPerHaAmt,
      fiscalReportedSpendAmount: updated.fiscalReportedSpendAmount,
      fiscalActualAmount: updated.fiscalActualAmount,
      fiscalCompletedSizeHa: updated.fiscalCompletedSizeHa,
      fiscalActualCostPerHaAmt: updated.fiscalActualCostPerHaAmt,
      firstNationsDelivPartInd: updated.firstNationsDelivPartInd,
      firstNationsEngagementInd: updated.firstNationsEngagementInd,
      firstNationsPartner: updated.firstNationsPartner,
      resultsNumber: updated.resultsNumber,
      resultsOpeningId: updated.resultsOpeningId,
      resultsContactEmail: updated.resultsContactEmail,
      isDelayedInd: isUpdate ? updated.isDelayedInd : false,
      fiscalForecastAmount: updated.fiscalForecastAmount,
      totalCostEstimateAmount: updated.totalCostEstimateAmount,
      cfsProjectCode: updated.cfsProjectCode,
      ancillaryFundingProvider: updated.ancillaryFundingProvider,
      otherPartner: updated.otherPartner,
      proposalTypeCode: updated.proposalTypeCode,
      endorserName: isDraft ? undefined : original.endorserName,
      endorsementTimestamp: isDraft ? undefined : original.endorsementTimestamp,
      endorsementCode: isDraft ? { endorsementCode: EndorsementCode.NOT_ENDORS } : original.endorsementCode,
      endorsementComment: isDraft ? undefined : original.endorsementComment,
      endorsementEvalTimestamp: isDraft ? undefined : original.endorsementEvalTimestamp,
      endorserUserGuid: isDraft ? undefined : original.endorserUserGuid,
      endorserUserUserid: isDraft ? undefined : original.endorserUserUserid,
      approverName: isDraft ? undefined : original.approverName,
      approvedTimestamp: isDraft ? undefined : original.approvedTimestamp,
      approverUserGuid: isDraft ? undefined : original.approverUserGuid,
      approverUserUserid: isDraft ? undefined : original.approverUserUserid,
      endorseApprUpdateUserid: isDraft ? undefined : original.endorseApprUpdateUserid,
      endorseApprUpdatedTimestamp: isDraft ? undefined : original.endorseApprUpdateUpdatedTimestamp,
      isApprovedInd: isDraft ? false : original.isApprovedInd,
      ...this.buildSubmissionFields(),
    };
  }

  deleteFiscalYear(form: any, index: number): void {
    const formData = form.value;
    const fiscalName = this.projectFiscals[this.selectedTabIndex]?.projectFiscalName;
    const fiscalYear = this.projectFiscals[this.selectedTabIndex]?.fiscalYear;
    const formattedYear = fiscalYear ? `${fiscalYear}/${(fiscalYear + 1).toString().slice(-2)}` : null;
    const yearName = (fiscalName && fiscalYear) ? `${fiscalName}:${formattedYear}` : 'this fiscal activity'
    const fiscalGuid = formData.projectPlanFiscalGuid ?? '';

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        indicator: 'delete-fiscal-year',
        title: ModalTitles.DELETE_FISCAL_YEAR_TITLE,
        message: `Are you sure you want to delete ${yearName}? This action cannot be reversed and will immediately remove the Fiscal Activity from the Project scope.`
      },
      width: '600px',
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {

        const hasActivities = this.activitiesComponent?.activities?.some(a => a.projectPlanFiscalGuid === fiscalGuid) ?? false;

        if (hasActivities) {
          this.snackbarService.open(
            this.messages.fiscalActivityDeletedFailure,
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
          return;
        }

        if (formData.projectPlanFiscalGuid) {
          // Delete from the service call if it's a saved fiscal activity
          this.projectService.deleteProjectFiscalByProjectPlanFiscalGuid(this.projectGuid, formData.projectPlanFiscalGuid)
            .subscribe({
              next: () => {
                this.snackbarService.open(
                  this.messages.projectFiscalDeletedSuccess,
                  'OK',
                  { duration: 5000, panelClass: 'snackbar-success' }
                );
                this.loadProjectFiscals(true);
                this.fiscalsUpdated.emit();
              },
              error: () => {
                this.snackbarService.open(
                  this.messages.projectFiscalDeletedFailure,
                  'OK',
                  { duration: 5000, panelClass: 'snackbar-error' }
                );
              }
            });
        } else {
          // If it's an unsaved draft, just remove it from the list
          this.projectFiscals.splice(index, 1);
          this.fiscalForms.splice(index, 1);

          this.snackbarService.open(
            this.messages.projectFiscalDeletedSuccess,
            "OK",
            { duration: 5000, panelClass: "snackbar-success" }
          );

          this.selectedTabIndex = Math.max(0, this.selectedTabIndex - 1);
        }
      }
    });
  }

  isUndeletable(form: any): boolean {
    return !!form?.value?.isApprovedInd;
  }

  onBoundariesChanged(): void {

    if (this.fiscalMapComponent) {
      this.fiscalMapComponent.getAllActivitiesBoundaries(); // refresh boundaries on map
    }
  }

  getCodeDescription(controlName: string): string | null {
    const form = this.fiscalForms[this.selectedTabIndex];
    if (!form) return null;

    const value = form.get(controlName)?.value;

    switch (controlName) {
      case CodeTableKeys.ACTIVITY_CATEGORY_CODE:
        return this.activityCategoryCode.find(item => item.activityCategoryCode === value)?.description ?? null;

      case CodeTableKeys.PLAN_FISCAL_STATUS_CODE:
        return this.planFiscalStatusCode.find(item => item.planFiscalStatusCode === value)?.description ?? null;

      case CodeTableKeys.PROPOSAL_TYPE_CODE:
        return this.proposalTypeCode.find(item => item.proposalTypeCode === value)?.description ?? null;

      default:
        return null;
    }
  }

  getFiscalControl(i: number, controlName: string): FormControl {
    return this.fiscalForms[i]?.get(controlName) as FormControl;
  }

  getDefaultFiscalData(): ProjectFiscal {
    return {
      fiscalYear: 0,
      projectFiscalName: '',
      projectGuid: this.projectGuid,
      planFiscalStatusCode: {
        planFiscalStatusCode: 'DRAFT',
        description: 'Draft',
      },
      proposalTypeCode: 'NEW',
      projectPlanStatusCode: 'ACTIVE',
      activityCategoryCode: '',
      fiscalPlannedProjectSizeHa: 0,
      fiscalPlannedCostPerHaAmt: 0,
      fiscalReportedSpendAmount: 0,
      fiscalActualAmount: 0,
      fiscalActualCostPerHaAmt: 0,
      firstNationsEngagementInd: false,
      firstNationsDelivPartInd: false,
      isApprovedInd: false,
      isDelayedInd: false,
      totalCostEstimateAmount: 0
    };
  }


  getStatusDescription(i: number): string | null {
    const form = this.fiscalForms[i];
    if (!form) return null;
    const code = form.get('planFiscalStatusCode')?.value;
    return (
      this.planFiscalStatusCode.find((item) => item.planFiscalStatusCode === code)?.description ?? null
    );
  }

  getStatusIcon(status: string) {
    return PlanFiscalStatusIcons[status];
  }

  updateFiscalStatus(index: number, newStatus: string): void {
    const form = this.fiscalForms[index];
    if (!form) return;

    // Parse status to plain English if it is IN_PROG
    // The remaining status do not require such parsing
    const currentStatus = form.value?.planFiscalStatusCode === 'IN_PROG' ? "In Progress" : capitalizeFirstLetter(form.value.planFiscalStatusCode)
    const requestedStatus = newStatus === 'IN_PROG' ? "In Progress" : capitalizeFirstLetter(newStatus)
    const message = `You are about to change the status of this Fiscal Activity from ${currentStatus} to ${requestedStatus}. Do you wish to continue?`

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        indicator: 'confirm-fiscal-status-update',
        title: `Confirm Change to ${requestedStatus}`,
        message: message
      },
      width: '600px',
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        form.get('planFiscalStatusCode')?.setValue(newStatus);
        form.markAsDirty();
        form.markAsTouched();
        this.onSaveFiscal(index);
      }
    });

  }

  onFiscalAction(event: { action: string; index: number }) {
    const { action, index } = event;

    if (action === 'DELETE') {
      this.deleteFiscalYear(this.fiscalForms[index], index);
    } else {
      this.updateFiscalStatus(index, action);
    }
  }


  onSaveEndorsement(updatedFiscal: ProjectFiscal): void {
    const index = this.selectedTabIndex;
    if (!this.projectFiscals[index]) return;
    this.isSavingFiscal[index] = true;

    // Merge updated fields
    this.mergeFiscalUpdates(index, updatedFiscal);

    const fiscalGuid = this.projectFiscals[index]?.projectPlanFiscalGuid;
    if (!fiscalGuid) return;

    // Dont update the timstamp of fiscal detail shows above.
    const payload: Partial<ProjectFiscal> = { ...this.projectFiscals[index] };
    delete payload.submittedByUserUserid;
    delete payload.submissionTimestamp;

    this.projectService.updateProjectFiscal(
      this.projectGuid,
      fiscalGuid,
      payload as ProjectFiscal
    ).subscribe({
      next: () => {
        this.handleEndorsementUpdateSuccess(index, updatedFiscal);
        this.isSavingFiscal[index] = false;
      },
      error: () => {
        this.showSnackbar(this.messages.projectFiscalUpdatedFailure, false);
        this.isSavingFiscal[index] = false;
      }
    });
  }

  mergeFiscalUpdates(index: number, updatedFiscal: ProjectFiscal): void {
    this.projectFiscals[index] = {
      ...this.projectFiscals[index],
      ...updatedFiscal
    };
  }

  handleEndorsementUpdateSuccess(index: number, updatedFiscal: ProjectFiscal): void {
    const isApproved = updatedFiscal.isApprovedInd === true;
    const isEndorsed = updatedFiscal.endorsementCode?.endorsementCode === EndorsementCode.ENDORSED;
    const isProposed = updatedFiscal.planFiscalStatusCode?.planFiscalStatusCode === FiscalStatuses.PROPOSED;

    if (isApproved && isEndorsed && isProposed) {
      this.promoteFiscalStatus(index);
    } else {
      this.showSnackbar(this.messages.projectFiscalUpdatedSuccess);
      this.loadProjectFiscals(true);
      this.fiscalsUpdated.emit();
    }
  }

  promoteFiscalStatus(index: number): void {
    const fiscalGuid = this.projectFiscals[index]?.projectPlanFiscalGuid;
    if (!fiscalGuid) return;

    const promotionPayload: ProjectFiscal = {
      ...this.projectFiscals[index],
      planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.PREPARED }
    };

    this.projectService.updateProjectFiscal(
      this.projectGuid,
      fiscalGuid,
      promotionPayload
    ).subscribe({
      next: () => {
        this.showSnackbar(this.messages.projectFiscalUpdatedSuccess);
        this.loadProjectFiscals(true);
        this.fiscalsUpdated.emit();
      },
      error: () => this.showSnackbar(this.messages.projectFiscalUpdatedFailure, false)
    });
  }

  showSnackbar(message: string, isSuccess: boolean = true): void {
    this.snackbarService.open(
      message,
      'OK',
      { duration: 5000, panelClass: isSuccess ? 'snackbar-success' : 'snackbar-error' }
    );
  }

  getFirstFiscalGuid(): string | null {
    return this.projectFiscals[0]?.projectPlanFiscalGuid ?? null;
  }

  buildSubmissionFields() {
    return {
      submittedByUserUserid: this.currentIdir,
      submissionTimestamp: getUtcIsoTimestamp()
    }
  }

  patchStatusCode(val: any): string {
    return typeof val === 'string' ? val : val?.planFiscalStatusCode ?? '';
  }

  isCurrentFiscalReadonly(): boolean {
    const current = this.projectFiscals[this.selectedTabIndex];
    if (!current) return false;

    const status = current.planFiscalStatusCode?.planFiscalStatusCode;
    return [this.FiscalStatuses.COMPLETE, this.FiscalStatuses.CANCELLED].includes(status);
  }

}

