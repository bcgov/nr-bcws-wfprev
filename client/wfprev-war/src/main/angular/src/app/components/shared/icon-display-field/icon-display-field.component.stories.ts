import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { IconDisplayFieldComponent } from './icon-display-field.component';

const meta: Meta<IconDisplayFieldComponent> = {
  title: 'Components/Shared/IconDisplayField',
  component: IconDisplayFieldComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule, MatIconModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<IconDisplayFieldComponent>;

export const MaterialIcon: Story = {
  args: {
    label: 'Total Cost',
    value: '$150,000.00',
    matIcon: 'attach_money',
  },
};

export const ImageIcon: Story = {
  args: {
    label: 'Completed Status',
    value: 'Complete',
    imageIcon: '/assets/complete-icon.svg',
  },
};

export const NumberValue: Story = {
  args: {
    label: 'Planned Hectares',
    value: 120.5,
    matIcon: 'landscape',
  },
};
