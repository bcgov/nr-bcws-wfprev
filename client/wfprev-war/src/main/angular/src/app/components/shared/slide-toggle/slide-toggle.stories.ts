import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { SlideToggleComponent } from './slide-toggle.component';

const meta: Meta<SlideToggleComponent> = {
  title: 'Components/Forms/SlideToggle',
  component: SlideToggleComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule, MatSlideToggleModule, NoopAnimationsModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<SlideToggleComponent>;

export const Default: Story = {
  args: {
    label: 'Enable Feature',
    checked: false,
  },
};

export const Checked: Story = {
  args: {
    label: 'Feature Active',
    checked: true,
  },
};
