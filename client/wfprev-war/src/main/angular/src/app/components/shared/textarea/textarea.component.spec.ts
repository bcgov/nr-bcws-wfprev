import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormControl, Validators } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { TextareaComponent } from './textarea.component';

describe('TextareaComponent', () => {
  let component: TextareaComponent;
  let fixture: ComponentFixture<TextareaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TextareaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TextareaComponent);
    component = fixture.componentInstance;
    component.control = new FormControl();
    component.label = 'Test Label';
    component.placeholder = 'Enter text';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render label', () => {
    const labelEl = fixture.debugElement.query(By.css('label'));
    expect(labelEl.nativeElement.textContent).toContain('Test Label');
  });

  it('should set textarea placeholder', () => {
    const textarea = fixture.debugElement.query(By.css('textarea'));
    expect(textarea.attributes['placeholder']).toBe('Enter text');
  });

  it('should set textarea rows', () => {
    component.rows = 6;
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('textarea'));
    expect(textarea.attributes['rows']).toBe('6');
  });

  it('should bind form control value', () => {
    component.control.setValue('Hello world');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('textarea')).nativeElement;
    expect(textarea.value).toBe('Hello world');
  });

  it('should show error when maxlength exceeded and touched', () => {
    component.control = new FormControl('too long text', [Validators.maxLength(5)]);
    component.control.markAsTouched();
    fixture.detectChanges();

    const errorEl = fixture.debugElement.query(By.css('.error'));
    expect(errorEl).toBeTruthy();
    expect(errorEl.nativeElement.textContent).toContain('Max length exceeded.');
  });

  it('should not show error when control is untouched', () => {
    component.control = new FormControl('too long text', [Validators.maxLength(5)]);
    fixture.detectChanges();

    const errorEl = fixture.debugElement.query(By.css('.error'));
    expect(errorEl).toBeNull();
  });
});
