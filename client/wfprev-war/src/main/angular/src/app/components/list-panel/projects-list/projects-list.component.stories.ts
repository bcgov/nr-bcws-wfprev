import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { ProjectsListComponent } from './projects-list.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const meta: Meta<ProjectsListComponent> = {
  title: 'ProjectsListComponent',
  component: ProjectsListComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [ProjectsListComponent, BrowserAnimationsModule], // BrowserAnimationsModule for Material animations
    }),
  ],
};

export default meta;
type Story = StoryObj<ProjectsListComponent>;

export const Default: Story = {
  args: {
    resultCount: 3,
    projectList: [
      {
        projectTypeCode: { projectTypeCode: 'FUEL_MGMT' },
        projectNumber: 12345,
        siteUnitName: 'Vancouver Forest Unit',
        forestAreaCode: { forestAreaCode: 'WEST' },
        generalScopeCode: { generalScopeCode: 'SL_ACT' },
        programAreaGuid: '27602cd9-4b6e-9be0-e063-690a0a0afb50',
        projectName: 'Sample Forest Management Project',
        projectLead: 'Jane Smith',
        projectLeadEmailAddress: 'jane.smith@example.com',
        projectDescription: 'This is a comprehensive forest management project focusing on sustainable practices',
        closestCommunityName: 'Vancouver',
        totalFundingRequestAmount: 100000.0,
        totalAllocatedAmount: 95000.0,
        totalPlannedProjectSizeHa: 500.0,
        totalPlannedCostPerHectare: 200.0,
        totalActualAmount: 0.0,
        isMultiFiscalYearProj: false,
        forestRegionOrgUnitId: 1001,
        forestDistrictOrgUnitId: 2001,
        fireCentreOrgUnitId: 3001,
        bcParksRegionOrgUnitId: 4001,
        bcParksSectionOrgUnitId: 5001,
      },
      {
        projectTypeCode: { projectTypeCode: 'WLD_MGMT' },
        projectNumber: 67890,
        siteUnitName: 'Kelowna Wildlife Zone',
        forestAreaCode: { forestAreaCode: 'EAST' },
        generalScopeCode: { generalScopeCode: 'WL_ACT' },
        programAreaGuid: '58672bcd-3e7f-8cd1-e053-680a0a0afc40',
        projectName: 'Sustainable Fuel Management Initiative',
        projectLead: 'John Doe',
        projectLeadEmailAddress: 'john.doe@example.com',
        projectDescription: 'An initiative to promote sustainable wildlife and fuel management practices',
        closestCommunityName: 'Kelowna',
        totalFundingRequestAmount: 75000.0,
        totalAllocatedAmount: 70000.0,
        totalPlannedProjectSizeHa: 300.0,
        totalPlannedCostPerHectare: 250.0,
        totalActualAmount: 0.0,
        isMultiFiscalYearProj: true,
        forestRegionOrgUnitId: 1101,
        forestDistrictOrgUnitId: 2101,
        fireCentreOrgUnitId: 3101,
        bcParksRegionOrgUnitId: 4101,
        bcParksSectionOrgUnitId: 5101,
      },
      {
        projectTypeCode: { projectTypeCode: 'URB_FOREST' },
        projectNumber: 11223,
        siteUnitName: 'Prince George Forest Sector',
        forestAreaCode: { forestAreaCode: 'NORTH' },
        generalScopeCode: { generalScopeCode: 'UF_REV' },
        programAreaGuid: '19762acd-7d8a-4fe2-e043-680a0b0afc11',
        projectName: 'Urban Forest Revitalization Program',
        projectLead: 'Alice Brown',
        projectLeadEmailAddress: 'alice.brown@example.com',
        projectDescription: 'A program aimed at revitalizing urban forests in northern regions',
        closestCommunityName: 'Prince George',
        totalFundingRequestAmount: 120000.0,
        totalAllocatedAmount: 115000.0,
        totalPlannedProjectSizeHa: 750.0,
        totalPlannedCostPerHectare: 160.0,
        totalActualAmount: 0.0,
        isMultiFiscalYearProj: true,
        forestRegionOrgUnitId: 1201,
        forestDistrictOrgUnitId: 2201,
        fireCentreOrgUnitId: 3201,
        bcParksRegionOrgUnitId: 4201,
        bcParksSectionOrgUnitId: 5201,
      },
    ],
    fiscalYearActivityTypes: ['Clearing', 'Burning', 'Pruning'],
    sortOptions: [
      { label: 'Project Name (A-Z)', value: 'ascending' },
      { label: 'Project Name (Z-A)', value: 'descending' },
    ],
  },
};

export const WithNoProjects: Story = {
  args: {
    resultCount: 0,
    projectList: [],
    fiscalYearActivityTypes: [],
  },
};

export const WithManyProjects: Story = {
  args: {
    resultCount: 10,
    projectList: Array(10)
      .fill(null)
      .map((_, index) => ({
        projectTypeCode: { projectTypeCode: `TYPE_${index + 1}` },
        projectNumber: index + 1,
        siteUnitName: `Unit ${index + 1}`,
        forestAreaCode: { forestAreaCode: `AREA_${index + 1}` },
        generalScopeCode: { generalScopeCode: `SCOPE_${index + 1}` },
        programAreaGuid: `guid-${index + 1}`,
        projectName: `Project ${index + 1}`,
        projectLead: `Lead ${index + 1}`,
        projectLeadEmailAddress: `lead${index + 1}@example.com`,
        projectDescription: `Description for project ${index + 1}`,
        closestCommunityName: `Community ${index + 1}`,
        totalFundingRequestAmount: 100000 + index * 1000,
        totalAllocatedAmount: 90000 + index * 1000,
        totalPlannedProjectSizeHa: 500 + index * 10,
        totalPlannedCostPerHectare: 200,
        totalActualAmount: 0,
        isMultiFiscalYearProj: index % 2 === 0,
        forestRegionOrgUnitId: 1000 + index,
        forestDistrictOrgUnitId: 2000 + index,
        fireCentreOrgUnitId: 3000 + index,
        bcParksRegionOrgUnitId: 4000 + index,
        bcParksSectionOrgUnitId: 5000 + index,
      })),
    fiscalYearActivityTypes: ['Activity 1', 'Activity 2', 'Activity 3'],
  },
};
