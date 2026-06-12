import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionPanel, MatExpansionPanelHeader } from '@angular/material/expansion';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { PermissionsService, WFPREV_ACTIONS } from 'src/app/services/permissions.service';
import { ProjectFiscalsSignalService } from 'src/app/services/project-fiscals-signal.service';
import { ProjectService } from 'src/app/services/project-services';
import { PerformanceUpdate, PerformanceUpdateExtended, ProgressStatusCode, ReportingPeriodCode } from '../../models';
import { ExpansionIndicatorComponent } from "../../shared/expansion-indicator/expansion-indicator.component";
import { IconButtonComponent } from "../../shared/icon-button/icon-button.component";
import { PerformanceUpdateHeaderComponent } from '../../shared/performance-update-header/performance-update-header.component';
import { StatusBadgeComponent } from '../../shared/status-badge/status-badge.component';
import { PerformanceUpdateModalWindowComponent } from '../../wfprev-performance-update-modal-window/wfprev-performance-update-modal-window.component';

@Component({
    selector: 'wfprev-performance-updates',
    standalone: true,
    imports: [CommonModule, MatExpansionPanel, ExpansionIndicatorComponent, MatExpansionPanelHeader, IconButtonComponent, StatusBadgeComponent, MatProgressSpinnerModule, PerformanceUpdateHeaderComponent],
    templateUrl: './wfprev-performance-updates.component.html',
    styleUrl: './wfprev-performance-updates.component.scss'
})
export class PerformanceUpdatesComponent implements OnChanges {
  @Input() fiscalGuid: string = '';
  @Input() projectGuid = '';
  @Input() currentForecast: string = '';
  @Input() originalCostEstimate: string = ''

  updates: PerformanceUpdateExtended[] = [];
  isUpdatesCalled: boolean = false;
  isLoading = true;

  get canCreatePerformanceUpdate(): boolean {
    return this.permissionsService.hasAction(WFPREV_ACTIONS.CREATE_PERFORMANCE_UPDATE);
  }

  get canCreateYearEndReport(): boolean {
    return this.permissionsService.hasAction(WFPREV_ACTIONS.CREATE_YEAR_END_REPORT);
  }

  constructor(
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    private readonly projectFiscalsSignalService: ProjectFiscalsSignalService,
    private readonly route: ActivatedRoute,
    private dialog: MatDialog,
    private readonly snackbarService: MatSnackBar,
    private readonly permissionsService: PermissionsService,
    private readonly router: Router
  ) {}

  navigateToYearEnd(): void {
    this.router.navigate(['/performance/year-end'], {
      queryParams: {
        projectGuid: this.projectGuid,
        fiscalGuid: this.fiscalGuid
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscalGuid']?.currentValue && !this.isUpdatesCalled) {
      this.isUpdatesCalled = true;
      this.getPerformanceUpdates();
    } else if (changes['fiscalGuid'] && !changes['fiscalGuid'].currentValue) {
      this.isLoading = false;
    }
  }

  getPerformanceUpdates(): void {
    if (this.fiscalGuid) {
      this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';

      if (this.projectGuid) {
        this.projectService.getPerformanceUpdates(this.projectGuid, this.fiscalGuid)
          .pipe(finalize(() => this.isLoading = false))
          .subscribe({
            next: (data: any) => {
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
      } else {
        this.isLoading = false;
      }
    } else {
      this.isLoading = false;
    }
  }

  openDialog(): void {
    forkJoin({
      projectFiscal: this.projectService.getProjectFiscalByProjectPlanFiscalGuid(this.projectGuid, this.fiscalGuid),
      reportingPeriod: this.codeTableService.getReportingPeriodCodes(),
      progressStatus: this.codeTableService.getProgressStatusCodes()
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
                reportingPeriod: reportingPeriod
                  .map(({ description, reportingPeriodCode }: ReportingPeriodCode) => ({
                    value: reportingPeriodCode,
                    description
                  })),
                progressStatus: progressStatus
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