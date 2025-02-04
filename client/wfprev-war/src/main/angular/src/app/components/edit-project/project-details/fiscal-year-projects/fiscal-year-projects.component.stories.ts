import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { MatExpansionModule } from '@angular/material/expansion';
import { CommonModule } from '@angular/common';
import { FiscalYearProjectsComponent } from 'src/app/components/edit-project/project-details/fiscal-year-projects/fiscal-year-projects.component';

const meta: Meta<FiscalYearProjectsComponent> = {
  title: 'FiscalYearProjectsComponent',
  component: FiscalYearProjectsComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [MatExpansionModule, CommonModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<FiscalYearProjectsComponent>;

export const Default: Story = {
  args: {
    projectFiscals: [
      {
        projectFiscalName: 'Fiscal Year 2024',
        fiscalYear: 2024,
        planFiscalStatusCode: 'PLANNED',
        fiscalPlannedProjectSizeHa: 150,
        totalCostEstimateAmount: 500000,
        lastProgressUpdateTimestamp: '2024-01-15',
      },
    ],
  },
};

export const WithCompletedFiscal: Story = {
  args: {
    projectFiscals: [
      {
        projectFiscalName: 'Fiscal Year 2023',
        fiscalYear: 2023,
        planFiscalStatusCode: 'COMPLETE',
        fiscalPlannedProjectSizeHa: 120,
        fiscalReportedSpendAmount: 450000,
        fiscalActualAmount: 430000,
        lastProgressUpdateTimestamp: '2023-12-10',
      },
    ],
  },
};

export const EmptyState: Story = {
  args: {
    projectFiscals: [],
  },
};
