import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { action } from '@storybook/addon-actions';
import { DownloadButtonComponent } from './download-button.component';

export default {
  title: 'Shared/Download Button',
  component: DownloadButtonComponent,
  decorators: [
    moduleMetadata({
      imports: [DownloadButtonComponent],
    }),
  ],
  argTypes: {
    download: { action: 'download' },
    disabled: { control: 'boolean' },
  },
} as Meta<DownloadButtonComponent>;

type Story = StoryObj<DownloadButtonComponent>;

export const Default: Story = {
  args: {
    disabled: false,
    download: action('download'),
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
    download: action('download'),
  },
};
