import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { CreateNewProjectDialogComponent } from 'src/app/components/create-new-project-dialog/create-new-project-dialog.component';
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


  setActive(menuItem: string): void {
    this.activeRoute = menuItem;
    switch (menuItem) {
      case 'list':
        this.router.navigate([ResourcesRoutes.LIST]);
        break;
      case 'map':
        this.router.navigate([ResourcesRoutes.MAP]);
        break;
    }
  }

  goHome(): void {
    this.router.navigate([ResourcesRoutes.LANDING]); // Navigate back to the home page
  }

  createNewProject(): void {
    this.dialog.open(CreateNewProjectDialogComponent, {
      width: '880px',
      disableClose: true,
      hasBackdrop: true,
    });
  }
}
