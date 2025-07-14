import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReadOnlyFieldComponent } from './read-only-field.component';

describe('ReadOnlyFieldComponent', () => {
  let component: ReadOnlyFieldComponent;
  let fixture: ComponentFixture<ReadOnlyFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReadOnlyFieldComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReadOnlyFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
