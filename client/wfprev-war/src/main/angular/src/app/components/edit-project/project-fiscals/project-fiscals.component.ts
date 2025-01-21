import { CommonModule, CurrencyPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute } from '@angular/router';
import { ProjectService } from 'src/app/services/project-services';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatExpansionModule } from '@angular/material/expansion';

@Component({
  selector: 'app-project-fiscals',
  templateUrl: './project-fiscals.component.html',
  styleUrls: ['./project-fiscals.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTabsModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatExpansionModule,
    CurrencyPipe
  ]
})
export class ProjectFiscalsComponent implements OnInit {
  projectGuid = '';
  projectFiscals: any[] = [];
  fiscalForms: FormGroup[] = [];
  fiscalYears: string[] = [];
  selectedTabIndex = 0;

  constructor(
    private route: ActivatedRoute,
    private projectService: ProjectService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.generateFiscalYears();
    this.loadProjectFiscals();
  }

  private generateFiscalYears(): void {
    const currentYear = new Date().getFullYear();
    const startYear = currentYear - 5; // 5 years in the past
    const endYear = currentYear + 5;  // 5 years in the future
    this.fiscalYears = Array.from({ length: endYear - startYear + 1 }, (_, i) => {
      const year = startYear + i;
      return `${year}/${(year + 1).toString().slice(-2)}`;
    });
  }
  


  private createFiscalForm(fiscal?: any): FormGroup {
    return this.fb.group({
      fiscalYear: [fiscal?.fiscalYear || '', [Validators.required]],
      projectFiscalName: [fiscal?.projectFiscalName || '', [Validators.required]],
      activityCategoryCode: [fiscal?.activityCategoryCode || ''],
      proposalType: [fiscal?.proposalType || ''],
      planFiscalStatusCode: [fiscal?.planFiscalStatusCode || ''],
      fiscalPlannedProjectSizeHa: [fiscal?.fiscalPlannedProjectSizeHa || ''],
      fiscalCompletedSizeHa: [fiscal?.fiscalCompletedSizeHa ?? ''],
      resultsOpeningId: [fiscal?.resultsOpeningId || ''],
      firstNationsEngagementInd: [fiscal?.firstNationsEngagementInd || false],
      firstNationsDelivPartInd: [fiscal?.firstNationsDelivPartInd || false],
      firstNationsPartner: [fiscal?.firstNationsPartner || ''],
      projectFiscalDescription: [fiscal?.projectFiscalDescription || '', [Validators.required]],
      otherPartners: [fiscal?.otherPartners || ''],
      totalCostEstimateAmount: [fiscal?.totalCostEstimateAmount ?? ''],
      forecastAmount: [fiscal?.forecastAmount ?? ''],
      cfsProjectCode: [fiscal?.cfsProjectCode || ''],
      ancillaryFundingSourceGuid: [fiscal?.ancillaryFundingSourceGuid || ''],
      fiscalAncillaryFundAmount: [fiscal?.fiscalAncillaryFundAmount ?? ''],
      fiscalReportedSpendAmount: [fiscal?.fiscalReportedSpendAmount ?? ''],
      cfsActualSpend: [fiscal?.cfsActualSpend || '']
    });
  }

  loadProjectFiscals(): void {
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';
    if (!this.projectGuid) return;
  
    this.projectService.getProjectFiscalsByProjectGuid(this.projectGuid).subscribe({
      next: (data) => {
        this.projectFiscals = (data._embedded.projectFiscals || []).map((fiscal:any) => {
          return {
            ...fiscal,
            fiscalYearFormatted: `${fiscal.fiscalYear}/${(fiscal.fiscalYear + 1).toString().slice(-2)}`
          };
        });
        this.fiscalForms = this.projectFiscals.map((fiscal) => this.createFiscalForm(fiscal));
      },
      error: (err) => {
        console.error('Error fetching project details:', err);
        this.projectFiscals = [];
        this.fiscalForms = [];
      },
    });
  }
  

  addNewFiscal(): void {
    const newFiscalData = { fiscalYear: '', projectFiscalName: '' };
    this.projectFiscals.push(newFiscalData);
    this.fiscalForms.push(this.createFiscalForm(newFiscalData));
    this.selectedTabIndex = this.projectFiscals.length - 1; // Navigate to the newly added tab
  }

  onCancelFiscal() {

  }

  onSaveFiscal() {
    
  }
}
