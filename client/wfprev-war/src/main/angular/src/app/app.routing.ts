import { RouterModule, Routes } from '@angular/router';
import { ListComponent } from 'src/app/components/list/list.component';
import { MapComponent } from 'src/app/components/map/map.component';
import { ResourcesRoutes } from 'src/app/utils';

const PANEL_ROUTES: Routes = [
  {
    path: ResourcesRoutes.MAP, // This resolves to 'map'
    component: MapComponent,
    pathMatch: 'full',
  },
  {
    path: ResourcesRoutes.LIST, // This resolves to 'list'
    component: ListComponent,
    pathMatch: 'full',
  }
];

export const ROUTING = RouterModule.forRoot(PANEL_ROUTES, {});