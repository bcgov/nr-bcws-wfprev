import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ExpansionPanelComponent } from './expansion-panel.component';

const meta: Meta<ExpansionPanelComponent> = {
  title: 'Components/Layout/ExpansionPanel',
  component: ExpansionPanelComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule, MatExpansionModule, NoopAnimationsModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<ExpansionPanelComponent>;

export const Default: Story = {
  render: (args) => ({
    props: args,
    template: `
      <wfprev-expansion-panel [title]="title">
        <div style="padding: 16px; background-color: #f9f9f9; border: 1px solid #ddd; border-radius: 4px;">
          <p>This is the projected body content of the expansion panel.</p>
        </div>
      </wfprev-expansion-panel>
    `,
  }),
  args: {
    title: 'Project Information',
  },
};
