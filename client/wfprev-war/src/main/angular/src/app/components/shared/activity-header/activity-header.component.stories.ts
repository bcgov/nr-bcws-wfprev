import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { ActivityHeaderComponent } from './activity-header.component';

const meta: Meta<ActivityHeaderComponent> = {
  title: 'Components/Shared/ActivityHeader',
  component: ActivityHeaderComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<ActivityHeaderComponent>;

export const Default: Story = {
  args: {
    title: 'Fuel Management Treatment',
    isExpanded: false,
    isResultsReportable: false,
    hasOutstandingObligations: false,
    isCarryForward: false,
    isSpatialAdded: undefined,
    statusCode: '',
    backgroundColor: '',
  },
};

export const ExpandedWithAllIndicators: Story = {
  args: {
    title: 'Spacing and Pruning Activity',
    isExpanded: true,
    isResultsReportable: true,
    hasOutstandingObligations: true,
    isCarryForward: true,
    isSpatialAdded: true,
    statusCode: 'COMPLETED',
  },
};

export const CancelledNoSpatial: Story = {
  args: {
    title: 'Debris Disposal Activity',
    isExpanded: false,
    isResultsReportable: false,
    hasOutstandingObligations: false,
    isCarryForward: false,
    isSpatialAdded: false,
    statusCode: 'CANCELLED',
  },
};

export const DeferredAndCarryForward: Story = {
  args: {
    title: 'Deferred Fuel Treatment',
    isExpanded: false,
    isResultsReportable: true,
    hasOutstandingObligations: false,
    isCarryForward: true,
    isSpatialAdded: true,
    statusCode: 'DEFERRED',
  },
};

export const YearEndBackground: Story = {
  args: {
    title: 'Year End Update Activity',
    isExpanded: false,
    isResultsReportable: true,
    hasOutstandingObligations: true,
    isCarryForward: true,
    isSpatialAdded: true,
    statusCode: 'COMPLETED',
    backgroundColor: '#FAF9F8',
  },
};
