import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { EndorsementApprovalSectionComponent, EndorsementApprovalSectionConfig } from './endorsement-approval-section.component';

const mockConfig: EndorsementApprovalSectionConfig = {
  checkboxLabel: 'Endorse Fiscal Activity',
  datepickerLabel: 'Endorsement Date',
  readOnlyLabel: 'Endorsed By',
  readOnlyValue: 'Smith, John',
  textareaLabel: "Endorser's Comments",
  textareaPlaceholder: 'Add supporting details...',
};

const mockForm = new FormGroup({
  endorseFiscalActivity: new FormControl<boolean | null>(false),
  endorsementDate: new FormControl<Date | null>(null),
  endorsementComment: new FormControl<string | null>(''),
});

describe('EndorsementApprovalSectionComponent', () => {
  let component: EndorsementApprovalSectionComponent;
  let fixture: ComponentFixture<EndorsementApprovalSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EndorsementApprovalSectionComponent, ReactiveFormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(EndorsementApprovalSectionComponent);
    component = fixture.componentInstance;
    component.config = mockConfig;
    component.form = mockForm;
    component.checkboxKey = 'endorseFiscalActivity';
    component.dateKey = 'endorsementDate';
    component.commentKey = 'endorsementComment';
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return checkboxControl from form', () => {
    expect(component.checkboxControl).toBe(mockForm.get('endorseFiscalActivity') as FormControl);
  });

  it('should return dateControl from form', () => {
    expect(component.dateControl).toBe(mockForm.get('endorsementDate') as FormControl);
  });

  it('should return commentControl from form', () => {
    expect(component.commentControl).toBe(mockForm.get('endorsementComment') as FormControl);
  });

  it('should reflect config checkboxLabel in template', () => {
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Endorse Fiscal Activity');
  });

  it('should reflect config readOnlyValue in template', () => {
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Smith, John');
  });

  it('should reflect checkbox form control value', () => {
    mockForm.get('endorseFiscalActivity')?.setValue(true);
    expect(component.checkboxControl.value).toBe(true);
  });

  it('should reflect disabled state of dateControl', () => {
    mockForm.get('endorsementDate')?.disable();
    expect(component.dateControl.disabled).toBe(true);
  });

  it('should reflect enabled state of dateControl', () => {
    mockForm.get('endorsementDate')?.enable();
    expect(component.dateControl.disabled).toBe(false);
  });
});