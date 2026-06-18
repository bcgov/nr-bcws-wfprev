import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { of } from 'rxjs';
import { YearEndSummaryComponent } from './year-end-summary.component';
import { ProjectService } from 'src/app/services/project-services';

const makeProjectServiceMock = (overrides: any = {}) => ({
  getAllFiscalCloseouts: () => of({
    _embedded: {
      fiscalCloseouts: [{
        outcomeComment: overrides.outcomeComment ?? 'Successful completion of all planned treatments.',
      }]
    }
  }),
  getProjectFiscalByProjectPlanFiscalGuid: () => of({
    fiscalReportedSpendAmount: overrides.fiscalReportedSpendAmount ?? 45000,
    fiscalActualAmount: overrides.fiscalActualAmount ?? 43000,
    fiscalCompletedSizeHa: overrides.fiscalCompletedSizeHa ?? 38,
  }),
  getFiscalActivities: () => of({
    _embedded: {
      activities: overrides.activities ?? [
        {
          activityGuid: 'act-guid-1',
          activityName: 'Fuel Management Treatment',
          activityStatusCode: { activityStatusCode: 'COMPLETED' },
          isResultsReportableInd: true,
          completedAreaHa: 25,
          reportedSpendAmount: 25000,
          finalOutcomeComments: 'Completed on schedule.',
          outstandingObligationsInd: false,
          isCarryForwardInd: false,
          activityComment: '',
        },
        {
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
      ]
    }
  }),
});

const meta: Meta<YearEndSummaryComponent> = {
  title: 'Components/YearEnd/YearEndSummary',
  component: YearEndSummaryComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
      providers: [
        { provide: ProjectService, useValue: makeProjectServiceMock() }
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<YearEndSummaryComponent>;

export const Default: Story = {
  args: {
    projectGuid: 'proj-guid-123',
    fiscalGuid: 'fiscal-guid-123',
  },
};

export const NoActivities: Story = {
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
      providers: [
        { provide: ProjectService, useValue: makeProjectServiceMock({ activities: [] }) }
      ],
    }),
  ],
  args: {
    projectGuid: 'proj-guid-123',
    fiscalGuid: 'fiscal-guid-123',
  },
};

export const HighSpend: Story = {
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
      providers: [
        {
          provide: ProjectService, useValue: makeProjectServiceMock({
            fiscalReportedSpendAmount: 250000,
            fiscalActualAmount: 248000,
            fiscalCompletedSizeHa: 120,
            outcomeComment: 'Large scale treatment completed across multiple zones.',
          })
        }
      ],
    }),
  ],
  args: {
    projectGuid: 'proj-guid-123',
    fiscalGuid: 'fiscal-guid-123',
  },
};

export const MissingGuids: Story = {
  args: {
    projectGuid: '',
    fiscalGuid: '',
  },
};