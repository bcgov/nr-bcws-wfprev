import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { DetailedErrorMessageComponent } from './detailed-error-message.component';
import { MAT_SNACK_BAR_DATA, MatSnackBarRef } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';

const meta: Meta<DetailedErrorMessageComponent> = {
  title: 'Components/Errors/DetailedErrorMessage',
  component: DetailedErrorMessageComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule],
      providers: [
        {
          provide: MatSnackBarRef,
          useValue: { dismiss: () => {} },
        },
        {
          provide: MAT_SNACK_BAR_DATA,
          useValue: {
            message: 'App initialization failed',
            detailedError: 'HttpStatus 500: Internal Server Error at loadAppConfig',
          },
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<DetailedErrorMessageComponent>;

export const Default: Story = {};
