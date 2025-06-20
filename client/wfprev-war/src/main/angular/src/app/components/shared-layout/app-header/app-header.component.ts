import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { TokenService } from 'src/app/services/token.service';
import { AppConfigService } from 'src/app/services/app-config.service';
import { EnvironmentIndicators } from 'src/app/utils/constants';

@Component({
  selector: 'wfprev-app-header',
  standalone: true,
  imports: [
    CommonModule, // Replace BrowserModule with CommonModule
    MatMenuModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './app-header.component.html',
  styleUrls: ['./app-header.component.scss']
})
export class AppHeaderComponent implements OnInit {

  constructor(
    protected router: Router,
    private readonly tokenService: TokenService,
    private readonly appConfigService: AppConfigService
  ) {
  }

  environment: string = ''
  title: string = 'PREVENTION'
  currentUser: string = 'User_1'
  readonly EnvironmentIndicators = EnvironmentIndicators;

  ngOnInit(): void {
    this.tokenService.credentialsEmitter.subscribe(() => {
      const name = this.tokenService.getUserFullName();
      if (name) {
        this.currentUser = name;
      }
    });

    // Display no environment indicator in prod
    // Case sensitive checking, set the variable as upper case 
    const env = (this.appConfigService.getConfig()?.application?.environment || '').toUpperCase();
    switch (env) {
      case EnvironmentIndicators.LOCAL_ENV:
        this.environment = EnvironmentIndicators.LOCAL_ENV_DISPLAY_IND;
        break;
      case EnvironmentIndicators.DEV_ENV:
        this.environment = EnvironmentIndicators.DEV_ENV_DISPLAY_IND;
        break;
      case EnvironmentIndicators.TEST_ENV:
        this.environment = EnvironmentIndicators.TEST_ENV_DISPLAY_IND;
        break;
      // set no value for PROD
      default:
        this.environment = '';
        break;
    }
  }


  onBCLogoClick() {
    this.router.navigate([ResourcesRoutes.LANDING]); // Navigate back to the home page
  }

  onSupportLinkClick() {
    //navigate to a support link page, upon decide which url would that be.
    const url = 'https://intranet.gov.bc.ca/bcws/corporate-governance/strategic-initiatives-and-innovation/wildfire-one/wildfire-one-training'
    window.open(url, '_blank', 'noopener');
  }
}
