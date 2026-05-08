import { moduleMetadata, Meta, StoryObj } from '@storybook/angular';
import { AppHeaderComponent } from './app-header.component';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import { of } from 'rxjs';

const meta: Meta<AppHeaderComponent> = {
  title: 'Components/Layout/AppHeader',
  component: AppHeaderComponent,
  decorators: [
    moduleMetadata({
      imports: [
        CommonModule, // Necessary for Angular directives
        MatMenuModule,
        MatButtonModule,
        MatIconModule,
        BrowserAnimationsModule, // For Angular Material animations
      ],
      providers: [
        {
          provide: AppConfigService,
          useValue: { getConfig: () => ({ application: { environment: 'DEV', version: '1.0.0' }, rest: { trainingAndSupportLink: 'https://example.com' } }) },
        },
        {
          provide: TokenService,
          useValue: {
            getUserFullName: () => 'John Doe',
            getIdir: () => 'jdoe',
            credentialsEmitter: of({ given_name: 'John', family_name: 'Doe' }),
            authTokenEmitter: of('mock-token'),
          },
        },
      ],
    }),
  ],
  tags: ['autodocs'], // Auto-generate documentation
};

export default meta;
type Story = StoryObj<AppHeaderComponent>;

// Default story
export const Default: Story = {
  args: {
    environment: 'DEV',
    title: 'ReMi PLANNER',
    currentUser: 'User_1',
  },
};

// Custom environment story
export const ProductionEnvironment: Story = {
  args: {
    environment: 'PRODUCTION',
    title: 'ReMi PLANNER',
    currentUser: 'Admin_User',
  },
};

// Custom user story
export const CustomUser: Story = {
  args: {
    environment: 'STAGING',
    title: 'ReMi PLANNER',
    currentUser: 'Custom_User',
  },
};
