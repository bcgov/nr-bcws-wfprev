import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CUSTOM_DATE_FORMATS } from 'src/app/utils/constants';
import { DatePickerComponent } from './date-picker.component';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, DateAdapter } from '@angular/material/core';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import moment from 'moment';

describe('DatePickerComponent', () => {
  let component: DatePickerComponent;
  let fixture: ComponentFixture<DatePickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatDatepickerModule,
        NoopAnimationsModule,
        DatePickerComponent
      ],
      providers: [
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
        { provide: MAT_DATE_LOCALE, useValue: 'en-CA' },
        { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DatePickerComponent);
    component = fixture.componentInstance;
    component.control = new FormControl();
    component.label = 'Test Date';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render label correctly', () => {
    const labelElement = fixture.debugElement.query(By.css('label'));
    expect(labelElement).toBeTruthy();
    expect(labelElement.nativeElement.textContent).toContain('Test Date');
  });


  it('should bind the form control and display date', () => {
    const testDate = moment('2024-01-01');
    component.control.setValue(testDate);
    fixture.detectChanges();

    const inputElement = fixture.debugElement.query(By.css('input'));
    expect(inputElement.nativeElement.value).toContain('2024');
  });

  it('should update form control when input changes', () => {
    const inputElement = fixture.debugElement.query(By.css('input')).nativeElement;
    
    inputElement.value = '2025-05-05';
    inputElement.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(component.control.value).toBeTruthy();
  });

  it('should apply custom date formats', () => {
    const dateAdapter = TestBed.inject(DateAdapter);
    const formatted = dateAdapter.format(moment('2024-06-01'), CUSTOM_DATE_FORMATS.display.dateInput);
    expect(formatted).toBeTruthy();
  });
});
