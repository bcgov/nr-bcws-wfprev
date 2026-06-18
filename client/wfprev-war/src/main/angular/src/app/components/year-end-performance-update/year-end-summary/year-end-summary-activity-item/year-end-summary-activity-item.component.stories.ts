import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { YearEndSummaryActivityItemComponent } from './year-end-summary-activity-item.component';

const meta: Meta<YearEndSummaryActivityItemComponent> = {
  title: 'Components/YearEnd/YearEndSummaryActivityItem',
  component: YearEndSummaryActivityItemComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<YearEndSummaryActivityItemComponent>;

export const Default: Story = {
  args: {
    fiscalGuid: 'fiscal-guid-123',
    activity: {
      data: {
        activityGuid: 'act-guid-1',
        activityName: 'Fuel Management Treatment',
        activityStatusCode: { activityStatusCode: 'ACTIVE' },
        isResultsReportableInd: false,
        completedAreaHa: 10,
        reportedSpendAmount: 5000,
        finalOutcomeComments: 'Treatment proceeding as planned.',
        outstandingObligationsInd: false,
        isCarryForwardInd: false,
        activityComment: '',
      },
      isExpanded: false,
    } as any,
  },
};

export const Completed: Story = {
  args: {
    fiscalGuid: 'fiscal-guid-123',
    activity: {
      data: {
        activityGuid: 'act-guid-2',
        activityName: 'Spacing and Pruning',
        activityStatusCode: { activityStatusCode: 'COMPLETED' },
        isResultsReportableInd: true,
        completedAreaHa: 42,
        reportedSpendAmount: 18500,
        finalOutcomeComments: 'All treatment areas completed successfully within budget.',
        outstandingObligationsInd: false,
        isCarryForwardInd: false,
        activityComment: '',
      },
      isExpanded: false,
    } as any,
  },
};

export const Deferred: Story = {
  args: {
    fiscalGuid: 'fiscal-guid-123',
    activity: {
      data: {
        activityGuid: 'act-guid-3',
        activityName: 'Debris Disposal',
        activityStatusCode: { activityStatusCode: 'DEFERRED' },
        isResultsReportableInd: false,
        completedAreaHa: 0,
        reportedSpendAmount: 0,
        finalOutcomeComments: 'Deferred due to weather conditions. Will resume next fiscal year.',
        outstandingObligationsInd: true,
        isCarryForwardInd: true,
        activityComment: 'Work will be carried forward to next fiscal year. Contractor has been notified.',
      },
      isExpanded: false,
    } as any,
  },
};

export const Cancelled: Story = {
  args: {
    fiscalGuid: 'fiscal-guid-123',
    activity: {
      data: {
        activityGuid: 'act-guid-4',
        activityName: 'Prescribed Burn',
        activityStatusCode: { activityStatusCode: 'CANCELLED' },
        isResultsReportableInd: false,
        completedAreaHa: 0,
        reportedSpendAmount: 0,
        finalOutcomeComments: 'Cancelled due to fire hazard conditions in the area.',
        outstandingObligationsInd: false,
        isCarryForwardInd: false,
        activityComment: '',
      },
      isExpanded: false,
    } as any,
  },
};

export const SubstantiallyComplete: Story = {
  args: {
    fiscalGuid: 'fiscal-guid-123',
    activity: {
      data: {
        activityGuid: 'act-guid-5',
        activityName: 'Road Deactivation',
        activityStatusCode: { activityStatusCode: 'SUBS_COMPL' },
        isResultsReportableInd: true,
        completedAreaHa: 38,
        reportedSpendAmount: 22000,
        finalOutcomeComments: 'Substantially complete. Minor deficiencies to be addressed.',
        outstandingObligationsInd: true,
        isCarryForwardInd: false,
        activityComment: 'Outstanding obligations relate to final inspection sign-off.',
      },
      isExpanded: false,
    } as any,
  },
};

export const WithCarryForward: Story = {
  args: {
    fiscalGuid: 'fiscal-guid-123',
    activity: {
      data: {
        activityGuid: 'act-guid-6',
        activityName: 'Riparian Restoration',
        activityStatusCode: { activityStatusCode: 'ACTIVE' },
        isResultsReportableInd: true,
        completedAreaHa: 15,
        reportedSpendAmount: 8000,
        finalOutcomeComments: 'Partial completion this fiscal year.',
        outstandingObligationsInd: false,
        isCarryForwardInd: true,
        activityComment: 'Remaining 10ha to be carried forward. Budget allocation confirmed.',
      },
      isExpanded: false,
    } as any,
  },
};