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
  activityForms: FormGroup[] = [];
  
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
  }
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscalGuid'] && changes['fiscalGuid'].currentValue) {
      this.getActivities();
    }
  }

  loadCodeTables(): void {
    const codeTables = [
      { name: 'contractPhaseCodes', embeddedKey: 'contractPhaseCode' },
      { name: 'fundingSourceCodes', embeddedKey: 'fundingSourceCode' },
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
    }
  }

  getActivities(): void {
    if (!this.fiscalGuid) return;
  
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';
  
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

  createActivityForm(activity?: any): FormGroup {
    return this.fb.group({
      activityGuid: [activity?.activityGuid || ''],
      projectPlanFiscalGuid: [activity?.projectPlanFiscalGuid || ''],
      activityStatusCode: [activity?.activityStatusCode || ''],
      silvicultureBaseGuid: [activity?.silvicultureBaseGuid || ''],
      silvicultureTechniqueGuid: [activity?.silvicultureTechniqueGuid || ''],
      silvicultureMethodGuid: [activity?.silvicultureMethodGuid || ''],
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
  }

  formatDate(date: string | Date): string {
    return date ? moment(date).format('YYYY-MM-DD') : '';
  }
  getActivityTitle(index: number) {
    const activity = this.activities[index];
    if (!activity) return 'N/A'; // Handle missing data

    const base = activity.silvicultureBaseGuid || 'Unknown Base';
    const method = activity.silvicultureMethodGuid || 'Unknown Method';
    const technique = activity.silvicultureTechniqueGuid || 'Unknown Technique';
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

}
