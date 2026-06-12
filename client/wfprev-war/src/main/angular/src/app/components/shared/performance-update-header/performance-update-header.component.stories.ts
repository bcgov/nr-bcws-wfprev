import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { PerformanceUpdateHeaderComponent } from './performance-update-header.component';
import { ProgressStatus, ReportingPeriod, UpdateGeneralStatus } from '../../models';

const meta: Meta<PerformanceUpdateHeaderComponent> = {
  title: 'Components/Shared/PerformanceUpdateHeader',
  component: PerformanceUpdateHeaderComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<PerformanceUpdateHeaderComponent>;

const baseUpdate = {
  submittedTimestamp: new Date('2026-06-11T00:00:00Z'),
  reportingPeriod: ReportingPeriod.Q2,
  progressStatusCode: ProgressStatus.OnTrack,
  updateGeneralStatus: UpdateGeneralStatus.Draft,
  submittedByUserid: '',
  submittedByGuid: '',
  generalUpdateComment: '',
  submittedBy: 'Test User',
  forecastAmount: 10000,
  forecastAdjustmentAmount: 0,
  previousForecastAmount: 0,
  forecastAdjustmentRationale: '',
  budgetHighRiskAmount: 0,
  budgetHighRiskRationale: '',
  budgetMediumRiskAmount: 0,
  budgetMediumRiskRationale: '',
  budgetLowRiskAmount: 0,
  budgetLowRiskRationale: '',
  budgetCompletedAmount: 0,
  budgetCompletedDescription: '',
  totalAmount: 0,
  isCarryForwardInd: false,
  outstandingObligationsInd: false,
};

export const Default: Story = {
  args: {
    update: baseUpdate,
    isExpanded: false,
  },
};

export const Expanded: Story = {
  args: {
    update: baseUpdate,
    isExpanded: true,
  },
};

export const Delayed: Story = {
  args: {
    update: {
      ...baseUpdate,
      progressStatusCode: ProgressStatus.Delayed,
      updateGeneralStatus: UpdateGeneralStatus.InProgress,
    },
    isExpanded: false,
  },
};

export const ForecastIncreased: Story = {
  args: {
    update: {
      ...baseUpdate,
      forecastAdjustmentAmount: 5000,
      updateGeneralStatus: UpdateGeneralStatus.InProgress,
    },
    isExpanded: false,
  },
};

export const ForecastDecreased: Story = {
  args: {
    update: {
      ...baseUpdate,
      forecastAdjustmentAmount: -5000,
      updateGeneralStatus: UpdateGeneralStatus.InProgress,
    },
    isExpanded: false,
  },
};

export const CarryForwardAndObligations: Story = {
  args: {
    update: {
      ...baseUpdate,
      isCarryForwardInd: true,
      outstandingObligationsInd: true,
    },
    isExpanded: false,
  },
};

export const AllBadges: Story = {
  args: {
    update: {
      ...baseUpdate,
      progressStatusCode: ProgressStatus.OnTrack,
      forecastAdjustmentAmount: 5000,
      isCarryForwardInd: true,
      outstandingObligationsInd: true,
      updateGeneralStatus: UpdateGeneralStatus.Complete,
    },
    isExpanded: false,
  },
};

export const Complete: Story = {
  args: {
    update: {
      ...baseUpdate,
      updateGeneralStatus: UpdateGeneralStatus.Complete,
    },
    isExpanded: false,
  },
};

export const Cancelled: Story = {
  args: {
    update: {
      ...baseUpdate,
      progressStatusCode: ProgressStatus.Cancelled,
      updateGeneralStatus: UpdateGeneralStatus.Cancelled,
    },
    isExpanded: false,
  },
};

export const Deferred: Story = {
  args: {
    update: {
      ...baseUpdate,
      progressStatusCode: ProgressStatus.Deferred,
      updateGeneralStatus: UpdateGeneralStatus.Draft,
    },
    isExpanded: false,
  },
};

export const March7Reporting: Story = {
  args: {
    update: {
      ...baseUpdate,
      reportingPeriod: ReportingPeriod.March7,
    },
    isExpanded: false,
  },
};

export const EndOfQ3: Story = {
  args: {
    update: {
      ...baseUpdate,
      reportingPeriod: ReportingPeriod.Q3,
    },
    isExpanded: false,
  },
};