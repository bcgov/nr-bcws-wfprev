import { AbstractControl, FormGroup, ValidationErrors } from '@angular/forms';
import { isEmpty } from './tools';

/**
 * Custom validator that requires a value to be non negative (0 or positive)
 */
export function nonNegativeValidator(getFormFn: () => FormGroup) {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.parent) return null;
    const form = getFormFn();
    const val = Number(control.value);
    
    // If empty, let Validators.required handle it
    if (isEmpty(control.value)) return null;
    else return val < 0 ? { min: true } : null; // allow 0
  };
}
