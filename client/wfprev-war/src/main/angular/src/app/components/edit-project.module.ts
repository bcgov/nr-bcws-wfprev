import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TextFieldModule } from '@angular/cdk/text-field';
import { EditProjectRoutingModule } from './edit-project-routing.module';
import { EditProjectComponent } from './edit-project/edit-project.component';
import { ProjectDetailsComponent } from './edit-project/project-details/project-details.component';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTooltipModule,
    TextFieldModule,
    EditProjectRoutingModule,
    EditProjectComponent,
    ProjectDetailsComponent 
  ]
})
export class EditProjectModule {}