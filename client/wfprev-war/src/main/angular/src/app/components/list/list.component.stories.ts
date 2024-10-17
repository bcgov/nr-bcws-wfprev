import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ListComponent } from './list.component';
import { RouterTestingModule } from '@angular/router/testing';

const meta: Meta<ListComponent> = {
  title: 'ListComponent',
  component: ListComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [
        ListComponent,
        RouterTestingModule
      ],
    })
  ],
};

export default meta;
type Story = StoryObj<ListComponent>;

export const Default: Story = {
  args: {
  },
};