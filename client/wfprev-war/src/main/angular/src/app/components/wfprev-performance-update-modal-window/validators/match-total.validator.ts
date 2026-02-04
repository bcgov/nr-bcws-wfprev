import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export function matchTotalValidator(totalAmountCtrlName: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const totalAmound = group.get(totalAmountCtrlName)?.value;
    const revisedForecastCtrl = group.get('revisedForecast');
    const currentForecastCtrl = group.get('currentForecast');

    if (!revisedForecastCtrl && !currentForecastCtrl) {
      // optional: keep or remove logging
      console.warn(
        '[matchTotalValidator] Controls not found: revisedForecast, currentForecast'
      );
      return null;
    }

    const enteredValue = revisedForecastCtrl?.value ?? currentForecastCtrl?.value;

    if (enteredValue == null || enteredValue === '') {
      return null;
    }

    const entered = Number(enteredValue);

    if (Number.isNaN(entered)) {
      return null;
    }

    return entered === totalAmound
      ? null
      : { totalMismatch: true };
  };
}