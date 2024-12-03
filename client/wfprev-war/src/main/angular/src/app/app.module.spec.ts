import { TestBed } from '@angular/core/testing';
import { AppModule } from './app.module';
import { AppComponent } from './app.component';
import { CoreUIModule } from './lib/core-ui.module';
import { OAuthService, OAuthLogger, UrlHelperService, DateTimeProvider } from 'angular-oauth2-oidc';
import { CustomOAuthLogger, CustomDateTimeProvider } from './utils';
import { MatMenuModule } from '@angular/material/menu';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialogModule } from '@angular/material/dialog';
import { ErrorPageComponent } from './components/error-page/error-page/error-page.component';

describe('AppModule', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        AppModule,
        MatMenuModule,
        BrowserAnimationsModule,
        MatDialogModule,
      ],
    });
  });

  it('should import CoreUIModule with correct configuration', () => {
    const coreModule = TestBed.inject(CoreUIModule);
    expect(coreModule).toBeTruthy();
  });

  it('should provide CustomOAuthLogger for OAuthLogger', () => {
    const oauthLogger = TestBed.inject(OAuthLogger);
    expect(oauthLogger instanceof CustomOAuthLogger).toBeTrue();
  });

  it('should declare ErrorPageComponent', () => {
    const errorPageComponent = TestBed.createComponent(ErrorPageComponent);
    expect(errorPageComponent).toBeTruthy();
  });
});