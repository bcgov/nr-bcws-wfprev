import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatMenuModule } from '@angular/material/menu';
import { ReactiveFormsModule, FormGroup, FormControl } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FiscalYearProjectsComponent } from 'src/app/components/edit-project/project-details/fiscal-year-projects/fiscal-year-projects.component';

const meta: Meta<FiscalYearProjectsComponent> = {
  title: 'FiscalYearProjectsComponent',
  component: FiscalYearProjectsComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [
        FiscalYearProjectsComponent,
        MatTabsModule,
        MatButtonModule,
        MatSelectModule,
        MatSlideToggleModule,
        MatExpansionModule,
        MatMenuModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<FiscalYearProjectsComponent>;

export const Default: Story = {
  args: {
    projectFiscals: [
      { fiscalYearFormatted: '2023/24' },
      { fiscalYearFormatted: '2024/25' }
    ],
    fiscalForms: [
      new FormGroup({
        fiscalYear: new FormControl('2023/24'),
        projectFiscalName: new FormControl('Project Alpha'),
        activityCategoryCode: new FormControl('A1'),
        planFiscalStatusCode: new FormControl('Active'),
        fiscalPlannedProjectSizeHa: new FormControl(100),
        fiscalCompletedSizeHa: new FormControl(50),
        resultsOpeningId: new FormControl('R1234'),
        firstNationsEngagementInd: new FormControl(true),
        firstNationsDelivPartInd: new FormControl(false),
        firstNationsPartner: new FormControl('First Nation Org'),
        projectFiscalDescription: new FormControl('This is a sample project for the fiscal activity.'),
        otherPartner: new FormControl('Company X'),
        totalCostEstimateAmount: new FormControl(50000),
        fiscalForecastAmount: new FormControl(60000),
        cfsProjectCode: new FormControl('CFS123'),
        ancillaryFundingSourceGuid: new FormControl('FUND001'),
        fiscalAncillaryFundAmount: new FormControl(10000),
        fiscalReportedSpendAmount: new FormControl(25000),
        fiscalActualAmount: new FormControl(20000),
      }),
    ],
    fiscalYears: ['2023/24', '2024/25'],
    activityCategoryCode: [{ activityCategoryCode: 'A1', description: 'Category A' }],
    planFiscalStatusCode: [{ planFiscalStatusCode: 'Active', description: 'Active' }],
    ancillaryFundingSourceCode: [{ ancillaryFundingSourceGuid: 'FUND001', fundingSourceName: 'Government Fund' }],
  },
};

export const WithNewFiscal: Story = {
  args: {
    projectFiscals: [
      { fiscalYearFormatted: '2025/26' }
    ],
    fiscalForms: [
      new FormGroup({
        fiscalYear: new FormControl('2025/26'),
        projectFiscalName: new FormControl('New Fiscal Project'),
        activityCategoryCode: new FormControl('B2'),
        planFiscalStatusCode: new FormControl('Pending'),
        fiscalPlannedProjectSizeHa: new FormControl(200),
        fiscalCompletedSizeHa: new FormControl(0),
        resultsOpeningId: new FormControl('R5678'),
        firstNationsEngagementInd: new FormControl(false),
        firstNationsDelivPartInd: new FormControl(true),
        firstNationsPartner: new FormControl('New Partner Org'),
        projectFiscalDescription: new FormControl('This is a newly created fiscal activity project.'),
        otherPartner: new FormControl('Company Y'),
        totalCostEstimateAmount: new FormControl(75000),
        fiscalForecastAmount: new FormControl(80000),
        cfsProjectCode: new FormControl('CFS567'),
        ancillaryFundingSourceGuid: new FormControl('FUND002'),
        fiscalAncillaryFundAmount: new FormControl(15000),
        fiscalReportedSpendAmount: new FormControl(5000),
        fiscalActualAmount: new FormControl(3000),
      }),
    ],
    fiscalYears: ['2025/26'],
    activityCategoryCode: [{ activityCategoryCode: 'B2', description: 'Category B' }],
    planFiscalStatusCode: [{ planFiscalStatusCode: 'Pending', description: 'Pending' }],
    ancillaryFundingSourceCode: [{ ancillaryFundingSourceGuid: 'FUND002', fundingSourceName: 'Private Investment' }],
  },
};
