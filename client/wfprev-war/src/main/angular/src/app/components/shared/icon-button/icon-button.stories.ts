import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { IconButtonComponent } from './icon-button.component';

const meta: Meta<IconButtonComponent> = {
  title: 'Components/Buttons/IconButton',
  component: IconButtonComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<IconButtonComponent>;

export const Default: Story = {
  args: {
    text: 'Click Me',
    icon: '/assets/complete-icon.svg',
    alt: 'Check',
    iconSize: 16,
    disabled: false,
  },
};

export const IconOnly: Story = {
  args: {
    text: '',
    icon: '/assets/complete-icon.svg',
    alt: 'Check Only',
    iconSize: 16,
    disabled: false,
  },
};

export const Disabled: Story = {
  args: {
    text: 'Disabled Button',
    icon: '/assets/complete-icon.svg',
    alt: 'Disabled',
    iconSize: 16,
    disabled: true,
  },
};
