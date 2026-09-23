import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AppConfigService } from 'src/app/services/app-config.service';
import { SmkService } from 'src/app/services/smk.service';
import { TokenService } from 'src/app/services/token.service';
import { ResourcesRoutes } from 'src/app/utils';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false
})
export class AppComponent implements OnInit {
  activeRoute = '';

  constructor(
    protected router: Router,
    protected appConfigService: AppConfigService,
    protected tokenService: TokenService,
    protected dialog: MatDialog,
    private readonly smkService: SmkService
  ) {
  }

  ngOnInit(): void {
    const config = this.appConfigService.getConfig();
    if (config) {
      console.log(`Environment: ${config.application.environment || 'local'}`,
        `SHA: ${config.application.buildId || 'unknown'}`,
        `Run Number: ${config.application.buildNumber || '0'}`);
    }
    // Every map needs SMK's modules; load them while the browser is idle rather than when the first map opens
    this.smkService.preload();
  }

  goHome(): void {
    this.router.navigate([ResourcesRoutes.LANDING]); // Navigate back to the home page
  }
}
