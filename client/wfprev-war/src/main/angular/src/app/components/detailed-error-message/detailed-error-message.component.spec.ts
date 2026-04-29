import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_SNACK_BAR_DATA, MatSnackBarRef } from '@angular/material/snack-bar';
import { DetailedErrorMessageComponent } from './detailed-error-message.component';

describe('DetailedErrorMessageComponent', () => {
  let component: DetailedErrorMessageComponent;
  let fixture: ComponentFixture<DetailedErrorMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetailedErrorMessageComponent],
      providers: [
        { provide: MAT_SNACK_BAR_DATA, useValue: {} },
        { provide: MatSnackBarRef, useValue: { dismiss: jasmine.createSpy('dismiss') } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetailedErrorMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
