import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MapComponent } from 'src/app/components/map/map.component';


const routes: Routes = [
  { path: '', component: MapComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes), MapComponent]
})
export class MapModule {}