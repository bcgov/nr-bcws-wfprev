import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-app-header',
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
export class AppHeaderComponent {

  constructor(
    protected router: Router,
  ) {
  }


  environment:string = 'DEV'
  title:string = 'PREVENTION'
  currentUser: string = 'User_1'

  onLogoutClick(){
    //clear token will be implemented after authorization piece done
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
