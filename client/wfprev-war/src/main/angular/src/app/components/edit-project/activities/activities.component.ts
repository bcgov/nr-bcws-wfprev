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
    FormsModule
  ],
  templateUrl: './activities.component.html',
  styleUrl: './activities.component.scss',
})
export class ActivitiesComponent implements OnChanges, OnInit{
  @Input() fiscalGuid: string = '';
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
  isEditingComment: boolean[] = [];
  isActivityDirty: boolean[] = [];
  //use this temporary code.. the table got dropped via liquibase script.
  fundingSourcesTable = [
    {
      fundingSourceGuid: "2141ef38-0a0d-422e-a990-6e9d3d2a4bc8",
      fundingSourceCode: "WRR",
      fundingSourceName: "Wildfire Risk Reduction"
    },
    {
      fundingSourceGuid: "8739d565-5ff8-4b74-b115-23ada22f84fb",
      fundingSourceCode: "BCP",
      fundingSourceName: "BC Parks"
    },
    {
      fundingSourceGuid: "04543243-28ed-4a2b-8935-5f91acdb608c",
      fundingSourceCode: "FEP",
      fundingSourceName: "Forest Employment Program"
    },
    {
      fundingSourceGuid: "483707b1-86f2-4d54-94f5-8cbf1aaef0c7",
      fundingSourceCode: "FESBC",
      fundingSourceName: "Forest Enhancement Society of BC"
    },
    {
      fundingSourceGuid: "2f84b1d7-4d84-432f-922c-e489fe7d764f",
      fundingSourceCode: "CFS",
      fundingSourceName: "FireSmart Community Funding Supports"
    }
  ];
  
  
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
        this.contractPhaseCode = data._embedded.contractPhaseCode || [];
        break;
      case 'fundingSourceCode':
        this.fundingSourceCode = data._embedded.fundingSourceCode || [];
        break;
      case 'silvicultureBaseCode':
        this.silvicultureBaseCode = data._embedded.silvicultureBaseCode || [];
        break;
      case 'silvicultureTechniqueCode':
        this.silvicultureTechniqueCode = data._embedded.silvicultureTechniqueCode || [];
        break;
      case 'silvicultureMethodCode':
        this.silvicultureMethodCode = data._embedded.silvicultureMethodCode || [];
        break;
    }
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

  createActivityForm(activity?: any): FormGroup {
    const form = this.fb.group({
      activityGuid: [activity?.activityGuid || ''],
      projectPlanFiscalGuid: [activity?.projectPlanFiscalGuid || ''],
      activityStatusCode: [activity?.activityStatusCode || ''],
      silvicultureBaseGuid: [activity?.silvicultureBaseGuid || ''],
      silvicultureTechniqueGuid: [activity?.silvicultureTechniqueGuid || {value: null, disabled: true}],
      silvicultureMethodGuid: [activity?.silvicultureMethodGuid || {value: null, disabled: true }],
      riskRatingCode: [activity?.riskRatingCode || ''],
      contractPhaseCode: [activity?.contractPhaseCode?.contractPhaseCode || ''],
      activityFundingSourceGuid: [activity?.activityFundingSourceGuid || ''],
      activityName: [activity?.activityName || '', [Validators.required]],
      activityDescription: [activity?.activityDescription || '', [Validators.required, Validators.maxLength(500)]],
      activityDateRange: this.fb.group({
        activityStartDate: [this.formatDate(activity?.activityStartDate) || '', Validators.required],
        activityEndDate: [this.formatDate(activity?.activityEndDate) || '', Validators.required]
      }),
      plannedSpendAmount: [activity?.plannedSpendAmount ?? ''],
      plannedTreatmentAreaHa: [activity?.plannedTreatmentAreaHa ?? ''],
      reportedSpendAmount: [activity?.reportedSpendAmount ?? ''],
      completedAreaHa: [activity?.completedAreaHa ?? ''],
      isResultsReportableInd: [activity?.isResultsReportableInd || false],
      outstandingObligationsInd: [activity?.outstandingObligationsInd || false],
      activityComment: [activity?.activityComment || ''],
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
    return date ? moment(date).format('YYYY-MM-DD') : '';
  }
  getActivityTitle(index: number): string {
    const activity = this.activities[index];
    if (!activity) return 'N/A'; // Handle missing data
  
    const base = this.silvicultureBaseCode.find(b => b.silvicultureBaseGuid === activity.silvicultureBaseGuid)?.description || 'Unknown Base';
    const method = this.silvicultureMethodCode.find(m => m.silvicultureMethodGuid === activity.silvicultureMethodGuid)?.description || 'Unknown Method';
    const technique = this.silvicultureTechniqueCode.find(t => t.silvicultureTechniqueGuid === activity.silvicultureTechniqueGuid)?.description || 'Unknown Technique';
  
    return `${base} - ${method} - ${technique}`;
  }
  
  
  getLastUpdated(index: number) {
    const activity = this.activities[index];
    if (!activity) return 'N/A'; // Handle missing data
    
    return moment(activity.updateDate).format('YYYY-MM-DD');
  }

  toggleActivityStatus(index: number) {
    const currentStatus = this.activityForms[index].value?.activityStatusCode;
    const newStatus = currentStatus === 'COMPLETED' ? 'ACTIVE' : 'COMPLETED';
  
    this.activityForms[index].patchValue({
      activityStatusCode: newStatus
    });
  }

  addActivity(): void {
    const newActivity = {};
    this.activities.push(newActivity);
    this.activityForms.push(this.createActivityForm(newActivity));
    
    this.cd.detectChanges();
  }

  toggleEditComment(index: number) {
    this.isEditingComment[index] = true;
  }
  
  saveComment(index: number) {
    this.isEditingComment[index] = false;
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
    const formData = this.activityForms[index]?.value;
  
    const updatedData = {
      ...originalData, // Include all original data and overwrite with form data
      ...formData,
    };
  
    const isUpdate = !!this.activities[index]?.activityGuid;
  
    if (isUpdate) {
      // Update existing activity
      this.projectService.updateFiscalActivities(this.projectGuid, this.fiscalGuid, updatedData.activityGuid, updatedData).subscribe({
        next: () => {
          this.snackbarService.open(
            'Activity updated successfully!',
            'OK',
            { duration: 5000, panelClass: 'snackbar-success' }
          );
          this.isActivityDirty[index] = false;
          this.activityForms[index].markAsPristine(); // reset dirty tracking
          this.getActivities(); // Refresh activities after saving
        },
        error: () => {
          this.snackbarService.open(
            'Failed to update activity. Please try again later.',
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
            'New activity created successfully!',
            'OK',
            { duration: 5000, panelClass: 'snackbar-success' }
          );
          this.isActivityDirty[index] = false;
          this.activityForms[index].markAsPristine(); // Reset dirty tracking
          this.getActivities();
        },
        error: () => {
          this.snackbarService.open(
            'Failed to create activity. Please try again later.',
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
        }
      });
    }
  }
  
  onCancelActivity(index: number): void {
    if (!this.activityForms[index]) return;
  
    const isNewEntry = !this.activities[index]?.activityGuid;
  
    if (isNewEntry) {
      // Remove the new entry
      this.activities.splice(index, 1);
      this.activityForms.splice(index, 1);
      this.snackbarService.open(
        'New activity creation cancelled.',
        'OK',
        { duration: 3000, panelClass: 'snackbar-warning' }
      );
    } else {
      // Reset to original values
      const originalData = this.originalActivitiesValues[index];
      this.activityForms[index].patchValue(originalData);
      this.activityForms[index].markAsPristine();
      this.activityForms[index].markAsUntouched();
      this.isActivityDirty[index] = false;
    }
  }
  
  
}
