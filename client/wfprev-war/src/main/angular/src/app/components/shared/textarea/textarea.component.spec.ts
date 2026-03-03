import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, Validators } from '@angular/forms';
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
    component.maxLength = 5;
    component.control = new FormControl('too long text', [Validators.maxLength(5)]);
    component.control.markAsTouched();
    fixture.detectChanges();

    const errors = fixture.debugElement.queryAll(By.css('.error'));
    const maxLengthError = errors.find(el => el.nativeElement.textContent.includes('Maximum character'));
    expect(maxLengthError).toBeTruthy();
    if (maxLengthError) {
      expect(maxLengthError.nativeElement.classList.contains('invisible')).toBeFalse();
    }
  });

  it('should not show error when control is untouched', () => {
    component.control = new FormControl('too long text', [Validators.maxLength(5)]);
    fixture.detectChanges();

    const errorEl = fixture.debugElement.query(By.css('.error'));
    expect(errorEl).toBeTruthy();
    expect(errorEl.nativeElement.classList.contains('invisible')).toBeTrue();
  });

  it('onInput should truncate value to maxLength and not emit valueChanges', () => {
    component.maxLength = 5;
    const longValue = '123456789';

    const valueChangesSpy = jasmine.createSpy('valueChanges');
    const sub = component.control.valueChanges.subscribe(valueChangesSpy);

    const setValueSpy = spyOn(component.control, 'setValue').and.callThrough();

    const textarea = document.createElement('textarea');
    textarea.value = longValue;
    const inputEvent = new Event('input');
    Object.defineProperty(inputEvent, 'target', { value: textarea });

    component.onInput(inputEvent);

    expect(setValueSpy).toHaveBeenCalledOnceWith('12345', { emitEvent: false });
    expect(component.control.value).toBe('12345');
    expect(valueChangesSpy).not.toHaveBeenCalled();

    sub.unsubscribe();
  });

  it('onInput should do nothing when value is within limit', () => {
    component.maxLength = 10;
    component.control.setValue('short');

    const setValueSpy = spyOn(component.control, 'setValue').and.callThrough();

    const textarea = document.createElement('textarea');
    textarea.value = 'short';
    const inputEvent = new Event('input');
    Object.defineProperty(inputEvent, 'target', { value: textarea });

    component.onInput(inputEvent);

    expect(setValueSpy).not.toHaveBeenCalled();
    expect(component.control.value).toBe('short');
  });

  it('onPaste should replace selected text with clipboard text, respecting maxLength', () => {
    component.maxLength = 8;
    component.control.setValue('abc');

    const textarea = document.createElement('textarea');
    textarea.value = 'abc';
    textarea.selectionStart = 3;
    textarea.selectionEnd = 3;

    const preventDefault = jasmine.createSpy('preventDefault');
    const clipboardData = { getData: (_: string) => 'LONGPASTE' };

    const pasteEvent = {
      preventDefault,
      clipboardData,
      target: textarea,
    } as unknown as ClipboardEvent;

    const setValueSpy = spyOn(component.control, 'setValue').and.callThrough();

    component.onPaste(pasteEvent);

    expect(preventDefault).toHaveBeenCalled();
    expect(setValueSpy).toHaveBeenCalledOnceWith('abcLONGP');
    expect(component.control.value).toBe('abcLONGP');
  });
});
