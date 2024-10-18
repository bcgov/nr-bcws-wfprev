import { APP_INITIALIZER, Injector, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { AppComponent } from './app.component';
import { ROUTING } from 'src/app/app.routing';
import { initializeAppConfig } from './app-initializer';
import { environment } from '../environments/environment';
import { CoreUIModule } from './lib/core-ui.module';
import { AppConfigService } from './services/app-config.service';
import { DateTimeProvider, OAuthLogger, OAuthService, UrlHelperService } from 'angular-oauth2-oidc';
import { CustomDateTimeProvider, CustomOAuthLogger } from './utils';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    ROUTING,
    CoreUIModule.forRoot({
      configurationPath: environment.app_config_location,
    }),
  ],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAppConfig,
      multi: true,
      deps: [AppConfigService],
    },
    OAuthService,
    UrlHelperService,
    { provide: OAuthLogger, useClass: CustomOAuthLogger },
    { provide: DateTimeProvider, useClass: CustomDateTimeProvider },
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }