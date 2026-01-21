import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatExpansionPanel, MatExpansionPanelHeader } from '@angular/material/expansion';
import { ExpansionIndicatorComponent } from "../../shared/expansion-indicator/expansion-indicator.component";
import { IconButtonComponent } from "../../shared/icon-button/icon-button.component";
import { ProjectService } from 'src/app/services/project-services';
import { PerformanceUpdate, ForecastStatus, ProgressStatus, UpdateGeneralStatus } from '../../models';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { PerformanceUpdateModalWindowComponent } from '../../performance-update-modal-window/performance-update-modal-window.component';

@Component({
  selector: 'wfprev-performance-updates',
  standalone: true,
  imports: [MatExpansionPanel, ExpansionIndicatorComponent, MatExpansionPanelHeader, IconButtonComponent],
  templateUrl: './performance-updates.component.html',
  styleUrl: './performance-updates.component.scss'
})
export class PerformanceUpdatesComponent implements OnChanges {
  @Input() fiscalGuid: string = '';

  projectGuid = '';

  protected readonly ForecastStatus = ForecastStatus;
  protected readonly ProgressStatus = ProgressStatus;
  protected readonly UpdateGeneralStatus = UpdateGeneralStatus;


  updates: PerformanceUpdate[] = [];
  isUpdatesCalled: boolean = false;

  constructor(
    private readonly projectService: ProjectService,
    private readonly route: ActivatedRoute,
    private dialog: MatDialog,
  ) { }

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
    const dialogRef = this.dialog.open(PerformanceUpdateModalWindowComponent,
      {
      data: { message: 'Are you sure?' },
      disableClose: true, // optional
    });
  }

  postPerformanceUpdates(): void {

  }

  putPerformanceUpdates(): void {

  }
  
}
