import { Meta, StoryObj } from '@storybook/angular';
import { StatusBadgeComponent } from './status-badge.component';

const meta: Meta<StatusBadgeComponent> = {
  title: 'Components/Indicators/StatusBadge',
  component: StatusBadgeComponent,
  tags: ['autodocs'],
};

export default meta;

export const Default: StoryObj<StatusBadgeComponent> = {
  args: {
    icon: {
      src: '/assets/complete-icon.svg',
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
