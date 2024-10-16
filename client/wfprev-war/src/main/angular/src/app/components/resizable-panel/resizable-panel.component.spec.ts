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
});
