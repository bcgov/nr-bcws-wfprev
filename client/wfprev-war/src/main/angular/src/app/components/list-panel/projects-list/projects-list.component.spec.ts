import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectsListComponent } from './projects-list.component';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

describe('ProjectsListComponent', () => {
  let component: ProjectsListComponent;
  let fixture: ComponentFixture<ProjectsListComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectsListComponent, BrowserAnimationsModule, MatExpansionModule, MatSlideToggleModule], // Using standalone component
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectsListComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the correct number of projects', () => {
    const projectItems = debugElement.queryAll(By.css('.project-name'));
    expect(projectItems.length).toBe(component.projectList.length);
    expect(projectItems[0].nativeElement.textContent).toContain(
      component.projectList[0].projectName
    );
  });

  it('should display fiscalYearActivityTypes correctly', () => {
    const activityTypes = debugElement.queryAll(By.css('.activity-type'));
    const expectedCount = component.fiscalYearActivityTypes.length * component.projectList.length;
    expect(activityTypes.length).toBe(expectedCount);
  });

  it('should handle sort change correctly', () => {
    spyOn(component, 'onSortChange');
    const select = debugElement.query(By.css('select')).nativeElement;
    select.value = component.sortOptions[1].value; // Change to second option
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(component.onSortChange).toHaveBeenCalled();
    expect(select.value).toBe('descending');
  });

  it('should toggle syncWithMap when the toggle is clicked', () => {
    spyOn(component, 'onSyncMapToggleChange'); // Spy on the method
    const toggleDebugElement = debugElement.query(By.css('mat-slide-toggle'));
    const toggle = toggleDebugElement.componentInstance; // Access MatSlideToggle instance
  
    // Simulate the toggle event
    toggle.change.emit({ checked: true }); // Emit the change event
    fixture.detectChanges();
  
    expect(component.onSyncMapToggleChange).toHaveBeenCalled(); // Assert the method was called
  });

  it('should display the correct data in project details', () => {
    const firstProject = component.projectList[0];
    const detailElements = debugElement.queryAll(By.css('.detail span'));
    expect(detailElements[0].nativeElement.textContent).toContain('Region');
    expect(detailElements[1].nativeElement.textContent).toContain(
      firstProject.forestRegionOrgUnitId
    );
    expect(detailElements[3].nativeElement.textContent).toContain(
      firstProject.totalPlannedProjectSizeHa.toString()
    );
  });

  it('should expand and collapse the expansion panel', () => {
    const panelDebugElement = debugElement.query(By.css('mat-expansion-panel'));
    const panel = panelDebugElement.componentInstance as any;
  
    panel.open();
    fixture.detectChanges();
    expect(panel.expanded).toBeTrue();
  
    panel.close();
    fixture.detectChanges();
    expect(panel.expanded).toBeFalse();
  });
  
  

  it('should render all sort options correctly', () => {
    const options = debugElement.queryAll(By.css('select option'));
    expect(options.length).toBe(component.sortOptions.length + 1); // +1 for the default option
    expect(options[1].nativeElement.textContent).toContain('Name (A-Z)');
    expect(options[2].nativeElement.textContent).toContain('Name (Z-A)');
  });
});
