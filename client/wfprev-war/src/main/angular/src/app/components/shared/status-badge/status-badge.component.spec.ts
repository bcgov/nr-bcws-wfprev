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

  it('should render the icon when provided', () => {
    component.label = 'With Icon';
    component.icon = {
      src: 'https://example.com/icon.svg',
      alt: 'Example Icon',
      title: 'Example Title'
    };
    fixture.detectChanges();

    const imgElement = fixture.debugElement.query(By.css('.badge-icon img'));
    expect(imgElement).toBeTruthy();
    expect(imgElement.attributes['src']).toBe('https://example.com/icon.svg');
    expect(imgElement.attributes['alt']).toBe('Example Icon');
    expect(imgElement.attributes['title']).toBe('Example Title');
  });

  it('should not render the icon when icon is null', () => {
    component.label = 'No Icon';
    component.icon = null;
    fixture.detectChanges();

    const iconElement = fixture.debugElement.query(By.css('.badge-icon'));
    expect(iconElement).toBeNull();
  });
});
