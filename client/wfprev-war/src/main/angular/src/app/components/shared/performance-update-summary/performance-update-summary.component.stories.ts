import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { PerformanceUpdateSummaryComponent } from './performance-update-summary.component';

const meta: Meta<PerformanceUpdateSummaryComponent> = {
  title: 'Components/Shared/PerformanceUpdateSummary',
  component: PerformanceUpdateSummaryComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<PerformanceUpdateSummaryComponent>;

export const Default: Story = {
  args: {
    update: {
      submittedBy: 'Jane Smith',
      generalUpdateComment: 'Project is progressing well. All milestones on track.',
      forecastAmount: 50000,
      forecastAdjustmentAmount: 0,
      forecastAdjustmentRationale: '',
      budgetHighRiskAmount: 0,
      budgetHighRiskRationale: '',
      budgetMediumRiskAmount: 0,
      budgetMediumRiskRationale: '',
      budgetLowRiskAmount: 0,
      budgetLowRiskRationale: '',
      budgetCompletedAmount: 50000,
      budgetCompletedDescription: 'All work completed.',
      totalAmount: 50000,
    } as any,
  },
};

export const ForecastIncreased: Story = {
  args: {
    update: {
      submittedBy: 'John Doe',
      generalUpdateComment: 'Additional resources were required due to scope expansion.',
      forecastAmount: 75000,
      forecastAdjustmentAmount: 25000,
      forecastAdjustmentRationale: 'Scope expanded to include additional treatment areas.',
      budgetHighRiskAmount: 20000,
      budgetHighRiskRationale: 'Supply chain delays may impact delivery.',
      budgetMediumRiskAmount: 15000,
      budgetMediumRiskRationale: 'Weather conditions uncertain.',
      budgetLowRiskAmount: 10000,
      budgetLowRiskRationale: 'Minor scheduling conflicts.',
      budgetCompletedAmount: 30000,
      budgetCompletedDescription: 'Phase 1 complete.',
      totalAmount: 75000,
    } as any,
  },
};

export const ForecastDecreased: Story = {
  args: {
    update: {
      submittedBy: 'Alice Johnson',
      generalUpdateComment: 'Scope reduced after site assessment.',
      forecastAmount: 35000,
      forecastAdjustmentAmount: -15000,
      forecastAdjustmentRationale: 'Reduced treatment area after site assessment.',
      budgetHighRiskAmount: 5000,
      budgetHighRiskRationale: 'Equipment availability.',
      budgetMediumRiskAmount: 5000,
      budgetMediumRiskRationale: 'Contractor availability.',
      budgetLowRiskAmount: 5000,
      budgetLowRiskRationale: 'Minor logistics.',
      budgetCompletedAmount: 20000,
      budgetCompletedDescription: 'Initial phase completed.',
      totalAmount: 35000,
    } as any,
  },
};

export const AllRiskLevels: Story = {
  args: {
    update: {
      submittedBy: 'Bob Williams',
      generalUpdateComment: 'Mixed progress across treatment areas with varying risk levels.',
      forecastAmount: 100000,
      forecastAdjustmentAmount: 10000,
      forecastAdjustmentRationale: 'Additional contingency added for high risk areas.',
      budgetHighRiskAmount: 30000,
      budgetHighRiskRationale: 'Critical path items at risk due to weather.',
      budgetMediumRiskAmount: 25000,
      budgetMediumRiskRationale: 'Contractor performance concerns.',
      budgetLowRiskAmount: 15000,
      budgetLowRiskRationale: 'Minor permit delays.',
      budgetCompletedAmount: 30000,
      budgetCompletedDescription: 'Completed sections in north quadrant.',
      totalAmount: 100000,
    } as any,
  },
};