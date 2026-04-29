import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import { ResourcesRoutes } from 'src/app/utils';
import { PermissionsService } from './services/permissions.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  activeRoute = '';

  constructor(
    protected router: Router,
    protected appConfigService: AppConfigService,
    protected tokenService: TokenService,
    protected dialog: MatDialog,
    protected permissionsService: PermissionsService,
  ) {
  }

  ngOnInit(): void {
    const config = this.appConfigService.getConfig();
    if (config) {
      console.log(`Environment: ${config.application.environment || 'local'}`,
        `SHA: ${config.application.buildId || 'unknown'}`,
        `Run Number: ${config.application.buildNumber || '0'}`);
    }

    // Load permissions from token on app initialization
    this.tokenService.credentialsEmitter.subscribe({
      next: (tokenDetails) => {
        this.permissionsService.loadFromCredentials(tokenDetails);
      },
      error: () => {
        this.permissionsService.clearScopes();
      },
    });
  }

  goHome(): void {
    this.router.navigate([ResourcesRoutes.LANDING]); // Navigate back to the home page
  }
}
