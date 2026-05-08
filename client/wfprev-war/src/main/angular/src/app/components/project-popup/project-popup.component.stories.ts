import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ProjectPopupComponent } from './project-popup.component';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const mockProject = {
  projectName: 'Fuel Reduction Pilot',
  projectGuid: 'P-99',
  projectTypeCode: { projectTypeCode: 'FUEL' },
  programAreaGuid: 'PA-1',
  projectFiscals: [
    { fiscalYear: 2026, totalEstimatedAmount: 150000, planFiscalStatusCode: 'APPROVED' },
  ],
};

const meta: Meta<ProjectPopupComponent> = {
  title: 'Components/Maps/ProjectPopup',
  component: ProjectPopupComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [BrowserAnimationsModule],
      providers: [
        {
          provide: CodeTableServices,
          useValue: {
            getProjectTypeCodes: () => of([{ projectTypeCode: 'FUEL', description: 'Fuel Management' }]),
            getProgramAreaCodes: () => of([{ programAreaGuid: 'PA-1', programAreaName: 'Prevention' }]),
            getPlanFiscalStatusCodes: () => of([{ planFiscalStatusCode: 'APPROVED', description: 'Approved' }]),
            getActivityCategoryCodes: () => of([]),
          },
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<ProjectPopupComponent>;

export const Default: Story = {
  args: {
    project: mockProject,
  },
};
