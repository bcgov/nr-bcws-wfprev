import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'wfprev-timestamp',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './timestamp.component.html',
  styleUrl: './timestamp.component.scss'
})
export class TimestampComponent {
  @Input() updateDate?: string;
  @Input() updateUser?: string;
}
