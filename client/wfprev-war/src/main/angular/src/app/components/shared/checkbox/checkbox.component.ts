import { Component, inject, Input } from '@angular/core';
import { ControlContainer, FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';

@Component({
  selector: 'wfprev-checkbox',
  standalone: true,
  imports: [ReactiveFormsModule, MatCheckboxModule],
  templateUrl: './checkbox.component.html',
  styleUrl: './checkbox.component.scss'
})
export class CheckboxComponent {
  @Input() formControlName!: string;
  @Input() disabled = false;
  private readonly controlContainer = inject(ControlContainer);

  get control(): FormControl {
    return this.controlContainer?.control?.get(this.formControlName) as FormControl;
  }
}
