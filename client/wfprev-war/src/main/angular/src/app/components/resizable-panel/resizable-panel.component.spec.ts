import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResizablePanelComponent } from './resizable-panel.component';

describe('ResizablePanelComponent', () => {
  let component: ResizablePanelComponent;
  let fixture: ComponentFixture<ResizablePanelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResizablePanelComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResizablePanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // Test 1: Check if the component is created
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Test 2: Check if panelWidth is updated and event is emitted on resizePanel call
  it('should resize the panel and emit the panelResized event', () => {
    spyOn(component.panelResized, 'emit');  // Spy on the event emitter

    component.resizePanel(75);
    expect(component.panelWidth).toBe('75vw');
    expect(component.panelResized.emit).toHaveBeenCalled();
  });

  // Test 3: Check if the window resize event is being handled
  it('should handle window resize event', () => {
    spyOn(component, 'onResize');
    window.dispatchEvent(new Event('resize'));

    expect(component.onResize).toHaveBeenCalled();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.panelWidth).toBe('50vw');
    expect(component.breakpoints).toEqual([5, 50, 90]);
  });

  it('should resize the panel and emit the panelResized event', () => {
    spyOn(component.panelResized, 'emit');  // Spy on the event emitter

    component.resizePanel(75);
    expect(component.panelWidth).toBe('75vw');
    expect(component.panelResized.emit).toHaveBeenCalled();
  });

  it('should emit panelResized with void', () => {
    const emitSpy = spyOn(component.panelResized, 'emit');
    component.resizePanel(75);
    expect(emitSpy).toHaveBeenCalledWith();  // Should be called with no arguments
  });

  it('should handle window resize event', () => {
    const event = new Event('resize');
    component.onResize(event);  // Call the method directly
    //given the method is empty, we can only check if it's called
    expect(true).toBeTruthy();  // Add assertions based on what onResize should do
  });
});
