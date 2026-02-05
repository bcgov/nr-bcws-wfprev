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
    
    const forecastValue = revisedForecastCtrl?.value ?? currentForecastCtrl?.value;

    if (forecastValue == null || forecastValue === '') {
      return { totalMismatch: true };
    }

    const entered = Number(forecastValue);

    if (Number.isNaN(entered)) {
      return { totalMismatch: true };
    };
    

    return entered === totalAmound
      ? null
      : { totalMismatch: true };
  };
}