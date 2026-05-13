import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ErrorPageComponent } from './error-page.component';
import { AppConfigService } from 'src/app/services/app-config.service';

const meta: Meta<ErrorPageComponent> = {
  title: 'Components/Errors/ErrorPage',
  component: ErrorPageComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      providers: [
        {
          provide: AppConfigService,
          useValue: { getConfig: () => ({ application: { remiPlannerEmailAddress: 'admin@example.com' } }) },
        },
      ],
    })
  ],
};

export default meta;
type Story = StoryObj<ErrorPageComponent>;

export const Default: Story = {
  args: {},
};
  