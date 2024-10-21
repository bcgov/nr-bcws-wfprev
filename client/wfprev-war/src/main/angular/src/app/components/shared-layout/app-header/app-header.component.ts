import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';

@Component({
  selector: 'app-app-header',
  standalone: true,
  imports: [],
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
  onMenuClick(){

  }

  onLogoutClick(){

  }

  onBCLogoClick(){
      this.router.navigate([ResourcesRoutes.LANDING]); // Navigate back to the home page
  }
}
