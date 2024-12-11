import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { EditProjectComponent } from './edit-project.component';
import { MatTabsModule } from '@angular/material/tabs';
import { ProjectDetailsComponent } from 'src/app/components/edit-project/project-details/project-details.component';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const meta: Meta<EditProjectComponent> = {
  title: 'EditProjectComponent',
  component: EditProjectComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [EditProjectComponent, MatTabsModule, ProjectDetailsComponent,BrowserAnimationsModule ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of({
              get: (key: string) => (key === 'name' ? 'Sample Project' : null),
            }),
          },
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<EditProjectComponent>;

export const Default: Story = {
  args: {},
};

export const WithCustomProjectName: Story = {
  decorators: [
    moduleMetadata({
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of({
              get: (key: string) => (key === 'name' ? 'Custom Project' : null),
            }),
          },
        },
      ],
    }),
  ],
  args: {},
};
