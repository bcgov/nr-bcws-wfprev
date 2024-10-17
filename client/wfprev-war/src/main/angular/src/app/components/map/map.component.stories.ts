import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { MapComponent } from './map.component';
import * as L from 'leaflet';

const meta: Meta<MapComponent> = {
  title: 'MapComponent',
  component: MapComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [MapComponent],
    })
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
