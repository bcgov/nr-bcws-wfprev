import { Component, OnInit } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-edit-project',
  standalone: true,
  imports: [MatTabsModule],
  templateUrl: './edit-project.component.html',
  styleUrl: './edit-project.component.scss'
})
export class EditProjectComponent implements OnInit {
  projectName: string | null = null;

  constructor(private route: ActivatedRoute) {}
  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.projectName = params.get('name');
    });
  }

}
