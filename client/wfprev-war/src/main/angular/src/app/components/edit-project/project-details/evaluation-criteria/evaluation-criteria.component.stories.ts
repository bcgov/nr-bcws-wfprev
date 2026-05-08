import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { EvaluationCriteriaComponent } from './evaluation-criteria.component';
import { MatDialog } from '@angular/material/dialog';
import { ProjectService } from 'src/app/services/project-services';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const mockProject = {
  projectGuid: 'P-123',
  projectTypeCode: { projectTypeCode: 'FUEL_MANAGEMENT' },
};

const meta: Meta<EvaluationCriteriaComponent> = {
  title: 'Components/Data Display/EvaluationCriteria',
  component: EvaluationCriteriaComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [BrowserAnimationsModule],
      providers: [
        {
          provide: MatDialog,
          useValue: { open: () => ({ afterClosed: () => of(true) }) },
        },
        {
          provide: ProjectService,
          useValue: {
            getEvaluationCriteriaSummaries: () => of({ _embedded: { eval_criteria_summary: [] } }),
          },
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<EvaluationCriteriaComponent>;

export const Default: Story = {
  args: {
    project: mockProject as any,
  },
};
