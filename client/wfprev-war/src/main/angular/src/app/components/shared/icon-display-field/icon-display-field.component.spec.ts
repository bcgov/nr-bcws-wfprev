import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MatIconModule } from '@angular/material/icon';
import { IconDisplayFieldComponent } from './icon-display-field.component';

describe('IconDisplayFieldComponent', () => {
  let component: IconDisplayFieldComponent;
  let fixture: ComponentFixture<IconDisplayFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IconDisplayFieldComponent, MatIconModule]
    }).compileComponents();

    fixture = TestBed.createComponent(IconDisplayFieldComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display label and value', () => {
    component.label = 'Hectares';
    component.value = '100';
    fixture.detectChanges();

    const labelEl = fixture.debugElement.query(By.css('.field-label')).nativeElement;
    const valEl = fixture.debugElement.query(By.css('.field-val')).nativeElement;

    expect(labelEl.textContent).toContain('Hectares');
    expect(valEl.textContent).toContain('100');
  });

  it('should render MatIcon if matIcon input is provided', () => {
    component.matIcon = 'home';
    fixture.detectChanges();

    const iconEl = fixture.debugElement.query(By.css('mat-icon'));
    expect(iconEl).toBeTruthy();
    expect(iconEl.nativeElement.textContent.trim()).toBe('home');

    const imgEl = fixture.debugElement.query(By.css('img'));
    expect(imgEl).toBeNull();
  });

  it('should render image if imageIcon input is provided', () => {
    component.imageIcon = '/assets/custom-icon.svg';
    fixture.detectChanges();

    const imgEl = fixture.debugElement.query(By.css('img'));
    expect(imgEl).toBeTruthy();
    expect(imgEl.nativeElement.src).toContain('/assets/custom-icon.svg');

    const iconEl = fixture.debugElement.query(By.css('mat-icon'));
    expect(iconEl).toBeNull();
  });
});
