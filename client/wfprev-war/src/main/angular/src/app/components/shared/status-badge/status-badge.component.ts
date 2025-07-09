import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'wfprev-status-badge',
  templateUrl: './status-badge.component.html',
  styleUrls: ['./status-badge.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class StatusBadgeComponent {
  @Input() icon!: { src: string; alt: string; title: string } | null;
  @Input() label!: string;
}
