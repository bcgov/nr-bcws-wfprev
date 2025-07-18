import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MatSlideToggle } from '@angular/material/slide-toggle';

import { SlideToggleComponent } from './slide-toggle.component';

describe('SlideToggleComponent', () => {
  let component: SlideToggleComponent;
  let fixture: ComponentFixture<SlideToggleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SlideToggleComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(SlideToggleComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the label', () => {
    component.label = 'Test Label';
    fixture.detectChanges();

    const labelEl = fixture.nativeElement.querySelector('label');
    expect(labelEl.textContent).toContain('Test Label');
  });

  it('should reflect the checked input value', () => {
    component.checked = true;
    fixture.detectChanges();

    const toggle = fixture.debugElement.query(By.directive(MatSlideToggle)).componentInstance;
    expect(toggle.checked).toBeTrue();
  });

  it('should emit checkedChange when toggled', () => {
    spyOn(component.checkedChange, 'emit');

    fixture.detectChanges();

    const toggleDebug = fixture.debugElement.query(By.directive(MatSlideToggle));
    const toggle = toggleDebug.componentInstance as MatSlideToggle;

    toggle.checked = true;
    toggle.change.emit({ source: toggle, checked: true });

    expect(component.checkedChange.emit).toHaveBeenCalledWith(true);
  });

});
