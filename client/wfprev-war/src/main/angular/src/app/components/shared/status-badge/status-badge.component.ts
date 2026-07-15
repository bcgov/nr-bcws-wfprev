
import { Component, HostBinding, Input } from '@angular/core';

@Component({
    selector: 'wfprev-status-badge',
    templateUrl: './status-badge.component.html',
    styleUrls: ['./status-badge.component.scss'],
    standalone: true,
    imports: []
})
export class StatusBadgeComponent {
  @Input() type?: string;
  @Input() label?: string;
  @Input() icon?: { src: string; alt: string; title: string } | null;

  // Apply the type as a CSS class for styling
  @HostBinding('class')
  get hostClass(): string {
    return this.type ?? '';
  }

  private statusMap: { [key: string]: { icon: string; label: string } } = {
    'on-track': { icon: '/assets/progress-status-ontrack.svg', label: 'On track' },
    'delayed': { icon: '/assets/progress-status-delayed.svg', label: 'Delayed' },
    'deferred': { icon: '/assets/progress-status-deferred.svg', label: 'Deferred' },
    'deferred-filled': { icon: '/assets/deferred.svg', label: 'Deferred' },
    'cancelled': { icon: '/assets/progress-status-cancelled.svg', label: 'Cancelled' },
    'forecast-decreased': { icon: '/assets/budget-change-decreased.svg', label: 'Forecast Decreased' },
    'forecast-increased': { icon: '/assets/budget-change-increased.svg', label: 'Forecast Increased' },
    'status-cancelled': { icon: '/assets/cancelled-icon.svg', label: 'Cancelled' },
    'status-complete': { icon: '/assets/complete-icon.svg', label: 'Complete' },
    'status-draft': { icon: '/assets/draft-icon.svg', label: 'Draft' },
    'status-in_prog': { icon: '/assets/in-progress-icon-only.svg', label: 'In Progress' },
    'status-in-progress': { icon: '/assets/in-progress-icon-only.svg', label: 'In Progress' },
    'status-prepared': { icon: '/assets/prepared-icon.svg', label: 'Prepared' },
    'status-proposed': { icon: '/assets/proposed-icon.svg', label: 'Proposed' },
    'risk-high': { icon: '/assets/high-risk.svg', label: 'High risk' },
    'risk-medium': { icon: '/assets/medium-risk.svg', label: 'Medium risk' },
    'risk-low': { icon: '/assets/low-risk.svg', label: 'Low risk' },
    'risk-none': { icon: '/assets/none-risk.svg', label: 'Complete' },
    'spatial-added': { icon: '/assets/spatial-added-icon.svg', label: 'Spatial Added' },
    'spatial-not-added': { icon: '/assets/no-spatial-added-icon.svg', label: 'No Spatial Added' },
    'substantially-complete': { icon: '/assets/substantially-complete.svg', label: 'Substantially Complete' },
    'warning': { icon: '/assets/warning.svg', label: 'Warning' },
    'missing-info': { icon: '/assets/warning-red.svg', label: 'Missing info' },
    'results-reportable': { icon: '/assets/blue-checkmark.svg', label: 'RESULTS Reportable' }
  };

  get displayIcon(): string | null {
    if (this.icon) return this.icon.src;
    if (this.type && this.statusMap[this.type]) return this.statusMap[this.type].icon;
    return null;
  }

  get displayLabel(): string {
    if (this.label) return this.label;
    if (this.type && this.statusMap[this.type]) return this.statusMap[this.type].label;
    return '';
  }
}
