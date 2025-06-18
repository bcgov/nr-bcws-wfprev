import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppHeaderComponent } from './app-header.component';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ResourcesRoutes } from 'src/app/utils';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppConfigService } from 'src/app/services/app-config.service';
import { Subject } from 'rxjs';
import { TokenService } from 'src/app/services/token.service';

describe('AppHeaderComponent', () => {
  let component: AppHeaderComponent;
  let fixture: ComponentFixture<AppHeaderComponent>;
  let mockTokenService: any;
  let credentialsSubject: Subject<void>;

  const mockConfig = {
    application: {
      lazyAuthenticate: false,
      enableLocalStorageToken: true,
      localStorageTokenKey: 'test-oauth',
      allowLocalExpiredToken: false,
      baseUrl: 'http://test.com',
      environment: 'DEV'
    },
    webade: {
      oauth2Url: 'http://oauth.test',
      clientId: 'test-client',
      authScopes: ['GET_PROJECT'],
      enableCheckToken: false,
      checkTokenUrl: 'http://check-token.test'
    }
  };
  
  const mockAppConfigService = {
    getConfig: jasmine.createSpy('getConfig').and.returnValue(mockConfig),
    loadConfig: jasmine.createSpy('loadConfig').and.returnValue(Promise.resolve(mockConfig))
  };

  beforeEach(async () => {
    credentialsSubject = new Subject<void>();
  
    mockTokenService = {
      getUserFullName: jasmine.createSpy('getUserFullName').and.returnValue('Mocked User'),
      credentialsEmitter: credentialsSubject
    };
  
    await TestBed.configureTestingModule({
      imports: [
        AppHeaderComponent,
        BrowserAnimationsModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
        { provide: AppConfigService, useValue: mockAppConfigService },
        { provide: TokenService, useValue: mockTokenService }
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
      'https://intranet.gov.bc.ca/bcws/corporate-governance/strategic-initiatives-and-innovation/wildfire-one/wildfire-one-training',
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

  it('should set currentUser when credentialsEmitter emits a value', () => {
    const mockName = 'Mike David';
    mockTokenService.getUserFullName.and.returnValue(mockName);
  
    component.ngOnInit();
    credentialsSubject.next();
  
    expect(component.currentUser).toBe(mockName);
  });
  
});
