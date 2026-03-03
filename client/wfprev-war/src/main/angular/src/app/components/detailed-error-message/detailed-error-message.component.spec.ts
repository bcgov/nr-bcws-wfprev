import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailedErrorMessageComponent } from './detailed-error-message.component';

describe('DetailedErrorMessageComponent', () => {
  let component: DetailedErrorMessageComponent;
  let fixture: ComponentFixture<DetailedErrorMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetailedErrorMessageComponent]
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
