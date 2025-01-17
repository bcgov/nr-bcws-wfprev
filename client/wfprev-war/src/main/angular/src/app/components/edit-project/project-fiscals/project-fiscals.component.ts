import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';

@Component({
  selector: 'app-project-fiscals',
  standalone: true,
  imports: [ReactiveFormsModule,MatExpansionModule,CommonModule,FormsModule],
  templateUrl: './project-fiscals.component.html',
  styleUrl: './project-fiscals.component.scss'
})
export class ProjectFiscalsComponent {

}
