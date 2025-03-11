import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule  } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import moment from 'moment';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDateFormats, MatNativeDateModule, MAT_DATE_FORMATS, MAT_DATE_LOCALE, DateAdapter } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { Messages } from 'src/app/utils/messages';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { Observable } from 'rxjs';
import { CanComponentDeactivate } from 'src/app/services/util/can-deactive.guard';
import { MomentDateAdapter } from '@angular/material-moment-adapter';


export const CUSTOM_DATE_FORMATS = {
  parse: { dateInput: 'YYYY/MM/DD' },
  display: {
    dateInput: 'YYYY/MM/DD',
    monthYearLabel: 'YYYY MMM',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'YYYY MMMM',
  },
};

@Component({
  selector: 'app-activities',
  standalone: true,
  imports: [MatExpansionModule,
    ReactiveFormsModule,
    CommonModule,
    MatSlideToggleModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    FormsModule,
    MatCheckboxModule
  ],
  templateUrl: './activities.component.html',
  styleUrl: './activities.component.scss',
  providers: [
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    { provide: MAT_DATE_LOCALE, useValue: 'en-CA' }, // Change locale to Canada (YYYY/MM/DD format)
    { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] }
    ],
})

export class ActivitiesComponent implements OnChanges, OnInit, CanComponentDeactivate{
  @Input() fiscalGuid: string = '';
  messages = Messages;
  isNewActivityBeingAdded = false;
  
  projectGuid = '';
  activities: any[] = [];
  originalActivitiesValues: any[] = [];
  contractPhaseCode: any[] = [];
  fundingSourceCode: any[] = [];
  silvicultureBaseCode: any[] = [];
  silvicultureTechniqueCode: any[] = [];
  silvicultureMethodCode: any[] = [];

  filteredTechniqueCode: any[] = [];
  filteredMethodCode: any[] = [];

  activityForms: FormGroup[] = [];
  projectTypeCode = '';
  isActivityDirty: boolean[] = [];
  expandedPanels: boolean[] = [];
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
  }
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscalGuid'] && changes['fiscalGuid'].currentValue) {
      this.loadCodeTables();
      this.getActivities();
    }
  }

  loadCodeTables(): void {
    const codeTables = [
      { name: 'contractPhaseCodes', embeddedKey: 'contractPhaseCode' },
      { name: 'fundingSourceCodes', embeddedKey: 'fundingSourceCode' },
      { name: 'silvicultureBaseCodes', embeddedKey: 'silvicultureBaseCode'},
      { name: 'silvicultureTechniqueCodes', embeddedKey: 'silvicultureTechniqueCode'},
      { name: 'silvicultureMethodCodes', embeddedKey: 'silvicultureMethodCode'}
    ];
  
    codeTables.forEach((table) => {
      this.codeTableService.fetchCodeTable(table.name).subscribe({
        next: (data) => {
          this.assignCodeTableData(table.embeddedKey, data);
        },
        error: (err) => {
          console.error(`Error fetching ${table.name}`, err);
          this.assignCodeTableData(table.embeddedKey, []); // Assign empty array on error
        },
      });
    });
  }

  assignCodeTableData(key: string, data: any): void {
    switch (key) {
      case 'contractPhaseCode':
        this.contractPhaseCode = this.sortArray(data._embedded.contractPhaseCode || [], 'description');
        break;
      case 'fundingSourceCode':
        this.fundingSourceCode = this.sortArray(data._embedded.fundingSourceCode || [], 'description');
        break;
      case 'silvicultureBaseCode':
        this.silvicultureBaseCode = this.sortArray(data._embedded.silvicultureBaseCode || [], 'description');
        break;
      case 'silvicultureTechniqueCode':
        this.silvicultureMethodCode = this.sortArray(data._embedded.silvicultureMethodCode || [], 'description');
        break;
      case 'silvicultureMethodCode':
        this.silvicultureTechniqueCode = this.sortArray(data._embedded.silvicultureTechniqueCode || [], 'description');
        break;
    }
  }

  sortArray<T>(array: T[], key?: keyof T): T[] {
    if (!array) return [];
    return key
      ? array.sort((a, b) => String(a[key]).localeCompare(String(b[key])))
      : array.sort((a, b) => String(a).localeCompare(String(b)));
  }


  getActivities(): void {
    if (!this.fiscalGuid) return;
  
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';
    
    if (this.projectGuid){

      this.getProjectType(this.projectGuid);
    
      this.projectService.getFiscalActivities(this.projectGuid, this.fiscalGuid).subscribe({
        next: (data) => {
          if (data && data._embedded?.activities) {
            this.activities = data._embedded.activities;
          } else {
            this.activities = [];
          }
    
          this.originalActivitiesValues = JSON.parse(JSON.stringify(this.activities));
    
          this.activityForms = this.activities.map((activity) => this.createActivityForm(activity));
          this.expandedPanels = this.activities.map((_, i) => this.expandedPanels[i] || false);

          this.cd.detectChanges();
        },
        error: (error) => {
          console.error('Error fetching activities:', error);
          this.activities = [];

          this.snackbarService.open(
            'Failed to load activities. Please try again later.',
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
        }
      });
    }
  }

  getProjectType(projectGuid: string) {
        this.projectService.getProjectByProjectGuid(this.projectGuid).subscribe({
          next: (data) => {
            this.projectTypeCode = data.projectTypeCode?.projectTypeCode
          },
          error: (err) => {
            console.error('Error fetching project:', err);
          },
        });
  }

  getFormattedDate(date: string | null): string {
    return date ? moment.utc(date).format('YYYY-MM-DD') : '';
  }
  

  createActivityForm(activity?: any): FormGroup {
    const form = this.fb.group({
      activityGuid: [activity?.activityGuid || ''],
      projectPlanFiscalGuid: [activity?.projectPlanFiscalGuid || ''],
      activityStatusCode: [activity?.activityStatusCode?.activityStatusCode || 'ACTIVE'],
      silvicultureBaseGuid: [activity?.silvicultureBaseGuid || ''],
      silvicultureTechniqueGuid: [activity?.silvicultureTechniqueGuid || {value: null, disabled: true}],
      silvicultureMethodGuid: [activity?.silvicultureMethodGuid || {value: null, disabled: true }],
      riskRatingCode: [activity?.riskRatingCode || {'riskRatingCode':'LOW_RISK'}],
      contractPhaseCode: [activity?.contractPhaseCode?.contractPhaseCode || ''],
      activityFundingSourceGuid: [activity?.activityFundingSourceGuid || ''],
      activityName: [activity?.activityName || '', [Validators.required]],
      activityDescription: [activity?.activityDescription || '', [Validators.required, Validators.maxLength(500)]],
      activityDateRange: this.fb.group({
        activityStartDate: [activity?.activityStartDate ? moment.utc(activity.activityStartDate).format('YYYY-MM-DD') : '', Validators.required],
        activityEndDate: [activity?.activityEndDate ? moment.utc(activity.activityEndDate).format('YYYY-MM-DD') : '', Validators.required]
      }),
      plannedSpendAmount: [activity?.plannedSpendAmount ?? '', [Validators.min(0)]],
      plannedTreatmentAreaHa: [activity?.plannedTreatmentAreaHa ?? '', [Validators.required,Validators.min(0)]],
      reportedSpendAmount: [activity?.reportedSpendAmount ?? '', [Validators.min(0)]],
      completedAreaHa: [activity?.completedAreaHa ?? '', [Validators.min(0)]],
      isResultsReportableInd: [activity?.isResultsReportableInd || false],
      outstandingObligationsInd: [activity?.outstandingObligationsInd || false],
      activityComment: [activity?.activityComment || '', [Validators.maxLength(500)]],
      isSpatialAddedInd: [activity?.isSpatialAddedInd || false],
      createDate: [activity?.createDate || ''], // ISO 8601 date format
    });
    if (activity?.silvicultureBaseGuid) {
      this.filteredTechniqueCode = this.silvicultureTechniqueCode.filter(t => t.silvicultureBaseGuid === activity.silvicultureBaseGuid);
    }  
    if (activity?.silvicultureTechniqueGuid) {
      this.filteredMethodCode = this.silvicultureMethodCode.filter(m => m.silvicultureTechniqueGuid === activity.silvicultureTechniqueGuid);
    }

    // Handle user selection changes
    form.get('silvicultureBaseGuid')?.valueChanges.subscribe((baseGuid) => this.onBaseChange(baseGuid, form));
    form.get('silvicultureTechniqueGuid')?.valueChanges.subscribe((techniqueGuid) => this.onTechniqueChange(techniqueGuid, form));

    form.valueChanges.subscribe(() => {
      const index = this.activityForms.indexOf(form);
      if (index !== -1) {
        this.isActivityDirty[index] = form.dirty
      }
    })
    return form;
  }

  onBaseChange(baseGuid: string, form: FormGroup) {
    if (!baseGuid) {
      form.get('silvicultureTechniqueGuid')?.setValue(null);
      form.get('silvicultureTechniqueGuid')?.disable();
      form.get('silvicultureMethodGuid')?.setValue(null);
      form.get('silvicultureMethodGuid')?.disable();
      this.filteredTechniqueCode = [];
      return;
    }

    this.filteredTechniqueCode = this.silvicultureTechniqueCode.filter(t => t.silvicultureBaseGuid === baseGuid);
    form.get('silvicultureTechniqueGuid')?.enable();
    form.get('silvicultureTechniqueGuid')?.setValue(null);
    form.get('silvicultureMethodGuid')?.setValue(null);
    form.get('silvicultureMethodGuid')?.disable();
    this.filteredMethodCode = [];
  }

  toggleResultsReportableInd(index: number): void {
    const form = this.activityForms[index];
  
    if (!form) return;
  
    const isReportable = form.get('isResultsReportableInd')?.value;
  
    const baseField = form.get('silvicultureBaseGuid');
    const nameField = form.get('activityName');
  
    if (isReportable) {
      baseField?.setValidators([Validators.required]);
      nameField?.disable();
      nameField?.setValue(this.getActivityTitle(index)); // Set name initially
  
      form.get('silvicultureBaseGuid')?.valueChanges.subscribe(() => {
        if (form.get('isResultsReportableInd')?.value) {
          nameField?.setValue(this.getActivityTitle(index));
        }
      });
  
      form.get('silvicultureTechniqueGuid')?.valueChanges.subscribe(() => {
        if (form.get('isResultsReportableInd')?.value) {
          nameField?.setValue(this.getActivityTitle(index));
        }
      });
  
      form.get('silvicultureMethodGuid')?.valueChanges.subscribe(() => {
        if (form.get('isResultsReportableInd')?.value) {
          nameField?.setValue(this.getActivityTitle(index));
        }
      });
    } else {
      baseField?.clearValidators();
      nameField?.enable();
      nameField?.setValue(''); // Clear name when toggle is OFF
    }
  
    baseField?.updateValueAndValidity();
    nameField?.updateValueAndValidity();
    this.cd.detectChanges();
  }
  
  
  

  onTechniqueChange(techniqueGuid: string, form: FormGroup) {
    if (!techniqueGuid) {
      form.get('silvicultureMethodGuid')?.setValue(null);
      form.get('silvicultureMethodGuid')?.disable();
      this.filteredMethodCode = [];
      return;
    }

    this.filteredMethodCode = this.silvicultureMethodCode.filter(m => m.silvicultureTechniqueGuid === techniqueGuid);
    form.get('silvicultureMethodGuid')?.enable();
    form.get('silvicultureMethodGuid')?.setValue(null);
  }

  formatDate(date: string | Date): string {
    return date ? moment.utc(date).format('YYYY-MM-DD') : ''; // Forces UTC interpretation
  }
  
  getActivityTitle(index: number): string {
    const activity = this.activityForms[index]?.value;
    if (!activity) return '';
  
    // If Results Reportable is ON, construct Base - Technique - Method dynamically
    if (activity.isResultsReportableInd) {
      const parts: string[] = [];
  
      const base = this.silvicultureBaseCode.find(b => b.silvicultureBaseGuid === activity.silvicultureBaseGuid)?.description;
      const technique = this.silvicultureTechniqueCode.find(t => t.silvicultureTechniqueGuid === activity.silvicultureTechniqueGuid)?.description;
      const method = this.silvicultureMethodCode.find(m => m.silvicultureMethodGuid === activity.silvicultureMethodGuid)?.description;
  
      if (base) parts.push(base);
      if (technique) parts.push(technique);
      if (method) parts.push(method);
  
      return parts.length ? parts.join(' - ') : '';
    } 
  
    // If Results Reportable is OFF, use the Activity Name (if available)
    return activity.activityName?.trim() || '';
  }
  
  
  
  getLastUpdated(index: number) {
    const activity = this.activities[index];
    if (!activity) return ''; // Handle missing data
    
    return moment(activity.updateDate).format('YYYY-MM-DD');
  }

  toggleActivityStatus(index: number) {
    const currentStatus = this.activityForms[index].value?.activityStatusCode;
    const newStatus = currentStatus === 'COMPLETED' ? 'ACTIVE' : 'COMPLETED';
  
    this.activityForms[index].patchValue({
      activityStatusCode: newStatus
    });
    this.isActivityDirty[index] = true;
  }

  addActivity(): void {
    if (this.isNewActivityBeingAdded) return;

    this.isNewActivityBeingAdded = true;
    const newActivity = {};
    this.activities.push(newActivity);
    this.activityForms.push(this.createActivityForm(newActivity));
    this.expandedPanels = this.activities.map((_, i) => i === this.activities.length - 1);

    this.cd.detectChanges();
  }

  getRiskIcon(riskCode: string): string {
    const riskMap: { [key: string]: string } = {
      'LOW_RISK': 'low-risk',
      'MODRT_RISK': 'medium-risk',
      'HIGH_RISK': 'high-risk'
    };
  
    return riskMap[riskCode] || 'none-risk';
  }
  
  getRiskDescription(description: string | null | undefined): string {
    return description || 'None'; // Default to "None" if description is empty or null
  }

  onSaveActivity(index: number): void {
    const originalData = this.activities[index];
    const form = this.activityForms[index];
    if (!form) return;
    let formData = { ...form.getRawValue() };
  
    //extract start and end dates separetely
    const activityStartDate = formData.activityDateRange?.activityStartDate
      ? moment.utc(formData.activityDateRange.activityStartDate, 'YYYY-MM-DD').toISOString()
      : null;

    const activityEndDate = formData.activityDateRange?.activityEndDate
      ? moment.utc(formData.activityDateRange.activityEndDate, 'YYYY-MM-DD').toISOString()
      : null;
    let updatedData:any = {
      ...originalData, // Include all original data and overwrite with form data
      ...formData,
      activityStartDate,
      activityEndDate,
      contractPhaseCode: typeof formData.contractPhaseCode === 'string' 
        ? { contractPhaseCode: formData.contractPhaseCode}
        : formData.contractPhaseCode,
      activityStatusCode: typeof formData.activityStatusCode === 'string' 
        ? { activityStatusCode: formData.activityStatusCode } 
        : formData.activityStatusCode,
      
    };
    delete updatedData.activityDateRange;

    if (!updatedData.projectPlanFiscalGuid) {
      updatedData.projectPlanFiscalGuid = this.fiscalGuid;
    }
    // Remove empty or null values
    updatedData = this.removeEmptyFields(updatedData, [
      'projectPlanFiscalGuid',
      'activityStartDate',
      'activityEndDate',
      'activityName'
    ]);
  
    const isUpdate = !!this.activities[index]?.activityGuid;
  
    if (isUpdate) {
      // Update existing activity
      this.projectService.updateFiscalActivities(this.projectGuid, this.fiscalGuid, updatedData.activityGuid, updatedData).subscribe({
        next: () => {
          this.snackbarService.open(
            this.messages.activityUpdatedSuccess,
            'OK',
            { duration: 5000, panelClass: 'snackbar-success' }
          );
          this.isActivityDirty[index] = false;
          this.activityForms[index].markAsPristine(); // reset dirty tracking
          this.expandedPanels[index] = true;
          this.getActivities(); // Refresh activities after saving
        },
        error: () => {
          this.snackbarService.open(
            this.messages.activityUpdatedFailure,
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
        }
      });
    } else {
      // Create new activity
      this.projectService.createFiscalActivity(this.projectGuid, this.fiscalGuid, updatedData).subscribe({
        next: (response) => {
          this.snackbarService.open(
            this.messages.activityCreatedSuccess,
            'OK',
            { duration: 5000, panelClass: 'snackbar-success' }
          );
          this.isActivityDirty[index] = false;
          this.activityForms[index].markAsPristine(); // Reset dirty tracking
          this.expandedPanels.push(true);
          this.getActivities();
        },
        error: () => {
          this.snackbarService.open(
            this.messages.activityCreatedFailure,
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
        }
      });
    }
    this.isNewActivityBeingAdded = false;
  }

  removeEmptyFields(obj: any, alwaysInclude: string[] = []): any {
    return Object.fromEntries(
        Object.entries(obj).filter(([key, value]) => {
            if (alwaysInclude.includes(key)) return true;
            if (value === null || value === undefined || value === '') return false;
            if (typeof value === 'object' && Object.keys(value).length > 0) {
                const cleanedValue = this.removeEmptyFields(value);
                return Object.keys(cleanedValue).length > 0;
            }

            return true;
        })
    );
  }


  
  onCancelActivity(index: number): void {
    if (!this.activityForms[index]) return;
  
    const isNewEntry = !this.activities[index]?.activityGuid;
    const originalData = this.originalActivitiesValues[index];

    if (isNewEntry) {
      // Remove the new entry
      this.activities.splice(index, 1);
      this.activityForms.splice(index, 1);
      this.isNewActivityBeingAdded = false;
    } else {
      // Reset to original values
      this.activityForms[index].reset(this.createActivityForm(originalData).getRawValue(), { emitEvent: false });

      this.activityForms[index].get('activityDateRange')?.setValue({
        activityStartDate: originalData.activityStartDate ? moment.utc(originalData.activityStartDate).format('YYYY-MM-DD') : '',
        activityEndDate: originalData.activityEndDate ? moment.utc(originalData.activityEndDate).format('YYYY-MM-DD') : '',
      }, { emitEvent: false });
  
      this.activityForms[index].markAsPristine();
      this.activityForms[index].markAsUntouched();
      this.isActivityDirty[index] = false;
  
      this.cd.detectChanges();
    }
  }

  onDeleteActivity(index:number): void{
    const data = this.activityForms[index]?.value;
    const activityGuid = data.activityGuid;
    const activityName = data.activityName;
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: { indicator: 'delete-activity', name:activityName},
      width: '500px',
    });
  
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
          if (!activityGuid) {
            // If no activityGuid, simply remove it from the local list
            this.activities.splice(index, 1);
            this.activityForms.splice(index, 1);
            this.isNewActivityBeingAdded = false;
            return;
          }
          // Delete from the service call if it's a saved fiscal year
          this.projectService.deleteActivity(this.projectGuid, this.fiscalGuid, activityGuid)
            .subscribe({
              next: () => {
                this.snackbarService.open(
                  this.messages.activityDeletedSuccess,
                  'OK',
                  { duration: 5000, panelClass: 'snackbar-success' }
                );
                this.getActivities()
              },
              error: () => {
                this.snackbarService.open(
                  this.messages.activityDeletedFailure,
                  'OK',
                  { duration: 5000, panelClass: 'snackbar-error' }
                );
              }
            });
        }
      }
    )
      
  }

  canDeleteActivity(index: number): boolean {
    const activity = this.activityForms[index]?.value;
    if (!activity) return false;
  
    // Delete is available when:
    // Activity is not set to Complete
    // Does not have a Performance update on the Fiscal
    // Delete is in an inactive state when:
    // The user does not have permission to delete
    // The Activity has been started ie. there is a Performance Update on the Fiscal
    // The Activity has polygon files associated to it
    // The activity is marked as Complete
  
    // We dont have permissions and performance implemneted yet. Check single condition that prevent deletion
    return activity.activityStatusCode !== 'COMPLETED'; 
  }
  
  getDeleteIcon(index: number): string {
    return this.canDeleteActivity(index) ? '/assets/delete-icon.svg' : '/assets/delete-disabled-icon.svg';
  }

  isFormDirty(): boolean {
    return this.activityForms.some((form) => form.dirty);
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
}
