import { Component, Input } from '@angular/core';

@Component({
  selector: 'wfprev-status-badge',
  templateUrl: './status-badge.component.html',
  styleUrls: ['./status-badge.component.scss'],
  imports: [],
})
export class StatusBadgeComponent {
  @Input() icon!: { src: string; alt: string; title: string } | null;
  @Input() label!: string;
}
