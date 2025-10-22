import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CheckboxComponent } from './checkbox.component';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

describe('CheckboxComponent', () => {
  let component: CheckboxComponent;
  let fixture: ComponentFixture<CheckboxComponent>;
  let checkboxDebugEl: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CheckboxComponent, MatCheckboxModule],
    }).compileComponents();

    fixture = TestBed.createComponent(CheckboxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    checkboxDebugEl = fixture.debugElement.query(By.css('mat-checkbox'));
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have default value as false', () => {
    expect(component.value).toBeFalse();
  });

  it('should call writeValue and update internal value', () => {
    component.writeValue(true);
    expect(component.value).toBeTrue();

    component.writeValue(false);
    expect(component.value).toBeFalse();
  });

  it('should call registerOnChange and registerOnTouched', () => {
    const changeFn = jasmine.createSpy('onChange');
    const touchedFn = jasmine.createSpy('onTouched');

    component.registerOnChange(changeFn);
    component.registerOnTouched(touchedFn);

    expect((component as any).onChange).toBe(changeFn);
    expect((component as any).onTouched).toBe(touchedFn);
  });

  it('should update disabled state via setDisabledState', () => {
    expect(component.disabled).toBeFalse();

    component.setDisabledState(true);
    expect(component.disabled).toBeTrue();

    component.setDisabledState(false);
    expect(component.disabled).toBeFalse();
  });

  it('should call onChange and onTouched when checkbox changes', () => {
    const mockEvent = { checked: true };
    const changeSpy = jasmine.createSpy('onChange');
    const touchedSpy = jasmine.createSpy('onTouched');

    component.registerOnChange(changeSpy);
    component.registerOnTouched(touchedSpy);

    component.onCheckboxChange(mockEvent);

    expect(component.value).toBeTrue();
    expect(changeSpy).toHaveBeenCalledWith(true);
    expect(touchedSpy).toHaveBeenCalled();
  });

  it('should reflect disabled state in template', () => {
    component.setDisabledState(true);
    fixture.detectChanges();

    const checkbox = checkboxDebugEl.nativeElement.querySelector('input[type="checkbox"]');
    expect(checkbox.disabled).toBeTrue();
  });
});
