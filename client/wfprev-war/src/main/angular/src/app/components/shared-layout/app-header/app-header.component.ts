import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { TokenService } from 'src/app/services/token.service';
import { AppConfigService } from 'src/app/services/app-config.service';

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
    this.environment = env === 'PROD' ? '' : env;

    console.log('AppConfig environment:', JSON.stringify(this.appConfigService.getConfig(), null, 2));
    console.log('Computed environment:', this.environment);
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
