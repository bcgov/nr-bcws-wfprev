import { AbstractControl, FormGroup, ValidationErrors } from '@angular/forms';

/**
 * Custom validator that requires a value to be strictly positive (> 0)
 * unless the activity's status is 'CANCELLED', in which case it must be non-negative (>= 0).
 */
export function nonZeroUnlessCancelledValidator(getFormFn: () => FormGroup) {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.parent) return null;
    const form = getFormFn();
    const status = form?.get('activityStatusCode')?.value;
    const val = Number(control.value);
    
    // If empty, let Validators.required handle it
    if (control.value === null || control.value === '') return null;
    
    if (status === 'CANCELLED') {
      return val < 0 ? { min: true } : null; // allow 0
    } else {
      return val <= 0 ? { strictlyPositive: true } : null; // must be > 0
    }
  };
}
