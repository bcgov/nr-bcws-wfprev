import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DropdownButtonComponent } from './dropdown-button.component';
import { By } from '@angular/platform-browser';

describe('DropdownButtonComponent', () => {
  let component: DropdownButtonComponent;
  let fixture: ComponentFixture<DropdownButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DropdownButtonComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(DropdownButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit action with correct payload', () => {
    spyOn(component.actionSelected, 'emit');

    component.index = 2;
    component.emitAction('approve');

    expect(component.actionSelected.emit).toHaveBeenCalledWith({ action: 'approve', index: 2 });
  });

  it('should return true for isButtonDisabled when status is COMPLETE', () => {
    component.status = component.FiscalStatuses.COMPLETE;
    expect(component.isButtonDisabled()).toBeTrue();
  });

  it('should return true for isButtonDisabled when status is CANCELLED', () => {
    component.status = component.FiscalStatuses.CANCELLED;
    expect(component.isButtonDisabled()).toBeTrue();
  });

  it('should return false for isButtonDisabled when status is DRAFT', () => {
    component.status = component.FiscalStatuses.DRAFT;
    expect(component.isButtonDisabled()).toBeFalse();
  });

  it('should return true for isButtonDisabled when isDisabled is true, even if status is DRAFT', () => {
    component.status = component.FiscalStatuses.DRAFT;
    component.isDisabled = true;
    expect(component.isButtonDisabled()).toBeTrue();
  });

});
