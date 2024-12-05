import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MapComponent } from './map.component';
import { By } from '@angular/platform-browser';
import * as L from 'leaflet';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;
  let mapMock: Partial<L.Map>;

  beforeEach(async () => {
    mapMock = {
      fitBounds: jasmine.createSpy('fitBounds'),
      invalidateSize: jasmine.createSpy('invalidateSize'),
      addLayer: jasmine.createSpy('addLayer'),
    };

    spyOn(L, 'map').and.returnValue(mapMock as L.Map);

    await TestBed.configureTestingModule({
      imports: [MapComponent, BrowserAnimationsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // Test 1: Check if the component is created
  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  // Test 2: Check if the title is displayed in an <h2> tag
  it('should display the title in an <h2> tag', () => {
    const h2Element = fixture.debugElement.query(By.css('h2')).nativeElement;
    expect(h2Element.textContent).toContain('This is the Map Component');
  });

  // Test 3: Check if the map content text is displayed
  it('should render the map content text', () => {
    const paragraphElement = fixture.debugElement.query(By.css('p')).nativeElement;
    expect(paragraphElement.textContent).toContain('Map content goes here');
  });

  // Test 4: Check if the Leaflet map is initialized
  it('should initialize the Leaflet map', () => {
    expect(L.map).toHaveBeenCalledWith('map');
    expect(mapMock.fitBounds).toHaveBeenCalled();
  });

  // Test 5: Check if invalidateSize() is called when the panel is resized
  it('should call invalidateSize() when the panel is resized', () => {
    component.onPanelResized();
    expect(mapMock.invalidateSize).toHaveBeenCalled();
  });
});
