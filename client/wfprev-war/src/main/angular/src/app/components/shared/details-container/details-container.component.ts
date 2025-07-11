import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'wfprev-details-container',
  standalone: true,
  imports: [],
  templateUrl: './details-container.component.html',
  styleUrls: ['./details-container.component.scss']
})
export class DetailsContainerComponent {
  @Input() cancelText = 'Cancel';
  @Input() saveText = 'Save';
  @Input() saveDisabled = false;
  @Input() cancelDisabled = false;
  @Input() title: string = '';

  @Output() save = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}
