import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { PerformanceUpdateModalWindowComponent } from './wfprev-performance-update-modal-window.component';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ProjectService } from 'src/app/services/project-services';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const mockData = {
  currentForecast: 10000,
  projectGuid: 'P-1',
  fiscalGuid: 'F-1',
  reportingPeriod: [
    { label: 'Q1', value: 'Q1' },
    { label: 'Q2', value: 'Q2' },
  ],
  progressStatus: [
    { label: 'On Track', value: 'OnTrack' },
    { label: 'At Risk', value: 'AtRisk' },
  ],
};

const meta: Meta<PerformanceUpdateModalWindowComponent> = {
  title: 'Components/Dialogs/PerformanceUpdateModalWindow',
  component: PerformanceUpdateModalWindowComponent,
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
          provide: MatDialog,
          useValue: { open: () => ({ afterClosed: () => of(true) }) },
        },
        {
          provide: ProjectService,
          useValue: {
            savePerformanceUpdates: () => of({}),
          },
        },
        {
          provide: MatSnackBar,
          useValue: { open: () => {} },
        },
        {
          provide: MAT_DIALOG_DATA,
          useValue: mockData,
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<PerformanceUpdateModalWindowComponent>;

export const Default: Story = {};
