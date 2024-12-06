import { TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppModule } from './app.module';
import { ErrorPageComponent } from './components/error-page/error-page/error-page.component';
import { CoreUIModule } from './lib/core-ui.module';
import { LibraryConfig } from './config/library-config';
import { AppConfigService } from './services/app-config.service';
import { APP_INITIALIZER } from '@angular/core';
import { TokenService } from './services/token.service';
 
describe('AppModule', () => {
  let mockAppConfigService: any;
  let mockTokenService: any;

  mockAppConfigService = {
    configEmitter: {
      subscribe: jasmine.createSpy('subscribe').and.callFake((callback: any) => {
        callback({}); // Simulate an emitted configuration
      }),
    },
    loadAppConfig: jasmine.createSpy('loadAppConfig').and.returnValue(Promise.resolve()),
  };

  mockTokenService = {
    someMethod: jasmine.createSpy('someMethod'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppModule, MatMenuModule, BrowserAnimationsModule, MatDialogModule],
      providers: [
        { provide: LibraryConfig, useValue: {} },
        { provide: AppConfigService, useValue: mockAppConfigService },
        { provide: TokenService, useValue: mockTokenService },
        {
          provide: APP_INITIALIZER,
          useValue: () => Promise.resolve(),
          multi: true,
        },
      ],
      teardown: { destroyAfterEach: true },
    });
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should import CoreUIModule with correct configuration', () => {
    const coreModule = TestBed.inject(CoreUIModule);
    expect(coreModule).toBeTruthy();
  });

  it('should declare ErrorPageComponent', () => {
    const errorPageComponent = TestBed.createComponent(ErrorPageComponent);
    expect(errorPageComponent).toBeTruthy();
  });
});