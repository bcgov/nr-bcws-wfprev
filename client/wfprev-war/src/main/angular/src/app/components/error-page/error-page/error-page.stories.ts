import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ErrorPageComponent } from './error-page.component';

const meta: Meta<ErrorPageComponent> = {
  title: 'ErrorPageComponent',
  component: ErrorPageComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [ErrorPageComponent],
    })
  ],
};

export default meta;
type Story = StoryObj<ErrorPageComponent>;

export const Default: Story = {
    args: {
      panelContent: `
       You do not have sufficient permissions to access this application
      `,
    },
  };
  