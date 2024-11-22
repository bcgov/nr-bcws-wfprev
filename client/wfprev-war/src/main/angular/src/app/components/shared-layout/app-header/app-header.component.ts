import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

@Component({
  selector: 'app-app-header',
  standalone: true,
  imports: [MatMenuModule, MatButtonModule, MatIconModule, BrowserAnimationsModule],
  templateUrl: './app-header.component.html',
  styleUrl: './app-header.component.scss'
})
export class AppHeaderComponent {

  constructor(
    protected router: Router,
  ) {
  }


  environment:string = 'dev'
  title:string = 'WFPREV'

  onLogoutClick(){
    // this.tokenService.clearLocalStorageToken();
  }

  onBCLogoClick(){
      this.router.navigate([ResourcesRoutes.LANDING]); // Navigate back to the home page
  }

  onSupportLinkClick() {
    //navigate to a support link page, upon decide which url would that be.
    const url = 'https://intranet.gov.bc.ca/bcws/provincial-programs/strategic-initiatives-and-innovation/wildfire-one/wildfire-one-training'
    window.open(url, '_blank');
  }
}
