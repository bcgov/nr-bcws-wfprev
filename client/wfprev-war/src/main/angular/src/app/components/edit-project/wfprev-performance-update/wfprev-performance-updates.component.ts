import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatExpansionPanel, MatExpansionPanelHeader } from '@angular/material/expansion';
import { ExpansionIndicatorComponent } from "../../shared/expansion-indicator/expansion-indicator.component";
import { IconButtonComponent } from "../../shared/icon-button/icon-button.component";
import { ProjectService } from 'src/app/services/project-services';
import { PerformanceUpdate, ForecastStatus, ProgressStatus, UpdateGeneralStatus, Option, ReportingPeriod, ReportingPeriodCode, ProgressStatusCode } from '../../models';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { PerformanceUpdateModalWindowComponent } from '../../wfprev-performance-update-modal-window/wfprev-performance-update-modal-window.component';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { CodeTableNames } from 'src/app/utils/constants';
import { ProjectFiscalsSignalService } from 'src/app/services/project-fiscals-signal.service';

@Component({
  selector: 'wfprev-performance-updates',
  standalone: true,
  imports: [CommonModule, MatExpansionPanel, ExpansionIndicatorComponent, MatExpansionPanelHeader, IconButtonComponent],
  templateUrl: './wfprev-performance-updates.component.html',
  styleUrl: './wfprev-performance-updates.component.scss'
})
export class PerformanceUpdatesComponent implements OnChanges {
  @Input() fiscalGuid: string = '';
  @Input() projectGuid = '';
  @Input() currentForecast: string = '';
  @Input() originalCostEstimate: string = ''

  protected readonly ForecastStatus = ForecastStatus;
  protected readonly ProgressStatus = ProgressStatus;
  protected readonly UpdateGeneralStatus = UpdateGeneralStatus;

  protected readonly delayedProgressStatus: Option<ProgressStatus> = { value: ProgressStatus.Delayed, description: 'Delayed' }
  protected readonly onTrackProgressStatus: Option<ProgressStatus> = { value: ProgressStatus.OnTrack, description: 'On track' }
  protected readonly deferredProgressStatus: Option<ProgressStatus> = { value: ProgressStatus.Deferred, description: 'Deferred' }
  protected readonly cancelledProgressStatus: Option<ProgressStatus> = { value: ProgressStatus.Cancelled, description: 'Cancelled' }
  
  protected readonly other: Option<ReportingPeriod> = { value: ReportingPeriod.Custom, description: 'Other' }
  protected readonly march7: Option<ReportingPeriod> = { value: ReportingPeriod.March7, description: 'March 7' }
  protected readonly q1: Option<ReportingPeriod> = { value: ReportingPeriod.Q1, description: 'End of First Quarter' }
  protected readonly q2: Option<ReportingPeriod> = { value: ReportingPeriod.Q2, description: 'End of Second Quarter' }
  protected readonly q3: Option<ReportingPeriod> = { value: ReportingPeriod.Q3, description: 'End of Third Quarter' }
  
  protected readonly cancelledUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Cancelled, description: 'Cancelled' }
  protected readonly completeUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Complete, description: 'Complete' }
  protected readonly draftUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Draft, description: 'Draft' }
  protected readonly inProgressUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.InProgress, description: 'In Progress' }
  protected readonly preparedUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Prepared, description: 'Prepared' }
  protected readonly proposedUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Proposed, description: 'Proposed' }



  updates: PerformanceUpdate[] = [];
  isUpdatesCalled: boolean = false;

  constructor(
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    private readonly projectFiscalsSignalService: ProjectFiscalsSignalService,
    private readonly route: ActivatedRoute,
    private dialog: MatDialog,
    private readonly snackbarService: MatSnackBar
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscalGuid'] && changes['fiscalGuid'].currentValue && !this.isUpdatesCalled) {
      this.isUpdatesCalled = true;
      this.getPerformanceUpdates();
    }
  }

  getPerformanceUpdates(): void {
    if (this.fiscalGuid) {
      this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';

      if (this.projectGuid) {
        this.projectService.getPerformanceUpdates(this.projectGuid, this.fiscalGuid).subscribe({
          next: (data) => {
            this.updates = data?._embedded?.performanceUpdate ?? [];
          },
          error: (error) => {
            console.error('Error fetching performance updates:', error);

            this.snackbarService.open(
              'Failed to load performance updates. Please try again later.',
              'OK',
              { duration: 5000, panelClass: 'snackbar-error' }
            );
          }
        });
      }
    }
  }

  openDialog(): void {
    forkJoin({
      projectFiscal: this.projectService.getProjectFiscalByProjectPlanFiscalGuid(this.projectGuid, this.fiscalGuid),
      reportingPeriod: this.codeTableService.fetchCodeTable(CodeTableNames.REPORTING_PERIOD_CODE),
      progressStatus: this.codeTableService.fetchCodeTable(CodeTableNames.PROGRESS_STATUS_CODE)
    })
    .subscribe({
        next: ({projectFiscal, reportingPeriod, progressStatus}) => {
          const dialogRef = this.dialog.open(PerformanceUpdateModalWindowComponent,
            {
              data: {
                projectGuid: this.projectGuid,
                fiscalGuid: this.fiscalGuid,
                currentForecast: projectFiscal.fiscalForecastAmount,
                originalCostEstimate: projectFiscal.totalCostEstimateAmount,
                projectFiscalName: projectFiscal.projectFiscalName,
                reportingPeriod: (reportingPeriod?._embedded?.reportingPeriodCode ?? [])
                  .sort((a: ReportingPeriodCode, b: ReportingPeriodCode) => a.displayOrder - b.displayOrder)
                  .map(({ description, reportingPeriodCode }: ReportingPeriodCode) => ({
                        value: reportingPeriodCode,
                        description
                      })),
                progressStatus: (progressStatus?._embedded?.progressStatusCode ?? [])
                  .sort((a: ProgressStatusCode, b: ProgressStatusCode) => a.displayOrder - b.displayOrder)
                  .map(({ description, progressStatusCode }: ProgressStatusCode) => ({
                        value: progressStatusCode,
                        description
                      }))
              },
              disableClose: true
            });
          dialogRef.afterClosed().subscribe(result => {
            if (result) {
              this.updates = [result, ...this.updates];
              if ((result as PerformanceUpdate).forecastAdjustmentAmount !== 0) {
                this.projectFiscalsSignalService.trigger();
              }
            } else {
              console.log('Dialog was closed without data');
            }
          });
        },
        error: (error) => {
          console.error('Error fetching performance updates estimates:', error);

          this.snackbarService.open(
            'Failed to load performance updates estimates. Please try again later.',
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
        }
      });
  }
}