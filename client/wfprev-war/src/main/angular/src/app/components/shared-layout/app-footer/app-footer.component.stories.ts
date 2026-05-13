import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { AppFooterComponent } from './app-footer.component';

const meta: Meta<AppFooterComponent> = {
  title: 'Components/Layout/AppFooter',
  component: AppFooterComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [AppFooterComponent],
    }),
  ],
};

export default meta;
type Story = StoryObj<AppFooterComponent>;

export const Default: Story = {
  args: {},
};
