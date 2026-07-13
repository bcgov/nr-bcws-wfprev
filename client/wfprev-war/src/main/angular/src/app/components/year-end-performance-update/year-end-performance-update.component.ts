import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { NgxCurrency } from '@dintecom/ngx-currency';
import { Observable, Subscription, finalize, forkJoin } from 'rxjs';
import { FiscalCloseout, ProjectFiscalExtended, YearEndActivityViewModel } from 'src/app/components/models';
import { TextareaComponent } from 'src/app/components/shared/textarea/textarea.component';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { PermissionsService, WFPREV_ACTIONS } from 'src/app/services/permissions.service';
import { ProjectService } from 'src/app/services/project-services';
import { ResourcesRoutes } from 'src/app/utils';
import { ModalMessages, ModalTitles, StatusManagementStatuses } from 'src/app/utils/constants';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { IconDisplayFieldComponent } from '../shared/icon-display-field/icon-display-field.component';
import { YearEndActivitiesComponent } from './year-end-activities/year-end-activities.component';



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
  projectName: string = '';
  @ViewChild(YearEndActivitiesComponent) activitiesComponent!: YearEndActivitiesComponent;

  private subscriptions = new Subscription();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly permissionsService: PermissionsService,
    private readonly projectService: ProjectService,
    private readonly snackbar: MatSnackBar,
    private readonly fb: FormBuilder,
    private readonly codeTableService: CodeTableServices,
    public readonly dialog: MatDialog,
  ) { }

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

  loadActivities(preserveState: boolean = false): void {
    if (!this.projectGuid || !this.fiscalGuid) {
      this.isLoading = false;
      return;
    }

    this.isLoading = true;

    // Fetch everything we need: fiscal data, activities, closeout data, and activity statuses
    const requests = {
      project: this.projectService.getProjectByProjectGuid(this.projectGuid),
      fiscal: this.projectService.getProjectFiscalByProjectPlanFiscalGuid(this.projectGuid, this.fiscalGuid),
      activities: this.projectService.getFiscalActivities(this.projectGuid, this.fiscalGuid),
      closeouts: this.projectService.getAllFiscalCloseouts(this.projectGuid, this.fiscalGuid),
      statuses: this.codeTableService.fetchCodeTable('planFiscalStatusCodes')
    };

    const sub = forkJoin(requests).pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (responses: any) => {
          this.fiscalData = responses.fiscal;
          this.projectName = responses.project?.projectName || '';
          const activities = responses.activities?._embedded?.activities || [];
          activities.sort((a: any, b: any) => {
                const nameA = (a.activityName || '').toLowerCase();
                const nameB = (b.activityName || '').toLowerCase();
                return nameA.localeCompare(nameB);
              });
              
          const closeouts = responses.closeouts?._embedded?.fiscalCloseouts || [];
          if (closeouts.length > 0) {
            this.closeoutData = closeouts[0];
          }

          // display only COMPLETE and CANCELLED in the dropdown, and ensure COMPLETE is listed first
          if (responses.statuses?._embedded?.planFiscalStatusCode) {
            const fiscalStatusCodeOrder = [StatusManagementStatuses.COMPLETE, StatusManagementStatuses.CANCELLED];
            this.planFiscalStatusCodes = [...responses.statuses._embedded.planFiscalStatusCode]
              .filter((s: any) => fiscalStatusCodeOrder.includes(s.planFiscalStatusCode))
              .sort((a: any, b: any) => fiscalStatusCodeOrder.indexOf(a.planFiscalStatusCode) - fiscalStatusCodeOrder.indexOf(b.planFiscalStatusCode));
          }

          if (preserveState) {
            this.activityViews.forEach(view => {
              const updatedActivity = activities.find((a: any) => a.activityGuid === view.data.activityGuid);
              if (updatedActivity) {
                Object.assign(view.data, updatedActivity);
              }
            });

            if (this.activitiesComponent && this.activitiesComponent.activityItems) {
              this.activitiesComponent.activityItems.forEach(item => {
                item.updateMissingInfo();
              });
            }
          } else {
            this.activityViews = activities.map((activity: any, index: number) => ({
              data: activity,
              isExpanded: index === 0
            }));
            this.patchForm();
          }
        },
        error: (err: any) => {
          console.error('Failed to load data', err);
        }
      });

    this.subscriptions.add(sub);
  }

  patchForm(): void {
    // Default Fiscal Status dropdown value to COMPLETE unless the fiscal is already CANCELLED, in which case default to CANCELLED
    let defaultStatus = this.fiscalData?.planFiscalStatusCode?.planFiscalStatusCode === StatusManagementStatuses.CANCELLED ? StatusManagementStatuses.CANCELLED : StatusManagementStatuses.COMPLETE;

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
        const { activityDateRange, isMissingInfo, ...activityWithoutDateRange } = activity;
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
          this.snackbar.open('Year End Update saved successfully', 'Close', { duration: 3000, panelClass: 'snackbar-success' });
          this.loadActivities();
        },
        error: (err: any) => {
          console.error('Failed to save summary', err);
          this.snackbar.open('Failed to save Year End Update', 'Close', { duration: 3000, panelClass: 'snackbar-error' });
        }
      });
    this.subscriptions.add(sub);
    this.goBack(false);
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
        if (this.activitiesComponent && this.activitiesComponent.activityItems) {
          const item = this.activitiesComponent.activityItems.find(i => i.activity.activityGuid === view.data.activityGuid);
          if (item) {
            item.form.markAsPristine();
          }
        }
        this.loadActivities(true); // Reload to get fresh data but preserve form states
      },
      error: (err: any) => {
        console.error('Failed to update activity', err);
        this.snackbar.open('Failed to update activity', 'Close', { duration: 3000, panelClass: 'snackbar-error' });
      }
    });

    this.subscriptions.add(sub);
  }

  onFilesUpdated(): void {
    this.loadActivities(true);
  }

  goBack(checkFormDirty: boolean): void {
    const navigate = () => {
      this.router.navigate(['/' + ResourcesRoutes.EDIT_PROJECT], {
        queryParams: {
          projectGuid: this.projectGuid,
          fiscalGuid: this.fiscalGuid,
          tab: 'fiscal'
        }
      });
    };

    if (checkFormDirty && this.isFormDirty()) {
      this.confirmDiscardChanges().subscribe(confirmed => {
        if (confirmed) {
          navigate();
        }
      });
    } else {
      navigate();
    }
  }

  isFormDirty(): boolean {
    return this.summaryForm.dirty || (!!this.activitiesComponent && this.activitiesComponent.isAnyDirty());
  }

  confirmDiscardChanges(): Observable<boolean> {
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
}
