import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ReadOnlyFieldComponent } from './read-only-field.component';

describe('ReadOnlyFieldComponent', () => {
  let component: ReadOnlyFieldComponent;
  let fixture: ComponentFixture<ReadOnlyFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReadOnlyFieldComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ReadOnlyFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render label and value', () => {
    component.label = 'Test Label';
    component.value = 'Test Value';

    fixture.detectChanges();

    const labelEl = fixture.debugElement.query(By.css('.label'));
    const valueEl = fixture.debugElement.query(By.css('.value'));

    expect(labelEl.nativeElement.textContent).toContain('Test Label');
    expect(valueEl.nativeElement.textContent).toContain('Test Value');
  });

  it('should update value dynamically', () => {
    component.label = 'Dynamic Label';
    component.value = 'Initial Value';
    fixture.detectChanges();

    component.value = 'Updated Value';
    fixture.detectChanges();

    const valueEl = fixture.debugElement.query(By.css('.value'));
    expect(valueEl.nativeElement.textContent).toContain('Updated Value');
  });
});
