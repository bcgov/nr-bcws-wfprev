import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  activeRoute = '';
  
  constructor(
    protected router: Router,
    protected appConfigService: AppConfigService,
    protected tokenService: TokenService,
    protected dialog: MatDialog
  ) {
  }
  
  goHome(): void {
    this.router.navigate([ResourcesRoutes.LANDING]); // Navigate back to the home page
  }
}
