import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TimestampComponent } from './timestamp.component';

describe('TimestampComponent', () => {
  let component: TimestampComponent;
  let fixture: ComponentFixture<TimestampComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimestampComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TimestampComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should render updateDate and updateUser when provided', () => {
    component.updateDate = '2025-08-07T12:34:56Z';
    component.updateUser = 'luli';
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('2025-08-07');
    expect(text).toContain('luli');
  });

  it('should handle missing inputs without throwing', () => {
    component.updateDate = undefined;
    component.updateUser = undefined;
    expect(() => fixture.detectChanges()).not.toThrow();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toBeDefined();
  });

  it('should update the view when inputs change after initial render', () => {
    component.updateDate = '2025-08-07T00:00:00Z';
    component.updateUser = 'first';
    fixture.detectChanges();

    component.updateDate = '2025-08-07T23:59:59Z';
    component.updateUser = 'second';
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('second');
    expect(text).toContain('2025-08-07');
  });
});
