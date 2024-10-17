import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ResizablePanelComponent } from './resizable-panel.component';

const meta: Meta<ResizablePanelComponent> = {
  title: 'ResizablePanelComponent',
  component: ResizablePanelComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [ResizablePanelComponent],
    })
  ],
};

export default meta;
type Story = StoryObj<ResizablePanelComponent>;

export const Default: Story = {
  args: {
    panelWidth: '50vw',  // Set default width for story
  },
};

export const ResizedTo90Percent: Story = {
  args: {
    panelWidth: '90vw',
  },
};

export const ResizedTo5Percent: Story = {
  args: {
    panelWidth: '5vw',
  },
};
