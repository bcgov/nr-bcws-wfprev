import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'wfprev-detail-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './detail-button.component.html',
  styleUrls: ['./detail-button.component.scss']
})
export class DetailButtonComponent {
  @Input() text: string = 'View Details';
  @Input() disabled: boolean = false;
  @Output() clicked = new EventEmitter<MouseEvent>();

  onClick(event: MouseEvent): void {
    event.stopPropagation();
    if (!this.disabled) {
      this.clicked.emit(event);
    }
  }
}
