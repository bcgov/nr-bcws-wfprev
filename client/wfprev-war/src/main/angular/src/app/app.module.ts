import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { DateTimeProvider, OAuthLogger, OAuthService, UrlHelperService } from 'angular-oauth2-oidc';
import { ROUTING } from 'src/app/app.routing';
import { environment } from '../environments/environment';
import { AppComponent } from './app.component';
import { CoreUIModule } from './lib/core-ui.module';
import { CustomDateTimeProvider, CustomOAuthLogger } from './utils';
import { AppHeaderComponent } from 'src/app/components/shared-layout/app-header/app-header.component';
import { MatMenuModule } from '@angular/material/menu';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialogModule } from '@angular/material/dialog';
import { ErrorPageComponent } from './components/error-page/error-page/error-page.component';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    ROUTING,
    CoreUIModule.forRoot({
      configurationPath: environment.app_config_location,
    }),
    AppHeaderComponent,
    MatMenuModule,
    BrowserAnimationsModule,
    MatDialogModule,
    ErrorPageComponent
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