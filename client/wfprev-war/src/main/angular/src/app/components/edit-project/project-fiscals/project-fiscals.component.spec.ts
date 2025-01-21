import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectFiscalsComponent } from './project-fiscals.component';

describe('ProjectFiscalsComponent', () => {
  let component: ProjectFiscalsComponent;
  let fixture: ComponentFixture<ProjectFiscalsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectFiscalsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProjectFiscalsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
