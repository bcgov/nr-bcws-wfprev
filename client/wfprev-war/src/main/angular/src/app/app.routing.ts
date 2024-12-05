import { RouterModule, Routes } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';

const PANEL_ROUTES: Routes = [
  {
    path: ResourcesRoutes.LIST,
    loadChildren: () =>
      import('src/app/components/list.module').then(m => m.ListModule),
  },
  {
    path: ResourcesRoutes.MAP,
    loadChildren: () =>
      import('src/app/components/map.module').then(m => m.MapModule),
  },
  { path: '', redirectTo: ResourcesRoutes.MAP, pathMatch: 'full' }, // Default route to map
];

export const ROUTING = RouterModule.forRoot(PANEL_ROUTES, {});