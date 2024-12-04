import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppHeaderComponent } from './app-header.component';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ResourcesRoutes } from 'src/app/utils';

describe('AppHeaderComponent', () => {
  let component: AppHeaderComponent;
  let fixture: ComponentFixture<AppHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AppHeaderComponent,
        BrowserAnimationsModule,
      ],
      providers: [
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AppHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should render the logo', () => {
    const logoElement = fixture.debugElement.query(By.css('.bcwfservice-logo'));
    expect(logoElement).toBeTruthy();
  });

  it('should call onBCLogoClick() when the logo is clicked', () => {
    spyOn(component, 'onBCLogoClick');
    const logoElement = fixture.debugElement.query(By.css('.bc-logo'));
    logoElement.triggerEventHandler('click', null);
    expect(component.onBCLogoClick).toHaveBeenCalled();
  });

  it('should navigate to the home page(map page by default) when onBCLogoClick() is called', () => {
    const router = TestBed.inject(Router);
    component.onBCLogoClick();
    expect(router.navigate).toHaveBeenCalledWith([ResourcesRoutes.LANDING]);
  });

  it('should call onSupportLinkClick() when Training and Support is clicked', () => {
    spyOn(component, 'onSupportLinkClick');
    const supportElement = fixture.debugElement.query(By.css('.training'));
    supportElement.triggerEventHandler('click', null);
    expect(component.onSupportLinkClick).toHaveBeenCalled();
  });

  it('should open the correct URL in a new tab with noopener when onSupportLinkClick() is called', () => {
    spyOn(window, 'open');
    component.onSupportLinkClick();
    expect(window.open).toHaveBeenCalledWith(
      'https://intranet.gov.bc.ca/bcws/provincial-programs/strategic-initiatives-and-innovation/wildfire-one/wildfire-one-training',
      '_blank',
      'noopener'
    );
  });

  it('should render the username', () => {
    const userNameElement = fixture.debugElement.query(By.css('.user-name')).nativeElement;
    expect(userNameElement.textContent).toContain(component.currentUser);
  });

  it('should display the correct environment', () => {
    const environmentElement = fixture.debugElement.query(By.css('.environment')).nativeElement;
    expect(environmentElement.textContent).toBe(component.environment);
  });

  it('should display the correct title', () => {
    const titleElement = fixture.debugElement.query(By.css('.title')).nativeElement;
    expect(titleElement.textContent).toBe(component.title);
  });

  it('should call onLogoutClick() when Logout is clicked', () => {
    spyOn(component, 'onLogoutClick');
    const menuTrigger = fixture.debugElement.query(By.css('button[mat-button]'));
    menuTrigger.triggerEventHandler('click', null);
    fixture.detectChanges();

    const logoutButton = fixture.debugElement.query(By.css('button[mat-menu-item]'));
    expect(logoutButton).toBeTruthy();
    logoutButton.triggerEventHandler('click', null);
    expect(component.onLogoutClick).toHaveBeenCalled();
  });

  it('should render the Material menu', () => {
    const menuElement = fixture.debugElement.query(By.css('mat-menu'));
    expect(menuElement).toBeTruthy();
  });
});
