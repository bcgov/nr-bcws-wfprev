import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'wfprev-select-field',
  standalone: true,
  templateUrl: './select-field.component.html',
  styleUrls: ['./select-field.component.scss'],
  imports: [MatTooltip, ReactiveFormsModule, CommonModule ],
})
export class SelectFieldComponent {
  @Input() disabled = false;
  @Input() label = '';
  @Input() control!: FormControl
  @Input() options: any[] = [];
  @Input() optionValueField = 'id';
  @Input() optionLabelField = 'label';
  @Input() required = false;
  @Input() tooltip: string | null = null;
  @Input() placeholder = 'Select';
  @Input() id = '';
  
}
