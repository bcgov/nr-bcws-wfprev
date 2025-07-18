import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'wfprev-slide-toggle',
  standalone: true,
  imports: [CommonModule, MatSlideToggleModule],
  templateUrl: './slide-toggle.component.html',
  styleUrl: './slide-toggle.component.scss'
})
export class SlideToggleComponent {
  @Input() label: string = '';
  @Input() checked: boolean = false;
  @Output() checkedChange = new EventEmitter<boolean>();

  onToggleChange(event: any) {
    this.checkedChange.emit(event.checked);
  }
}