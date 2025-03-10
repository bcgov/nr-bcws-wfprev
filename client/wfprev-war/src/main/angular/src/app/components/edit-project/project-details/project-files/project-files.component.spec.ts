import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectFilesComponent } from './project-files.component';

describe('ProjectFilesComponent', () => {
  let component: ProjectFilesComponent;
  let fixture: ComponentFixture<ProjectFilesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectFilesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProjectFilesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
