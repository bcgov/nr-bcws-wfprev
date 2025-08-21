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
    // selection-aware paste handle
    const textArea = event.target as HTMLTextAreaElement;
    const existingText = this.control.value ?? '';
    const pastedText = event.clipboardData?.getData('text') ?? '';

    // figure out which part of the text is currently selected
    const selectionStart = textArea.selectionStart ?? existingText.length;
    const selectionEnd = textArea.selectionEnd ?? selectionStart;
    const selectionLength = selectionEnd - selectionStart;
    const availableSpace = this.maxLength
      ? Math.max(0, this.maxLength - (existingText.length - selectionLength))
      : Infinity;
    const textToInsert =
      availableSpace === Infinity ? pastedText : pastedText.slice(0, availableSpace);

    const newValue =
      existingText.slice(0, selectionStart) +
      textToInsert +
      existingText.slice(selectionEnd);

    event.preventDefault();
    this.control.setValue(newValue);
    this.control.markAsDirty();
    this.control.markAsTouched();

    queueMicrotask(() => {
      const cursorPosition = selectionStart + textToInsert.length;
      textArea.selectionStart = textArea.selectionEnd = cursorPosition;
    });
  }

}
