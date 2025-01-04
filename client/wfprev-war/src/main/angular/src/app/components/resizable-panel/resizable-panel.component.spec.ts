import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResizablePanelComponent } from './resizable-panel.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { Component } from '@angular/core';

// Mock ProjectsListComponent
@Component({
  selector: 'app-projects-list',
  template: '<div>Mock Projects List</div>',
})
class MockProjectsListComponent {}

describe('ResizablePanelComponent', () => {
  let component: ResizablePanelComponent;
  let fixture: ComponentFixture<ResizablePanelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrowserAnimationsModule], // Standalone component dependencies
      declarations: [ResizablePanelComponent, MockProjectsListComponent], // Include mock component
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of({ get: () => null }), // Mock ActivatedRoute
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResizablePanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.panelWidth).toBe('50vw');
    expect(component.breakpoints).toEqual([5, 50, 90]);
    expect(component.selectedTabIndex).toBe(0); // Default tab index
  });

  it('should resize the panel and emit the panelResized event', () => {
    spyOn(component.panelResized, 'emit'); // Spy on the event emitter
    component.resizePanel(75);
    expect(component.panelWidth).toBe('75vw');
    expect(component.panelResized.emit).toHaveBeenCalled();
  });

  it('should emit panelResized with void', () => {
    const emitSpy = spyOn(component.panelResized, 'emit');
    component.resizePanel(75);
    expect(emitSpy).toHaveBeenCalledWith(); // Should be called with no arguments
  });

  it('should update selectedTabIndex when selectTab is called', () => {
    component.selectTab(2); // Switch to the third tab
    expect(component.selectedTabIndex).toBe(2);
  });

  it('should render the correct number of tabs', () => {
    const tabs = component.tabs;
    expect(tabs.length).toBe(3);
    expect(tabs[0].name).toBe('Projects');
    expect(tabs[1].name).toBe('Dashboard');
    expect(tabs[2].name).toBe('Planning');
  });

  it('should render default panel width', () => {
    expect(component.panelWidth).toBe('50vw');
  });
});
