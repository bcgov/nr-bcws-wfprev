import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription, finalize, forkJoin } from 'rxjs';
import { PermissionsService, WFPREV_ACTIONS } from 'src/app/services/permissions.service';
import { ProjectService } from 'src/app/services/project-services';
import { ResourcesRoutes } from 'src/app/utils';
import { YearEndActivitiesComponent } from './year-end-activities/year-end-activities.component';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { NgxCurrency } from '@dintecom/ngx-currency';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectFiscalExtended, FiscalCloseout, YearEndActivityViewModel } from 'src/app/components/models';
import { TextareaComponent } from 'src/app/components/shared/textarea/textarea.component';
import { MatButtonModule } from '@angular/material/button';
import { IconDisplayFieldComponent } from '../shared/icon-display-field/icon-display-field.component';
import { FiscalStatuses } from 'src/app/utils/constants';



@Component({
  selector: 'wfprev-year-end-performance-update',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    NgxCurrency,
    TextareaComponent,
    MatIconModule, 
    MatProgressSpinnerModule,
    YearEndActivitiesComponent,
    IconDisplayFieldComponent
  ],
  templateUrl: './year-end-performance-update.component.html',
  styleUrl: './year-end-performance-update.component.scss'
})
export class YearEndPerformanceUpdateComponent implements OnInit, OnDestroy {
  projectGuid: string = '';
  fiscalGuid: string = '';
  workflow: string = '';
  isLoading: boolean = true;
  activityViews: YearEndActivityViewModel[] = [];
  summaryForm!: FormGroup;
  fiscalData?: ProjectFiscalExtended;
  closeoutData?: FiscalCloseout;
  planFiscalStatusCodes: any[] = [];
  isSavingSummary = false;
  @ViewChild(YearEndActivitiesComponent) activitiesComponent!: YearEndActivitiesComponent;

  private subscriptions = new Subscription();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly permissionsService: PermissionsService,
    private readonly projectService: ProjectService,
    private readonly snackbar: MatSnackBar,
    private readonly fb: FormBuilder,
    private readonly codeTableService: CodeTableServices
  ) {}

  ngOnInit(): void {
    if (!this.permissionsService.hasAction(WFPREV_ACTIONS.CREATE_YEAR_END_REPORT)) {
      this.router.navigate(['/' + ResourcesRoutes.ERROR_PAGE]);
      return;
    }

    this.projectGuid = this.route.snapshot.queryParamMap.get('projectGuid') || '';
    this.fiscalGuid = this.route.snapshot.queryParamMap.get('fiscalGuid') || '';
    this.workflow = this.route.snapshot.queryParamMap.get('workflow') || 'update';

    this.initForm();
    this.loadActivities();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  initForm(): void {
    this.summaryForm = this.fb.group({
      planFiscalStatusCode: ['', Validators.required], 
      fiscalReportedSpendAmount: [0, [Validators.required, Validators.min(0)]],
      fiscalActualAmount: [0, [Validators.required, Validators.min(0)]],
      fiscalCompletedSizeHa: [0, [Validators.required, Validators.min(0)]],
      outcomeComment: ['', Validators.required],
      spatialFileUploaded: [false, Validators.requiredTrue],
      acknowledgement: [false, Validators.requiredTrue]
    });
  }

  loadActivities(): void {
    if (!this.projectGuid || !this.fiscalGuid) {
      this.isLoading = false;
      return;
    }

    this.isLoading = true;

    // Fetch everything we need: fiscal data, activities, closeout data, and activity statuses
    const requests = {
      fiscal: this.projectService.getProjectFiscalByProjectPlanFiscalGuid(this.projectGuid, this.fiscalGuid),
      activities: this.projectService.getFiscalActivities(this.projectGuid, this.fiscalGuid),
      closeouts: this.projectService.getAllFiscalCloseouts(this.projectGuid, this.fiscalGuid),
      statuses: this.codeTableService.fetchCodeTable('planFiscalStatusCodes')
    };

    const sub = forkJoin(requests).pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (responses: any) => {
          this.fiscalData = responses.fiscal;
          
          const activities = responses.activities?._embedded?.activities || [];
          this.activityViews = activities.map((activity: any, index: number) => ({
            data: activity,
            isExpanded: index === 0
          }));

          const closeouts = responses.closeouts?._embedded?.fiscalCloseouts || [];
          if (closeouts.length > 0) {
            this.closeoutData = closeouts[0];
          }

          if (responses.statuses && responses.statuses._embedded && responses.statuses._embedded.planFiscalStatusCode) {
            this.planFiscalStatusCodes = [...responses.statuses._embedded.planFiscalStatusCode].sort((a: any, b: any) => (a.description ?? '').localeCompare(b.description ?? ''));
          }

          this.patchForm();
        },
        error: (err: any) => {
          console.error('Failed to load data', err);
        }
      });
      
    this.subscriptions.add(sub);
  }

  patchForm(): void {
    let defaultStatus = this.fiscalData?.planFiscalStatusCode?.planFiscalStatusCode || '';
    if (this.workflow === 'cancel') {
        defaultStatus = FiscalStatuses.CANCELLED;
    } else if (this.workflow === 'update') {
        defaultStatus = FiscalStatuses.COMPLETE;
    }

    this.summaryForm.patchValue({
      planFiscalStatusCode: defaultStatus,
      fiscalReportedSpendAmount: this.fiscalData?.fiscalReportedSpendAmount || 0,
      fiscalActualAmount: this.fiscalData?.fiscalActualAmount || 0,
      fiscalCompletedSizeHa: this.fiscalData?.fiscalCompletedSizeHa || 0,
      outcomeComment: this.closeoutData?.outcomeComment || ''
    });
  }

  get outcomeCommentControl(): import('@angular/forms').FormControl {
    return this.summaryForm.get('outcomeComment') as import('@angular/forms').FormControl;
  }

  get isFormValid(): boolean {
    return this.summaryForm.valid && (!this.activitiesComponent || this.activitiesComponent.isAllValid());
  }

  onSubmitSummary(): void {
    if (!this.fiscalData || !this.summaryForm.valid) return;
    this.isSavingSummary = true;
    
    console.log("this.fiscalData.planFiscalStatusCode: ", this.fiscalData.planFiscalStatusCode )
    // Update ProjectFiscal
    const updatedFiscal = {
      ...this.fiscalData,
      fiscalReportedSpendAmount: this.summaryForm.value.fiscalReportedSpendAmount,
      fiscalActualAmount: this.summaryForm.value.fiscalActualAmount,
      fiscalCompletedSizeHa: this.summaryForm.value.fiscalCompletedSizeHa,
      planFiscalStatusCode: this.summaryForm.value.planFiscalStatusCode
        ? { planFiscalStatusCode: this.summaryForm.value.planFiscalStatusCode }
        : null,
      endorsementCode: this.fiscalData.endorsementCode 
        ? { endorsementCode: this.fiscalData.endorsementCode.endorsementCode } 
        : null
    };

    // Update activities
    const activities = (this.activitiesComponent ? this.activitiesComponent.getActivityData() : [])
      .map((activity: any) => {
        const { activityDateRange, ...activityWithoutDateRange } = activity;
        return {
          ...activityWithoutDateRange,
          activityStartDate: activityDateRange?.activityStartDate ? new Date(activityDateRange.activityStartDate).toISOString() : null,
          activityEndDate: activityDateRange?.activityEndDate ? new Date(activityDateRange.activityEndDate).toISOString() : null,
          activityStatusCode: activity.activityStatusCode
            ? { activityStatusCode: typeof activity.activityStatusCode === 'string' ? activity.activityStatusCode : activity.activityStatusCode.activityStatusCode }
            : null,
          riskRatingCode: activity.riskRatingCode
            ? { riskRatingCode: activity.riskRatingCode.riskRatingCode }
            : null,
          contractPhaseCode: activity.contractPhaseCode
            ? { contractPhaseCode: activity.contractPhaseCode.contractPhaseCode }
            : null
        };
      });

    const payload = {
      projectFiscal: updatedFiscal,
      closeout: {
        outcomeComment: this.summaryForm.value.outcomeComment
      },
      activities: activities
    };

    const sub = this.projectService.submitFiscalCloseout(this.projectGuid, this.fiscalGuid, payload)
      .pipe(finalize(() => this.isSavingSummary = false))
      .subscribe({
        next: () => {
          this.snackbar.open('Year End Update saved successfully', 'Close', { duration: 3000, panelClass: 'snackbar-success'});
          this.loadActivities();
        },
        error: (err: any) => {
          console.error('Failed to save summary', err);
          this.snackbar.open('Failed to save Year End Update', 'Close', { duration: 3000, panelClass: 'snackbar-error' });
        }
      });
    this.subscriptions.add(sub);
  }

  onSaveActivity(view: YearEndActivityViewModel, updatedActivity: any): void {
    const originalActivity = view.data;
    const payload = {
      ...originalActivity,
      ...updatedActivity
    };

    const sub = this.projectService.updateFiscalActivities(
      this.projectGuid,
      this.fiscalGuid,
      originalActivity.activityGuid,
      payload
    ).subscribe({
        next: (response: any) => {
          this.snackbar.open('Activity updated successfully', 'Close', { duration: 3000, panelClass: 'snackbar-success' });
          this.loadActivities(); // Reload to get fresh data
        },
        error: (err: any) => {
          console.error('Failed to update activity', err);
          this.snackbar.open('Failed to update activity', 'Close', { duration: 3000, panelClass: 'snackbar-error' });
        }
      });

    this.subscriptions.add(sub);
  }

  onFilesUpdated(): void {
    this.loadActivities();
  }

  goBack(): void {
    this.router.navigate(['/' + ResourcesRoutes.EDIT_PROJECT], {
      queryParams: {
        projectGuid: this.projectGuid,
        fiscalGuid: this.fiscalGuid
      }
    });
  }
}
