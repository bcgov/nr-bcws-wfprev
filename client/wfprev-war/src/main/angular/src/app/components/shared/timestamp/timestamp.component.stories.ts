import type { Meta, StoryObj } from '@storybook/angular';
import { TimestampComponent } from './timestamp.component';

const meta: Meta<TimestampComponent> = {
  title: 'Shared/Timestamp',
  component: TimestampComponent,
  tags: ['autodocs'],
  render: (args: TimestampComponent) => ({
    props: args,
    template: `
      <div style="padding: 1rem; border: 1px dashed #ccc; max-width: 520px;">
        <wfprev-timestamp
          [updateDate]="updateDate"
          [updateUser]="updateUser">
        </wfprev-timestamp>
      </div>
    `,
  }),
  argTypes: {
    updateDate: {
      control: 'text',
      description:
        'ISO date/time string (e.g. 2025-08-07T12:34:56Z). Displayed via Angular date pipe as yyyy-MM-dd (UTC).',
    },
    updateUser: {
      control: 'text',
      description: 'User who last updated the entity.',
    },
  },
  parameters: {
    docs: {
      description: {
        component:
          'Shows “Last Updated: {yyyy-MM-dd}” and the update user when provided. The date pipe is forced to UTC in the template.',
      },
    },
    layout: 'centered',
  },
};

export default meta;
type Story = StoryObj<TimestampComponent>;

export const Both: Story = {
  name: 'Date + User',
  args: {
    updateDate: '2025-08-07T12:34:56Z',
    updateUser: 'luli',
  },
};

export const DateOnly: Story = {
  args: {
    updateDate: '2025-08-07T00:00:00Z',
  },
};

export const UserOnly: Story = {
  args: {
    updateUser: 'IDIR\\LULI',
  },
};

export const Empty: Story = {
  args: {
    updateDate: undefined,
    updateUser: undefined,
  },
  parameters: {
    docs: {
      description: {
        story:
          'With neither input, the `.timestamp` container is not rendered because of the `*ngIf` in the template.',
      },
    },
  },
};
