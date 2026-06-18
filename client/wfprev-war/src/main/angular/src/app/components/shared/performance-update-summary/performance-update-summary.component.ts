import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { YearEndPerformanceUpdateExtended } from '../../models';
import { StatusBadgeComponent } from '../status-badge/status-badge.component';

@Component({
  selector: 'wfprev-performance-update-summary',
  standalone: true,
  imports: [CommonModule, StatusBadgeComponent],
  templateUrl: './performance-update-summary.component.html',
  styleUrl: './performance-update-summary.component.scss'
})
export class PerformanceUpdateSummaryComponent {
  @Input() update!: YearEndPerformanceUpdateExtended;
}