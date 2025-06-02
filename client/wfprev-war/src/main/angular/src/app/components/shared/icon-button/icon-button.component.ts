import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-icon-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './icon-button.component.html',
  styleUrls: ['./icon-button.component.scss']
})
export class IconButtonComponent {
  @Input() text: string = '';
  @Input() icon: string = '';
  @Input() alt: string = '';
  @Input() iconSize: number = 14;
  @Input() disabled: boolean = false;
  @Output() clicked = new EventEmitter<void>();

  onClick(event: MouseEvent): void {
    event.stopPropagation();
    if (!this.disabled) {
      this.clicked.emit();
    }
  }
}
