import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { SpatialViewerDialogComponent } from './spatial-viewer-dialog.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ProjectFile } from '../models';

const mockFileWithGeometry: ProjectFile = {
  fileName: 'test-spatial-file.zip',
  documentPath: 'test-spatial-file.zip',
  boundaryGeometry: {
    type: 'MultiPolygon',
    coordinates: [
      [
        [
          [-127.6476, 53.7267],
          [-127.6576, 53.7267],
          [-127.6576, 53.7367],
          [-127.6476, 53.7367],
          [-127.6476, 53.7267]
        ]
      ]
    ]
  }
};

const mockFileWithoutGeometry: ProjectFile = {
  fileName: 'missing-geometry.zip',
  documentPath: 'missing-geometry.zip',
};

const mockFileWithOnlyDocumentPath: ProjectFile = {
  documentPath: 'only-document-path.zip',
  boundaryGeometry: {
    type: 'MultiPolygon',
    coordinates: [
      [
        [
          [-120.6408, 50.5343],
          [-120.6508, 50.5343],
          [-120.6508, 50.5443],
          [-120.6408, 50.5443],
          [-120.6408, 50.5343]
        ]
      ]
    ]
  }
};

const meta: Meta<SpatialViewerDialogComponent> = {
  title: 'Components/Dialogs/SpatialViewerDialog',
  component: SpatialViewerDialogComponent,
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
          useValue: { file: mockFileWithGeometry },
        },
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<SpatialViewerDialogComponent>;

export const WithGeometry: Story = {};

export const WithoutGeometryWarning: Story = {
  decorators: [
    moduleMetadata({
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            file: mockFileWithoutGeometry,
          },
        },
      ],
    }),
  ],
};

export const FallbackToDocumentPath: Story = {
  decorators: [
    moduleMetadata({
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            file: mockFileWithOnlyDocumentPath,
          },
        },
      ],
    }),
  ],
};
