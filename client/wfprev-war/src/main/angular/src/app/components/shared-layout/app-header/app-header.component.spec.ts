import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppHeaderComponent } from './app-header.component';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('AppHeaderComponent', () => {
  let component: AppHeaderComponent;
  let fixture: ComponentFixture<AppHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AppHeaderComponent, // Import the standalone component
        BrowserAnimationsModule, // Add this to enable Angular Material animations
      ],
      providers: [
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } }, // Mock Router
      ],
    }).compileComponents();
  
    fixture = TestBed.createComponent(AppHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
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

  it('should call onSupportLinkClick() when Training and Support is clicked', () => {
    spyOn(component, 'onSupportLinkClick');
    const supportElement = fixture.debugElement.query(By.css('.training'));
    supportElement.triggerEventHandler('click', null);

    expect(component.onSupportLinkClick).toHaveBeenCalled();
  });

  it('should call onLogoutClick() when Logout is clicked', () => {
    spyOn(component, 'onLogoutClick');
  
    // Simulate clicking the menu trigger button
    const menuTrigger = fixture.debugElement.query(By.css('button[mat-button]'));
    menuTrigger.triggerEventHandler('click', null);
    fixture.detectChanges(); // Let the menu render
  
    // Find the logout button inside the Material menu
    const logoutButton = fixture.debugElement.query(By.css('button[mat-menu-item]'));
    expect(logoutButton).toBeTruthy(); // Ensure the button exists
    logoutButton.triggerEventHandler('click', null); // Simulate logout button click
  
    expect(component.onLogoutClick).toHaveBeenCalled();
  });

  it('should render the username', () => {
    const userNameElement = fixture.debugElement.query(By.css('.user-name')).nativeElement;
    expect(userNameElement.textContent).toContain(component.currentUser);
  });

  it('should render the Material menu', () => {
    const menuElement = fixture.debugElement.query(By.css('mat-menu'));
    expect(menuElement).toBeTruthy();
  });
});
