import { Meta, StoryObj } from '@storybook/angular';
import { StatusBadgeComponent } from './status-badge.component';

const meta: Meta<StatusBadgeComponent> = {
  title: 'Components/Shared/StatusBadge',
  component: StatusBadgeComponent,
  tags: ['autodocs'],
};

export default meta;
    icon: {
      src: 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Blue_check.svg/2048px-Blue_check.svg.png',
      alt: 'Verified',
      title: 'Verified Badge',
    },
    label: 'Verified',
  },
};

export const WithoutIcon: StoryObj<StatusBadgeComponent> = {
  args: {
    icon: null,
    label: 'Draft',
  },
};
