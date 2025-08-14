import { TextFieldModule } from '@angular/cdk/text-field';
import { CommonModule } from '@angular/common';
import { Component, forwardRef, Input } from '@angular/core';
import { FormControl, FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Messages } from 'src/app/utils/constants';

@Component({
  selector: 'wfprev-textarea',
  templateUrl: './textarea.component.html',
  styleUrls: ['./textarea.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TextFieldModule, MatTooltipModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TextareaComponent),
      multi: true,
    },
  ],
})
export class TextareaComponent {
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() control!: FormControl;
  @Input() required: boolean = false;
  @Input() rows: number = 2;
  @Input() maxLength = 500;
  message = Messages;
  
  get charCount(): number {
    const v = this.control?.value;
    return typeof v === 'string' ? v.length : 0;
  }

  get isOverLimit(): boolean {
    if (!this.maxLength) return false;
    return this.charCount >= this.maxLength;
  }
  onInput(event: Event) {
    const value = (event.target as HTMLTextAreaElement).value;
    if (value.length > this.maxLength) {
      this.control.setValue(value.slice(0, this.maxLength), { emitEvent: false });
    }
  }

  onPaste(event: ClipboardEvent) {
    event.preventDefault();
    const pasteData = (event.clipboardData?.getData('text') || '').slice(0, this.maxLength);
    const currentValue = this.control.value || '';
    const newValue = (currentValue + pasteData).slice(0, this.maxLength);
    this.control.setValue(newValue);
  }
}
