import { CommonModule } from '@angular/common';
import { Component, OnInit} from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { ActivatedRoute } from '@angular/router';
import { ProjectService } from 'src/app/services/project-services';
import { convertFiscalYear } from 'src/app/utils/tools';
import { MatTableModule } from '@angular/material/table';

@Component({
  selector: 'app-fiscal-year-projects',
  standalone: true,
  imports: [MatExpansionModule,CommonModule, MatTableModule],
  templateUrl: './fiscal-year-projects.component.html',
  styleUrl: './fiscal-year-projects.component.scss'
})
export class FiscalYearProjectsComponent implements OnInit{
  projectFiscals: any[] = [];

  //this fake data will be replaced by fiscalActivities once it is ready
  activities = [
    { name: 'Site Prep', description: 'Preparing the site for seeding', startDate:'2024-02-15', endDate: '2024-03-15', completedHectares: 50 },
    { name: 'Seeding', description: 'Planting seeds for reforestation', endDate: '2024-05-10', completedHectares: 100 },
    { name: 'Monitoring', description: 'Tracking vegetation growth', endDate: '2024-08-25', completedHectares: 75 },
  ];
  displayedColumnsComplete: string[] = ['name', 'description', 'endDate', 'completedHectares'];
  displayedColumnsPlanned: string[] = ['name', 'description', 'startDate', 'endDate', 'plannedHectares'];

  completeFiscalDisplayedColumns: string[] = ['name', 'description', 'endDate', 'completedHectares'];
  planFiscalStatusMap: Record<string, string> = {
    "DRAFT": "Draft",
    "PROPOSED": "Proposed",
    "IN_PROG": "In Progress",
    "COMPLETE": "Complete",
    "ABANDONED": "Abandoned",
    "PREPARED": "Prepared"
  };

  convertFiscalYear = convertFiscalYear
  constructor(
    private route: ActivatedRoute,
    private projectService: ProjectService
  ) {}
  ngOnInit(): void {
      this.loadProjectFiscals();
  }

  loadProjectFiscals(): void {
    const projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid');
    if (projectGuid) {
      this.projectService.getProjectFiscalsByProjectGuid(projectGuid).subscribe((data) => {
        this.projectFiscals = (data._embedded?.projectFiscals || []).sort((a: { fiscalYear: number; }, b: { fiscalYear: number; }) => a.fiscalYear - b.fiscalYear);
      });
    }
  }

  getStatusIcon(statusCode: string): string {
    const iconMap: Record<string, string> = {
      "DRAFT": "draft-icon.svg",
      "PROPOSED": "proposed-icon.svg",
      "IN_PROG": "in-progress-icon.svg",
      "COMPLETE": "complete-icon.svg",
      "ABANDONED": "abandoned-icon.svg",
      "PREPARED": "prepared-icon.svg"
    };
    return iconMap[statusCode]
  }
  
  getPlanFiscalStatus(statusCode: string): string {
    return this.planFiscalStatusMap[statusCode] || "Unknown";
  }
  
}
