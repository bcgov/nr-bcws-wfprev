import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { PermissionsService, WFPREV_ACTIONS } from 'src/app/services/permissions.service';
import { ResourcesRoutes } from 'src/app/utils';

@Component({
  selector: 'wfprev-year-end-performance-update',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './year-end-performance-update.component.html',
  styleUrl: './year-end-performance-update.component.scss'
})
export class YearEndPerformanceUpdateComponent implements OnInit {
  projectGuid: string = '';
  fiscalGuid: string = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly permissionsService: PermissionsService
  ) {}

  ngOnInit(): void {
    if (!this.permissionsService.hasAction(WFPREV_ACTIONS.CREATE_YEAR_END_REPORT)) {
      this.router.navigate(['/' + ResourcesRoutes.ERROR_PAGE]);
      return;
    }

    this.projectGuid = this.route.snapshot.queryParamMap.get('projectGuid') || '';
    this.fiscalGuid = this.route.snapshot.queryParamMap.get('fiscalGuid') || '';
  }

  goBack(): void {
    this.router.navigate(['/' + ResourcesRoutes.EDIT_PROJECT], {
      queryParams: {
        projectGuid: this.projectGuid,
        fiscalGuid: this.fiscalGuid
      }
    });
  }
}
