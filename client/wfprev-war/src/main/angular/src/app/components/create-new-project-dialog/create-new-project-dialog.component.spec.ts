import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateNewProjectDialogComponent } from './create-new-project-dialog.component';

describe('CreateNewProjectDialogComponent', () => {
  let component: CreateNewProjectDialogComponent;
  let fixture: ComponentFixture<CreateNewProjectDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateNewProjectDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateNewProjectDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
