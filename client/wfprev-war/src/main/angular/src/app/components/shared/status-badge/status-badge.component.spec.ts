import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { StatusBadgeComponent } from './status-badge.component';

describe('StatusBadgeComponent', () => {
  let component: StatusBadgeComponent;
  let fixture: ComponentFixture<StatusBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatusBadgeComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(StatusBadgeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the label', () => {
    component.label = 'My Status';
    component.icon = null;
    fixture.detectChanges();

    const labelElement = fixture.debugElement.query(By.css('.badge-label'));
    expect(labelElement.nativeElement.textContent).toContain('My Status');
  });

  it('should display the correct icon and label for "on-track" type', () => {
    component.type = 'on-track';
    fixture.detectChanges();

    const img = fixture.debugElement.query(By.css('img')).nativeElement;
    expect(img.src).toContain('/assets/progress-status-ontrack.svg');
    
    const label = fixture.debugElement.query(By.css('.badge-label')).nativeElement;
    expect(label.textContent).toContain('On track');
  });

  it('should display the correct icon and label for "delayed" type', () => {
    component.type = 'delayed';
    fixture.detectChanges();

    const img = fixture.debugElement.query(By.css('img')).nativeElement;
    expect(img.src).toContain('/assets/progress-status-delayed.svg');
    
    const label = fixture.debugElement.query(By.css('.badge-label')).nativeElement;
    expect(label.textContent).toContain('Delayed');
  });

  it('should use custom label if provided', () => {
    component.type = 'on-track';
    component.label = 'Custom On Track';
    fixture.detectChanges();

    const label = fixture.debugElement.query(By.css('.badge-label')).nativeElement;
    expect(label.textContent).toContain('Custom On Track');
  });

  it('should handle invalid type gracefully', () => {
    component.type = 'invalid-type';
    fixture.detectChanges();

    const img = fixture.debugElement.query(By.css('img'));
    expect(img).toBeNull();
    
    const label = fixture.debugElement.query(By.css('.badge-label')).nativeElement;
    expect(label.textContent).toBe('');
  });
});
