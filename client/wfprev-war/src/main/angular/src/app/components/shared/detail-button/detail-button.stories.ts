import { action } from 'storybook/actions';
import { Meta, moduleMetadata, StoryObj } from '@storybook/angular';
import { DetailButtonComponent } from './detail-button.component';

export default {
  argTypes: {
    text: { control: 'text' },
    isDisabled: { control: 'boolean' },
    clicked: { action: 'clicked' },
  },
  component: DetailButtonComponent,
  decorators: [
    moduleMetadata({
      imports: [DetailButtonComponent],
    }),
  ],
  title: 'Components/Buttons/Detail Button',
} as Meta<DetailButtonComponent>;

type Story = StoryObj<DetailButtonComponent>;

export const Default: Story = {
  args: {
    clicked: action('clicked'),
    isDisabled: false,
    text: 'View Details',
  },
};

export const CustomText: Story = {
  args: {
    clicked: action('clicked'),
    isDisabled: false,
    text: 'Edit Details',
  },
};

export const Disabled: Story = {
  args: {
    clicked: action('clicked'),
    isDisabled: true,
    text: 'View Details',
  },
};
