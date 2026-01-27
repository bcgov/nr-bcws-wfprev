import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatExpansionPanel, MatExpansionPanelHeader } from '@angular/material/expansion';
import { ExpansionIndicatorComponent } from "../../shared/expansion-indicator/expansion-indicator.component";
import { IconButtonComponent } from "../../shared/icon-button/icon-button.component";
import { ProjectService } from 'src/app/services/project-services';
import { PerformanceUpdate, ForecastStatus, ProgressStatus, UpdateGeneralStatus, Option } from '../../models';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { PerformanceUpdateModalWindowComponent } from '../../wfprev-performance-update-modal-window/wfprev-performance-update-modal-window.component';
import { CommonModule } from '@angular/common';

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

  protected readonly delayed: Option<ProgressStatus> = { value: ProgressStatus.Delayed, description: 'Delayed' }
  protected readonly onTrack: Option<ProgressStatus> = { value: ProgressStatus.OnTrack, description: 'On track' }
  protected readonly deffered: Option<ProgressStatus> = { value: ProgressStatus.Deffered, description: 'Deffered' }
  protected readonly cancelled: Option<ProgressStatus> = { value: ProgressStatus.Cancelled, description: 'Cancelled' }
  
  updates: PerformanceUpdate[] = [];
  isUpdatesCalled: boolean = false;

  constructor(
    private readonly projectService: ProjectService,
    private readonly route: ActivatedRoute,
    private dialog: MatDialog,
  ) { 
    console.info('PerformanceUpdatesComponent constructor called')
  }

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
            this.updates = data;
            console.info('JSON responce is:', data);

          },
          error: (error) => {
            console.error('Error fetching performance updates:', error);
          }
        });
      }
    }
  }

  openDialog(): void {
    this.projectService.getPerformanceUpdatesEstimates(this.projectGuid, this.fiscalGuid)
      .subscribe({
        next: (data) => {
          const dialogRef = this.dialog.open(PerformanceUpdateModalWindowComponent,
            {
              data: {
                projectGuid: this.projectGuid,
                fiscalGuid: this.fiscalGuid,
                currentForecast: data.currentForecast,
                originalCostEstimate: data.originalCostEstimate
              },
              disableClose: true
            });

        },
        error: (error) => {
          console.error('Error fetching performance updates:', error);
        }
      });
  }
}