import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'wfprev-textarea',
  templateUrl: './textarea.component.html',
  styleUrls: ['./textarea.component.scss'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule]
})
export class TextareaComponent {
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() control!: FormControl;
  @Input() required: boolean = false;
  @Input() rows: number = 4;
}
