import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppHeaderComponent } from './app-header.component';
import { By } from '@angular/platform-browser';

describe('AppHeaderComponent', () => {
  let component: AppHeaderComponent;
  let fixture: ComponentFixture<AppHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppHeaderComponent], // Import standalone component
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

  it('should render the title and environment text', () => {
    const titleElement = fixture.debugElement.query(By.css('.title')).nativeElement;
    const environmentElement = fixture.debugElement.query(By.css('.environment')).nativeElement;

    expect(titleElement.textContent).toContain(component.title);
    expect(environmentElement.textContent).toContain(component.environment);
  });

  it('should render the user name', () => {
    const userNameElement = fixture.debugElement.query(By.css('.user-name')).nativeElement;
    expect(userNameElement.textContent).toContain(component.currentUser);
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
    const logoutElement = fixture.debugElement.query(By.css('button[mat-menu-item]'));
    logoutElement.triggerEventHandler('click', null);

    expect(component.onLogoutClick).toHaveBeenCalled();
  });

  it('should render the Material menu', () => {
    const menuElement = fixture.debugElement.query(By.css('mat-menu'));
    expect(menuElement).toBeTruthy();
  });

  it('should render the account icon and dropdown arrow', () => {
    const accountIcon = fixture.debugElement.query(By.css('mat-icon:first-child')).nativeElement;
    const dropdownIcon = fixture.debugElement.query(By.css('mat-icon:last-child')).nativeElement;

    expect(accountIcon.textContent).toContain('account_circle');
    expect(dropdownIcon.textContent).toContain('keyboard_arrow_down');
  });
});
