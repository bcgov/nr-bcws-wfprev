import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { ProjectService } from 'src/app/services/project-services';
import { convertFiscalYear } from 'src/app/utils/tools';
import { ExpansionIndicatorComponent } from "../../../shared/expansion-indicator/expansion-indicator.component";
import { StatusBadgeComponent } from 'src/app/components/shared/status-badge/status-badge.component';

@Component({
  selector: 'wfprev-fiscal-year-projects',
  standalone: true,
  imports: [MatExpansionModule, CommonModule, MatTableModule, ExpansionIndicatorComponent, StatusBadgeComponent],
  templateUrl: './fiscal-year-projects.component.html',
  styleUrl: './fiscal-year-projects.component.scss'
})
export class FiscalYearProjectsComponent implements OnInit{
  projectFiscals: any[] = [];
  activities: any[] = [];
  activitiesMap: { [fiscalGuid: string]: any[] } = {};
  projectGuid: string = '';

  displayedColumnsComplete: string[] = ['name', 'description', 'endDate', 'completedHectares'];
  displayedColumnsPlanned: string[] = ['name', 'description', 'startDate', 'endDate', 'plannedHectares'];

  completeFiscalDisplayedColumns: string[] = ['name', 'description', 'endDate', 'completedHectares'];
  planFiscalStatusMap: Record<string, string> = {
    "DRAFT": "Draft",
    "PROPOSED": "Proposed",
    "IN_PROG": "In Progress",
    "COMPLETE": "Complete",
    "CANCELLED": "Cancelled",
    "PREPARED": "Prepared"
  };

  convertFiscalYear = convertFiscalYear;
  
  constructor(
    private route: ActivatedRoute,
    private projectService: ProjectService
  ) {}

  ngOnInit(): void {
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';
    if (this.projectGuid) {
      this.loadProjectFiscals();
    }
  }

  loadProjectFiscals(): void {
    this.projectService.getProjectFiscalsByProjectGuid(this.projectGuid).subscribe({
      next: (data) => {
        this.projectFiscals = (data._embedded?.projectFiscals || []).map((fiscal: any) => ({
          ...fiscal,
          fiscalYearFormatted: `${fiscal.fiscalYear}/${(fiscal.fiscalYear + 1).toString().slice(-2)}`,
        })).sort((a: { fiscalYear: number }, b: { fiscalYear: number }) => b.fiscalYear - a.fiscalYear);
        
        // Load activities for each fiscal
        this.projectFiscals.forEach(fiscal => {
          this.loadActivities(fiscal.projectPlanFiscalGuid);
        });
      },
      error: (error) => {
        console.error('Error fetching project fiscals:', error);
      }
    });
  }

  loadActivities(fiscalGuid: string): void {
    this.projectService.getFiscalActivities(this.projectGuid, fiscalGuid).subscribe({
      next: (data) => {
        if (data && data._embedded?.activities) {
          const fiscalActivities = data._embedded.activities.map((activity: any) => ({
            name: activity.activityName,
            description: activity.activityDescription,
            startDate: this.formatDate(activity.activityStartDate),
            endDate: this.formatDate(activity.activityEndDate),
            completedHectares: activity.completedAreaHa,
            plannedHectares: activity.plannedTreatmentAreaHa
          }));
          this.activitiesMap[fiscalGuid] = fiscalActivities;
        } else {
          this.activitiesMap[fiscalGuid] = [];
        }
      },
      error: (error) => {
        console.error('Error fetching activities:', error);
        this.activitiesMap[fiscalGuid] = [];
      }
    });
  }

  private formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toISOString().split('T')[0];
  }

  getStatusIcon(statusCode: string): string {
    const iconMap: Record<string, string> = {
      "DRAFT": "draft-icon.svg",
      "PROPOSED": "proposed-icon.svg",
      "IN_PROG": "in-progress-icon-only.svg",
      "COMPLETE": "complete-icon.svg",
      "CANCELLED": "cancelled-icon.svg",
      "PREPARED": "prepared-icon.svg"
    };
    return iconMap[statusCode]
  }
  
  getPlanFiscalStatus(statusCode: string): string {
    return this.planFiscalStatusMap[statusCode] || "Unknown";
  }
  
}
