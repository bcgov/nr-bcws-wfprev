import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DetailsContainerComponent } from './details-container.component';
import { By } from '@angular/platform-browser';

describe('DetailsContainerComponent', () => {
  let component: DetailsContainerComponent;
  let fixture: ComponentFixture<DetailsContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetailsContainerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DetailsContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render custom button texts', () => {
    component.cancelText = 'Nope';
    component.saveText = 'Yes!';
    fixture.detectChanges();

    const buttons = fixture.debugElement.queryAll(By.css('button'));
    expect(buttons[0].nativeElement.textContent).toContain('Nope');
    expect(buttons[1].nativeElement.textContent).toContain('Yes!');
  });

  it('should disable buttons based on inputs', () => {
    component.cancelDisabled = true;
    component.saveDisabled = true;
    fixture.detectChanges();

    const buttons = fixture.debugElement.queryAll(By.css('button'));
    expect(buttons[0].nativeElement.disabled).toBeTrue();
    expect(buttons[1].nativeElement.disabled).toBeTrue();
  });

  it('should emit cancel when cancel button clicked', () => {
    spyOn(component.cancel, 'emit');
    const button = fixture.debugElement.queryAll(By.css('button'))[0];
    button.nativeElement.click();
    expect(component.cancel.emit).toHaveBeenCalled();
  });

  it('should emit save when save button clicked', () => {
    spyOn(component.save, 'emit');
    const button = fixture.debugElement.queryAll(By.css('button'))[1];
    button.nativeElement.click();
    expect(component.save.emit).toHaveBeenCalled();
  });
});
