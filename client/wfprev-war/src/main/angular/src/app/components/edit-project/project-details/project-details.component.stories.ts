import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ProjectDetailsComponent } from './project-details.component';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { CommonModule } from '@angular/common';

const fb = new FormBuilder();

const meta: Meta<ProjectDetailsComponent> = {
  title: 'ProjectDetailsComponent',
  component: ProjectDetailsComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [ProjectDetailsComponent, ReactiveFormsModule, MatExpansionModule, CommonModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<ProjectDetailsComponent>;

export const Default: Story = {
  args: {
    detailsForm: fb.group({
      projectLead: ['', [Validators.required]],
      projectLeadEmailAddress: ['', [Validators.required, Validators.email]],
      projectTypeCode: ['', [Validators.required]],
      businessArea: ['', [Validators.required]],
      forestRegion: ['', [Validators.required]],
      forestDistrict: ['', []],
      bcParksRegion: ['', []],
      bcParksDistrict: ['', []],
      siteUnitName: ['', [Validators.required]],
      closestCommunityName: ['', []],
      fundingStream: ['', []],
      totalFundingRequestAmount: [0, [Validators.required, Validators.min(0)]],
      totalAllocatedAmount: [0, [Validators.required, Validators.min(0)]],
      projectDescription: ['', []],
      coordinats: ['', []],
    }),
  },
};

export const WithPrepopulatedForm: Story = {
  args: {
    detailsForm: fb.group({
      projectLead: ['Jane Smith', [Validators.required]],
      projectLeadEmailAddress: ['jane.smith@example.com', [Validators.required, Validators.email]],
      projectTypeCode: ['FUEL_MGMT', [Validators.required]],
      businessArea: ['Area1', [Validators.required]],
      forestRegion: ['Region1', [Validators.required]],
      forestDistrict: ['District1', []],
      bcParksRegion: ['Region1', []],
      bcParksDistrict: ['District1', []],
      siteUnitName: ['Vancouver Forest Unit', [Validators.required]],
      closestCommunityName: ['Vancouver', []],
      fundingStream: ['Stream1', []],
      totalFundingRequestAmount: [100000, [Validators.required, Validators.min(0)]],
      totalAllocatedAmount: [95000, [Validators.required, Validators.min(0)]],
      projectDescription: ['This is a sample project.', []],
      coordinats: ['48.407326,-123.329773', []],
    }),
  },
};

export const FormWithErrors: Story = {
  args: {
    detailsForm: fb.group({
      projectLead: ['', [Validators.required]], // Missing required field
      projectLeadEmailAddress: ['invalid-email', [Validators.required, Validators.email]],
      projectTypeCode: ['', [Validators.required]], // Missing required field
      businessArea: ['', [Validators.required]], // Missing required field
      forestRegion: ['', [Validators.required]], // Missing required field
      forestDistrict: ['', []],
      bcParksRegion: ['', []],
      bcParksDistrict: ['', []],
      siteUnitName: ['', [Validators.required]], // Missing required field
      closestCommunityName: ['', []],
      fundingStream: ['', []],
      totalFundingRequestAmount: [-100, [Validators.required, Validators.min(0)]], // Invalid value
      totalAllocatedAmount: [-50, [Validators.required, Validators.min(0)]], // Invalid value
      projectDescription: ['', []],
      coordinats: ['', []],
    }),
  },
};
