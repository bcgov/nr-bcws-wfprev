import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { ExpansionIndicatorComponent } from './expansion-indicator.component';

const meta: Meta<ExpansionIndicatorComponent> = {
  title: 'Components/Layout/ExpansionIndicator',
  component: ExpansionIndicatorComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<ExpansionIndicatorComponent>;

export const Collapsed: Story = {
  args: {
    isExpanded: false,
    marginRight: '8px',
  },
};

export const Expanded: Story = {
  args: {
    isExpanded: true,
    marginRight: '8px',
  },
};
