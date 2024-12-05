import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EditProjectComponent } from 'src/app/components/edit-project/edit-project.component';


const routes: Routes = [
  { path: '', component: EditProjectComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes), EditProjectComponent]
})
export class EditProjectModule {}