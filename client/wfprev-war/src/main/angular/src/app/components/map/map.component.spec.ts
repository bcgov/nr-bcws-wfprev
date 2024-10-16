import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapComponent } from './map.component';
import { By } from '@angular/platform-browser';

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MapComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should display the title in an <h2> tag', () => {
    const h2Element = fixture.debugElement.query(By.css('h2')).nativeElement;
    expect(h2Element.textContent).toContain('This is the Map Component');
  });

  it('should render the list content', () => {
    const paragraphElement = fixture.debugElement.query(By.css('p')).nativeElement;
    expect(paragraphElement.textContent).toContain('Map content goes here');
  });
});
