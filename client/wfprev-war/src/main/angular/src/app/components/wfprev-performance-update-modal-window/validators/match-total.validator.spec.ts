import { FormControl, FormGroup } from '@angular/forms';
import { matchTotalValidator } from './match-total.validator';

describe('matchTotalValidator', () => {
  let form: FormGroup;

  const totalCtrlName = 'totalAmount';

  function createForm(
    totalAmountValue: any,
    revisedValue: any,
    currentValue: any
  ) {
    return new FormGroup(
      {
        [totalCtrlName]: new FormControl(totalAmountValue),
        revisedForecast: new FormControl(revisedValue),
        currentForecast: new FormControl(currentValue)
      },
      {
        validators: matchTotalValidator(totalCtrlName)
      }
    );
  }

  it('should return null when revisedForecast matches totalAmount', () => {
    form = createForm(100, 100, null);
    expect(form.errors).toBeNull();
  });

  it('should return null when currentForecast matches totalAmount and revisedForecast is null', () => {
    form = createForm(200, null, 200);
    expect(form.errors).toBeNull();
  });

  it('should return error when revisedForecast does not match totalAmount', () => {
    form = createForm(150, 100, null);
    expect(form.errors).toEqual({ totalMismatch: true });
  });

  it('should return error when currentForecast does not match totalAmount', () => {
    form = createForm(150, null, 100);
    expect(form.errors).toEqual({ totalMismatch: true });
  });

  it('should return null if both revisedForecast and currentForecast are null', () => {
    form = createForm(100, null, null);
    expect(form.errors).toBeNull();
  });

  it('should return null if revisedForecast is empty string', () => {
    form = createForm(100, '', null);
    expect(form.errors).toBeNull();
  });

  it('should return null if currentForecast is empty string', () => {
    form = createForm(100, null, '');
    expect(form.errors).toBeNull();
  });

  it('should return null if entered value is NaN', () => {
    form = createForm(100, 'abc', null);
    expect(form.errors).toBeNull();
  });

  it('should handle missing controls gracefully', () => {
    form = new FormGroup(
      {
        [totalCtrlName]: new FormControl(100)
        // revisedForecast and currentForecast missing
      },
      {
        validators: matchTotalValidator(totalCtrlName)
      }
    );

    spyOn(console, 'warn');
    form.updateValueAndValidity();
    expect(console.warn).toHaveBeenCalled();
    expect(form.errors).toBeNull();
  });
});
