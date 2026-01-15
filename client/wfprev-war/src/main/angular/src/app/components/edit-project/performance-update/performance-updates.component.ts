import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatExpansionPanel, MatExpansionPanelHeader } from '@angular/material/expansion';
import { ExpansionIndicatorComponent } from "../../shared/expansion-indicator/expansion-indicator.component";
import { IconButtonComponent } from "../../shared/icon-button/icon-button.component";
import { ProjectService } from 'src/app/services/project-services';
import { PerformanceUpdate, ForecastStatus, TimeStatus, ProgressStatus } from '../../models';
import { ActivatedRoute } from '@angular/router';

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
  protected readonly TimeStatus = TimeStatus;
  protected readonly ProgressStatus = ProgressStatus;


  updates: PerformanceUpdate[] = [];


  constructor(
    private readonly projectService: ProjectService,
    private readonly route: ActivatedRoute
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscalGuid'] && changes['fiscalGuid'].currentValue) {
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

}
