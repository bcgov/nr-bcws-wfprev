import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectsListComponent } from './projects-list.component';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute } from '@angular/router';

describe('ProjectsListComponent', () => {
  let component: ProjectsListComponent;
  let fixture: ComponentFixture<ProjectsListComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectsListComponent, BrowserAnimationsModule, MatExpansionModule, MatSlideToggleModule], // Using standalone component
      providers: [
        { provide: ActivatedRoute, useValue: ActivatedRoute }, // Provide mock for ActivatedRoute
      ],
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

  it('should initialize with default values', () => {
    expect(component.selectedSort).toBe('');
    expect(component.syncWithMap).toBeFalse();
    expect(component.resultCount).toBe(3);
    expect(component.projectList.length).toBe(3);
    expect(component.fiscalYearActivityTypes.length).toBe(3);
  });

  it('should update selectedSort when onSortChange is called', () => {
    const mockEvent = { target: { value: 'ascending' } };
    component.onSortChange(mockEvent);
    expect(component.selectedSort).toBe('ascending');
  });

  it('should update syncWithMap when onSyncMapToggleChange is called', () => {
    component.onSyncMapToggleChange({ checked: true });
    expect(component.syncWithMap).toBeTrue();
  
    component.onSyncMapToggleChange({ checked: false });
    expect(component.syncWithMap).toBeFalse();
  });

  it('should display the correct number of results', () => {
    const resultCountElement = debugElement.query(By.css('.result-count span')).nativeElement;
    expect(resultCountElement.textContent).toContain(`${component.resultCount} Results`);
  });
  
  it('should display project names correctly', () => {
    const projectNames = debugElement.queryAll(By.css('.project-name')).map(el => el.nativeElement.textContent.trim());
    const expectedNames = component.projectList.map(project => project.projectName);
    expect(projectNames).toEqual(expectedNames);
  });

  it('should display the correct project details when expanded', () => {
    const panelDebugElement = debugElement.query(By.css('mat-expansion-panel'));
    const panelInstance = panelDebugElement.componentInstance;
  
    panelInstance.open(); // Expand the panel
    fixture.detectChanges();
  
    const detailElements = debugElement.queryAll(By.css('.project-header-details .detail span'));
    const firstProject = component.projectList[0];
  
    expect(detailElements[0].nativeElement.textContent).toContain('Region');
    expect(detailElements[1].nativeElement.textContent).toContain(firstProject.forestRegionOrgUnitId.toString());
    expect(detailElements[3].nativeElement.textContent).toContain(firstProject.totalPlannedProjectSizeHa.toString());
  });

  it('should handle empty projectList gracefully', () => {
    component.projectList = [];
    fixture.detectChanges();
  
    const projectItems = debugElement.queryAll(By.css('.project-name'));
    expect(projectItems.length).toBe(0);
  });
  
  it('should handle empty fiscalYearActivityTypes gracefully', () => {
    component.fiscalYearActivityTypes = [];
    fixture.detectChanges();
  
    const activityTypes = debugElement.queryAll(By.css('.activity-type'));
    expect(activityTypes.length).toBe(0);
  });

  it('should display all sort options dynamically', () => {
    const sortOptions = debugElement.queryAll(By.css('select option'));
    expect(sortOptions.length).toBe(component.sortOptions.length + 1); // +1 for default option
    expect(sortOptions[1].nativeElement.textContent).toContain('Name (A-Z)');
    expect(sortOptions[2].nativeElement.textContent).toContain('Name (Z-A)');
  });
});
