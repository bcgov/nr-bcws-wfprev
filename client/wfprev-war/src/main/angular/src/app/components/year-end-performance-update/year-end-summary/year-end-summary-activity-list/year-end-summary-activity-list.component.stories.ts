import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { YearEndSummaryActivityListComponent } from './year-end-summary-activity-list.component';

const meta: Meta<YearEndSummaryActivityListComponent> = {
  title: 'Components/YearEnd/YearEndSummaryActivityList',
  component: YearEndSummaryActivityListComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<YearEndSummaryActivityListComponent>;

export const Empty: Story = {
  args: {
    fiscalGuid: 'fiscal-guid-123',
    activities: [],
  },
};

export const SingleActivity: Story = {
  args: {
    fiscalGuid: 'fiscal-guid-123',
    activities: [
      {
        data: {
          activityGuid: 'act-guid-1',
          activityName: 'Fuel Management Treatment',
          activityStatusCode: { activityStatusCode: 'COMPLETED' },
          isResultsReportableInd: true,
          completedAreaHa: 25,
          reportedSpendAmount: 12000,
          finalOutcomeComments: 'Completed on schedule.',
          outstandingObligationsInd: false,
          isCarryForwardInd: false,
          activityComment: '',
        },
        isExpanded: false,
      } as any,
    ],
  },
};

export const MultipleActivities: Story = {
  args: {
    fiscalGuid: 'fiscal-guid-123',
    activities: [
      {
        data: {
          activityGuid: 'act-guid-1',
          activityName: 'Fuel Management Treatment',
          activityStatusCode: { activityStatusCode: 'COMPLETED' },
          isResultsReportableInd: true,
          completedAreaHa: 25,
          reportedSpendAmount: 12000,
          finalOutcomeComments: 'Completed on schedule.',
          outstandingObligationsInd: false,
          isCarryForwardInd: false,
          activityComment: '',
        },
        isExpanded: false,
      },
      {
        data: {
          activityGuid: 'act-guid-2',
          activityName: 'Debris Disposal',
          activityStatusCode: { activityStatusCode: 'DEFERRED' },
          isResultsReportableInd: false,
          completedAreaHa: 0,
          reportedSpendAmount: 0,
          finalOutcomeComments: 'Deferred to next fiscal year.',
          outstandingObligationsInd: true,
          isCarryForwardInd: true,
          activityComment: 'Will resume in Q1 next year.',
        },
        isExpanded: false,
      },
      {
        data: {
          activityGuid: 'act-guid-3',
          activityName: 'Prescribed Burn',
          activityStatusCode: { activityStatusCode: 'CANCELLED' },
          isResultsReportableInd: false,
          completedAreaHa: 0,
          reportedSpendAmount: 0,
          finalOutcomeComments: 'Cancelled due to fire hazard.',
          outstandingObligationsInd: false,
          isCarryForwardInd: false,
          activityComment: '',
        },
        isExpanded: false,
      },
    ] as any,
  },
};