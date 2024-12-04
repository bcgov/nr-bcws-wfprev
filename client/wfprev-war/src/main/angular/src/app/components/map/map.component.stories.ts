import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { MapComponent } from './map.component';
import { ResizablePanelComponent } from 'src/app/components/resizable-panel/resizable-panel.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const meta: Meta<MapComponent> = {
  title: 'MapComponent',
  component: MapComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [MapComponent, ResizablePanelComponent, BrowserAnimationsModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<MapComponent>;

export const Default: Story = {
  args: {
    panelContent: `
      The goal of the BC Wildfire Service (BCWS) Prevention Program is to reduce the negative impacts of wildfire on public safety, property, the environment and the economy using the seven disciplines of the FireSmart program.
      <br>
      British Columbia is experiencing a serious and sustained increase in extreme wildfire behaviour and fire events particularly in the wildland-urban interface.
    `,
  },
};

export const WithAdditionalContent: Story = {
  args: {
    panelContent: `
      This is additional content for testing dynamic panel updates.
      <br>
      Panel resizing should update the map layout dynamically.
    `,
  },
};
