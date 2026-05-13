import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { AddAttachmentComponent } from './add-attachment.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const meta: Meta<AddAttachmentComponent> = {
  title: 'Components/Dialogs/AddAttachment',
  component: AddAttachmentComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [BrowserAnimationsModule],
      providers: [
        {
          provide: MatDialogRef,
          useValue: { close: () => {} },
        },
        {
          provide: MAT_DIALOG_DATA,
          useValue: { indicator: 'project-files', name: 'Gross Project Area Boundary' },
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<AddAttachmentComponent>;

export const Default: Story = {};
export const ActivityFiles: Story = {
  decorators: [
    moduleMetadata({
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: { indicator: 'activity-files', name: 'Activity Polygon' },
        },
      ],
    }),
  ],
};
