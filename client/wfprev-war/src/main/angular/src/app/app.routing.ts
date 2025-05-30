import { RouterModule, Routes } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { ErrorPageComponent } from './components/error-page/error-page/error-page.component';
import { PrevAuthGuard } from './services/util/prev-auth-guard';
import { ROLES_UI } from './shared/scopes';

const PROFILE_SCOPES = [[ROLES_UI.ADMIN]];

const PANEL_ROUTES: Routes = [
  {
    path: ResourcesRoutes.LIST,
    loadChildren: () =>
      import('src/app/components/list.module').then(m => m.ListModule),
    canActivate: [PrevAuthGuard],
    data: { scopes: PROFILE_SCOPES },
  },
  {
    path: ResourcesRoutes.MAP,
    loadChildren: () =>
      import('src/app/components/map.module').then(m => m.MapModule),
    canActivate: [PrevAuthGuard],
    data: { scopes: PROFILE_SCOPES }
  },
  {
    path: ResourcesRoutes.ERROR_PAGE,
    component: ErrorPageComponent,
    pathMatch: 'full',
  },
  {
    path: ResourcesRoutes.EDIT_PROJECT,
    loadChildren: () =>
      import('src/app/components/edit-project-routing.module').then(m => m.EditProjectRoutingModule),
    canActivate: [PrevAuthGuard],
    data: { scopes: PROFILE_SCOPES }
  },
  { 
    path: '', 
    redirectTo: ResourcesRoutes.MAP,
    pathMatch: 'full'
  }
];

export const ROUTING = RouterModule.forRoot(PANEL_ROUTES, {});