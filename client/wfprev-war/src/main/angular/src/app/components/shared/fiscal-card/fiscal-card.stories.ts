import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { StatusBadgeComponent } from 'src/app/components/shared/status-badge/status-badge.component';
import { FiscalCardComponent } from './fiscal-card.component';

const meta: Meta<FiscalCardComponent> = {
  title: 'Components/Data Display/FiscalCard',
  component: FiscalCardComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule, StatusBadgeComponent],
    }),
  ],
};

export default meta;
type Story = StoryObj<FiscalCardComponent>;

const mockGetDescription = (key: string, value: any): string | null => {
  if (key === 'planFiscalStatusCode') {
    return value === 'COMPLETE' ? 'Complete' : 'Draft';
  }
  if (key === 'activityCategoryCodes') {
    return 'Fuel Management';
  }
  return value ? String(value) : null;
};

export const Default: Story = {
  args: {
    fiscal: {
      projectFiscalName: 'Fiscal Activity 2024',
      planFiscalStatusCode: {
        planFiscalStatusCode: 'DRAFT',
      },
      activityCategoryCode: 'FUEL_MGMT',
      fiscalYear: 2024,
      fiscalPlannedSizeHa: 150,
      fiscalForecastAmount: 500000,
    },
    getDescription: mockGetDescription,
  },
};

export const CompleteState: Story = {
  args: {
    fiscal: {
      projectFiscalName: 'Completed Activity 2023',
      planFiscalStatusCode: 'COMPLETE', // To trigger the direct === 'COMPLETE' check in template line 19
      activityCategoryCode: 'COMM_PLAN',
      fiscalYear: 2023,
      fiscalCompletedSizeHa: 120,
      fiscalActualAmount: 430000,
    },
    getDescription: mockGetDescription,
  },
};
