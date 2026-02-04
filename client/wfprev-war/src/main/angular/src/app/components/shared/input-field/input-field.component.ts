import { Component, Input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'wfprev-input-field',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatTooltipModule],
  templateUrl: './input-field.component.html',
  styleUrls: ['./input-field.component.scss'],
})
export class InputFieldComponent {
  @Input() control!: FormControl;
  @Input() label = '';
  @Input() placeholder = '';
  @Input() id = '';
  @Input() required = false;
  @Input() tooltip: string | null = null;
  @Input() type: 'text' | 'number' | 'email' = 'text';  
  @Input() errorMessages: { [key: string]: string } = {};
  @Input() prefix: string | null = null;
  @Input() img: string | null = null;
}
