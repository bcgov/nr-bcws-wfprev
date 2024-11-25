import { moduleMetadata, Meta, StoryObj } from '@storybook/angular';
import { AppHeaderComponent } from './app-header.component';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';

const meta: Meta<AppHeaderComponent> = {
  title: 'AppHeader', // Storybook title
  component: AppHeaderComponent,
  decorators: [
    moduleMetadata({
      imports: [
        CommonModule, // Necessary for Angular directives
        MatMenuModule,
        MatButtonModule,
        MatIconModule,
        BrowserAnimationsModule, // For Angular Material animations
        AppHeaderComponent, // Import the standalone component
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
    title: 'PREVENTION',
    currentUser: 'User_1',
  },
};

// Custom environment story
export const ProductionEnvironment: Story = {
  args: {
    environment: 'PRODUCTION',
    title: 'PREVENTION',
    currentUser: 'Admin_User',
  },
};

// Custom user story
export const CustomUser: Story = {
  args: {
    environment: 'STAGING',
    title: 'PREVENTION',
    currentUser: 'Custom_User',
  },
};
