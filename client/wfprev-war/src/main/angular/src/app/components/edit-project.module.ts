import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EditProjectComponent } from 'src/app/components/edit-project/edit-project.component';
import { CanDeactivateGuard } from 'src/app/services/util/can-deactive.guard';

const routes: Routes = [
  {
    path: '',
    component: EditProjectComponent,
    canDeactivate: [CanDeactivateGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [CanDeactivateGuard]
})
export class EditProjectModule {}
