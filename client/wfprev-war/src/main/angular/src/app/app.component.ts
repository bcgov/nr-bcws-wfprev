import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'Wildfire Prevention';
  activeRoute = '';
  
  constructor(
    protected router: Router,
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
}
