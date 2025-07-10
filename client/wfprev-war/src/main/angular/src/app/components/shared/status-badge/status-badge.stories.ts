import { Meta, StoryObj } from '@storybook/angular';
import { StatusBadgeComponent } from './status-badge.component';

const meta: Meta<StatusBadgeComponent> = {
  title: 'Components/StatusBadge',
  component: StatusBadgeComponent,
  tags: ['autodocs'],
  argTypes: {
    icon: {
      control: 'object',
      description: 'Optional icon object with src, alt, title',
    },
    label: {
      control: 'text',
      description: 'Text label displayed in the badge',
    },
  },
};
export default meta;

export const WithIcon: StoryObj<StatusBadgeComponent> = {
  args: {
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
