import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

const meta: Meta<ConfirmationDialogComponent> = {
  title: 'Components/Dialogs/ConfirmationDialog',
  component: ConfirmationDialogComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule, MatIconModule],
      providers: [
        {
          provide: MatDialogRef,
          useValue: { close: (val?: boolean) => {} },
        },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            indicator: 'confirm-cancel',
            title: 'Unsaved Changes',
            message: 'You have unsaved changes. Do you want to continue?',
          },
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<ConfirmationDialogComponent>;

export const Default: Story = {};

export const DeleteScenario: Story = {
  decorators: [
    moduleMetadata({
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            indicator: 'delete-project',
            title: 'Delete Project',
            message: 'Are you sure you want to delete this project?',
          },
        },
      ],
    }),
  ],
};
