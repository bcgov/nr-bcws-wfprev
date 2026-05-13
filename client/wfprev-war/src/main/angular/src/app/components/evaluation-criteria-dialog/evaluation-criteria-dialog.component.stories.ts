import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { EvaluationCriteriaDialogComponent } from './evaluation-criteria-dialog.component';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const mockProject = {
  projectGuid: 'P-123',
  projectTypeCode: { projectTypeCode: 'FUEL_MANAGEMENT' },
};

const meta: Meta<EvaluationCriteriaDialogComponent> = {
  title: 'Components/Dialogs/EvaluationCriteriaDialog',
  component: EvaluationCriteriaDialogComponent,
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
          provide: CodeTableServices,
          useValue: {
            fetchCodeTable: (name: string) => of({ _embedded: { evaluationCriteriaCode: [], wuiRiskClassRank: [] } }),
          },
        },
        {
          provide: ProjectService,
          useValue: {
            createEvaluationCriteriaSummary: () => of({}),
            updateEvaluationCriteriaSummary: () => of({}),
          },
        },
        {
          provide: MatSnackBar,
          useValue: { open: () => {} },
        },
        {
          provide: MAT_DIALOG_DATA,
          useValue: { project: mockProject },
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<EvaluationCriteriaDialogComponent>;

export const Default: Story = {};
export const PrescribedFire: Story = {
  decorators: [
    moduleMetadata({
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            project: {
              projectGuid: 'P-123',
              projectTypeCode: { projectTypeCode: 'CULTURAL_PRESCRIBED_FIRE' },
            },
          },
        },
      ],
    }),
  ],
};
