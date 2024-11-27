import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { Location } from '@angular/common';

describe('AppComponent', () => {
  let router: Router;
  let location: Location;
  let fixture: ComponentFixture<AppComponent>;
  let app: AppComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([])], // Setup RouterTestingModule properly
      declarations: [AppComponent],
    }).compileComponents();

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    fixture = TestBed.createComponent(AppComponent);
    app = fixture.componentInstance;
    router.initialNavigation();
  });

  it('should create the app', () => {
    expect(app).toBeTruthy();
  });

  it('should navigate to "list" route and set active route', async () => {
    spyOn(router, 'navigate');
    app.setActive('list');
    expect(app.activeRoute).toBe('list');
    expect(router.navigate).toHaveBeenCalledWith(['list']);
  });

  it('should navigate to "map" route and set active route', async () => {
    spyOn(router, 'navigate');
    app.setActive('map');
    expect(app.activeRoute).toBe('map');
    expect(router.navigate).toHaveBeenCalledWith(['map']);
  });

  it('should navigate to home when title is clicked', async () => {
    spyOn(router, 'navigate');
    const titleElement = fixture.debugElement.query(By.css('.title'));
    titleElement.triggerEventHandler('click', null);
    expect(router.navigate).toHaveBeenCalledWith(['']);
  });

  it('should add "active" class to the list menu item when activeRoute is "list"', () => {
    app.activeRoute = 'list';
    fixture.detectChanges();
    const listMenuItem = fixture.debugElement.query(By.css('span:nth-child(1)'));
    expect(listMenuItem.nativeElement.classList).toContain('active');
  });

  it('should add "active" class to the map menu item when activeRoute is "map"', () => {
    app.activeRoute = 'map';
    fixture.detectChanges();

    const mapMenuItem = fixture.debugElement.query(
      By.css('.span-class[class="span-class active"]')  // Or more simply: '.span-class.active'
    );

    expect(mapMenuItem).not.toBeNull();
    expect(mapMenuItem.nativeElement.classList).toContain('active');
  });
});
