import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ResizablePanelComponent } from './resizable-panel.component';
import { ProjectsListComponent } from 'src/app/components/list-panel/projects-list/projects-list.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const meta: Meta<ResizablePanelComponent> = {
  title: 'ResizablePanelComponent',
  component: ResizablePanelComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [ResizablePanelComponent, ProjectsListComponent, BrowserAnimationsModule], // Import dependencies
    }),
  ],
};

export default meta;
type Story = StoryObj<ResizablePanelComponent>;

export const Default: Story = {
  args: {
    panelWidth: '50vw', // Default width
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

export const WithMultipleTabs: Story = {
  args: {
    panelWidth: '70vw', // Custom width for the panel
    selectedTabIndex: 0, // Start with the first tab selected
  },
};
