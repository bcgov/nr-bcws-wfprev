import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditProjectComponent } from './edit-project.component';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ProjectDetailsComponent } from 'src/app/components/edit-project/project-details/project-details.component';

describe('EditProjectComponent', () => {
  let component: EditProjectComponent;
  let fixture: ComponentFixture<EditProjectComponent>;
  let mockActivatedRoute: Partial<ActivatedRoute>;

  beforeEach(async () => {
    const mockParamMap: ParamMap = {
      has: (key: string) => key === 'name',
      get: (key: string) => (key === 'name' ? 'Test Project' : null),
      getAll: () => [],
      keys: [],
    };

    mockActivatedRoute = {
      queryParamMap: of(mockParamMap),
    };

    await TestBed.configureTestingModule({
      imports: [EditProjectComponent, BrowserAnimationsModule],
      providers: [{ provide: ActivatedRoute, useValue: mockActivatedRoute }],
    }).compileComponents();

    fixture = TestBed.createComponent(EditProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set the projectName from query parameters', () => {
    expect(component.projectName).toBe('Test Project');
  });

  it('should display the project name in the title', () => {
    const projectTitleElement = fixture.debugElement.query(By.css('.project-title span')).nativeElement;
    expect(projectTitleElement.textContent).toContain('Test Project');
  });

  it('should render the Details tab', () => {
    const detailsTab = fixture.debugElement.query(By.css('.details-tab'));
    expect(detailsTab).toBeTruthy();
  });

  it('should display ProjectDetailsComponent inside the Details tab', () => {
    const projectDetailsComponent = fixture.debugElement.query(By.directive(ProjectDetailsComponent));
    expect(projectDetailsComponent).toBeTruthy();
  });
});
