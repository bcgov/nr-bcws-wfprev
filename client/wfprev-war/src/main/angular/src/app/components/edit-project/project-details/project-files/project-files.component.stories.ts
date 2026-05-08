import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ProjectFilesComponent } from './project-files.component';
import { ProjectService } from 'src/app/services/project-services';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { AttachmentService } from 'src/app/services/attachment-service';
import { SpatialService } from 'src/app/services/spatial-services';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const mockFiles = [
  {
    fileAttachmentGuid: 'F-1',
    fileName: 'boundary.zip',
    uploadedBy: 'Sam',
    uploadedByTimestamp: '2026-05-08T12:00:00Z',
    polygonHectares: 25.4,
    attachmentDescription: 'Project area map boundary',
    attachmentContentTypeCode: { attachmentContentTypeCode: 'MAP' },
  },
];

const meta: Meta<ProjectFilesComponent> = {
  title: 'Components/Data Display/ProjectFiles',
  component: ProjectFilesComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [BrowserAnimationsModule],
      providers: [
        {
          provide: ProjectService,
          useValue: {
            getProjectBoundaries: () => of({ _embedded: { projectBoundary: [] } }),
          },
        },
        {
          provide: MatSnackBar,
          useValue: { open: () => {} },
        },
        {
          provide: MatDialog,
          useValue: { open: () => ({ afterClosed: () => of(true) }) },
        },
        {
          provide: AttachmentService,
          useValue: {
            getProjectAttachments: () => of({ _embedded: { fileAttachment: mockFiles } }),
          },
        },
        {
          provide: SpatialService,
          useValue: {},
        },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({}) },
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<ProjectFilesComponent>;

export const Default: Story = {
  args: {
    projectGuid: 'P-123',
  },
};
