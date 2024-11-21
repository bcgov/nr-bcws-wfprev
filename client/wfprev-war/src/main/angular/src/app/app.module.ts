import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { DateTimeProvider, OAuthLogger, OAuthService, UrlHelperService } from 'angular-oauth2-oidc';
import { ROUTING } from 'src/app/app.routing';
import { environment } from '../environments/environment';
import { AppComponent } from './app.component';
import { CoreUIModule } from './lib/core-ui.module';
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
    OAuthService,
    UrlHelperService,
    { provide: OAuthLogger, useClass: CustomOAuthLogger },
    { provide: DateTimeProvider, useClass: CustomDateTimeProvider },
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }