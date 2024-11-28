import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { CreateNewProjectDialogComponent } from './create-new-project-dialog.component';
import { MatDialogModule } from '@angular/material/dialog';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

const fb = new FormBuilder();

const meta: Meta<CreateNewProjectDialogComponent> = {
  title: 'CreateNewProjectDialogComponent',
  component: CreateNewProjectDialogComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CreateNewProjectDialogComponent, MatDialogModule, ReactiveFormsModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<CreateNewProjectDialogComponent>;

export const Default: Story = {
  args: {
    projectForm: fb.group({
      projectName: ['', [Validators.required, Validators.maxLength(50)]],
      latLong: ['', [Validators.maxLength(25)]],
      businessArea: ['', [Validators.required]],
      forestRegion: ['', [Validators.required]],
      forestDistrict: ['', [Validators.required]],
      bcParksRegion: ['', [Validators.required]],
      bcParksSection: ['', [Validators.required]],
      projectLead: ['', [Validators.maxLength(50)]],
      projectLeadEmail: ['', [Validators.email, Validators.maxLength(50)]],
      siteUnitName: ['', [Validators.maxLength(50)]],
      closestCommunity: ['', [Validators.required, Validators.maxLength(50)]],
    }),
  },
};

export const WithPrepopulatedForm: Story = {
  args: {
    projectForm: fb.group({
      projectName: ['Test Project', [Validators.required, Validators.maxLength(50)]],
      latLong: ['123.456', [Validators.maxLength(25)]],
      businessArea: ['Area 1', [Validators.required]],
      forestRegion: ['Region 1', [Validators.required]],
      forestDistrict: ['District 1', [Validators.required]],
      bcParksRegion: ['Northern', [Validators.required]],
      bcParksSection: ['Omineca', [Validators.required]],
      projectLead: ['John Doe', [Validators.maxLength(50)]],
      projectLeadEmail: ['john.doe@example.com', [Validators.email, Validators.maxLength(50)]],
      siteUnitName: ['Unit 1', [Validators.maxLength(50)]],
      closestCommunity: ['Community 1', [Validators.required, Validators.maxLength(50)]],
    }),
  },
};

export const FormWithErrors: Story = {
  args: {
    projectForm: fb.group({
      projectName: ['', [Validators.required, Validators.maxLength(50)]], // Missing required field
      latLong: ['', [Validators.maxLength(25)]],
      businessArea: ['', [Validators.required]],
      forestRegion: ['', [Validators.required]],
      forestDistrict: ['', [Validators.required]],
      bcParksRegion: ['', [Validators.required]],
      bcParksSection: ['', [Validators.required]],
      projectLead: ['', [Validators.maxLength(50)]],
      projectLeadEmail: ['invalid-email', [Validators.email, Validators.maxLength(50)]],
      siteUnitName: ['', [Validators.maxLength(50)]],
      closestCommunity: ['', [Validators.required, Validators.maxLength(50)]],
    }),
  },
};
