import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EndorsementApprovalComponent } from 'src/app/components/edit-project/endorsement-approval/endorsement-approval.component';


describe('EndorsementComponent', () => {
  let component: EndorsementApprovalComponent;
  let fixture: ComponentFixture<EndorsementApprovalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EndorsementApprovalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EndorsementApprovalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
