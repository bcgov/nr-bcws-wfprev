import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { action } from '@storybook/addon-actions';
import { DetailButtonComponent } from './detail-button.component';

export default {
  title: 'Shared/Detail Button',
  component: DetailButtonComponent,
  decorators: [
    moduleMetadata({
      imports: [DetailButtonComponent],
    }),
  ],
  argTypes: {
    text: { control: 'text' },
    disabled: { control: 'boolean' },
    clicked: { action: 'clicked' },
  },
} as Meta<DetailButtonComponent>;

type Story = StoryObj<DetailButtonComponent>;

export const Default: Story = {
  args: {
    text: 'View Details',
    disabled: false,
    clicked: action('clicked'),
  },
};

export const CustomText: Story = {
  args: {
    text: 'Edit Details',
    disabled: false,
    clicked: action('clicked'),
  },
};

export const Disabled: Story = {
  args: {
    text: 'View Details',
    disabled: true,
    clicked: action('clicked'),
  },
};
