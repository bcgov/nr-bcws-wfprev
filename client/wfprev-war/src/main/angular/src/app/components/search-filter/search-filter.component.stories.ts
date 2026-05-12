import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { SearchFilterComponent } from './search-filter.component';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { SharedService } from 'src/app/services/shared-service';
import { ProjectFilterStateService } from 'src/app/services/project-filter-state.service';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const mockTables = {
  projectTypeCode: [{ projectTypeCode: 'P1', description: 'Fuel Management' }],
  businessAreas: [{ programAreaGuid: 'B1', programAreaName: 'Prevention' }],
  activityCategoryCode: [{ activityCategoryCode: 'A1', description: 'Development' }],
  forestRegions: [{ orgUnitId: 'R1', orgUnitName: 'Cariboo' }],
  forestDistricts: [{ orgUnitId: 'D1', orgUnitName: '100 Mile House', parentOrgUnitId: 'R1' }],
  wildfireOrgUnit: [{ orgUnitIdentifier: 'O1', orgUnitName: 'Kamloops Fire Centre', wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: 'FIRE_CENTRE' } }],
  planFiscalStatusCode: [{ planFiscalStatusCode: 'S1', description: 'Approved' }]
};

const meta: Meta<SearchFilterComponent> = {
  title: 'Components/Forms/SearchFilter',
  component: SearchFilterComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [BrowserAnimationsModule],
      providers: [
        {
          provide: SharedCodeTableService,
          useValue: { codeTables$: of(mockTables) },
        },
        {
          provide: SharedService,
          useValue: {
            currentFilters: {},
            filters$: of({}),
            updateFilters: () => {},
          },
        },
        {
          provide: ProjectFilterStateService,
          useValue: {
            filters: () => ({}),
            update: () => {},
          },
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
type Story = StoryObj<SearchFilterComponent>;

export const Default: Story = {};
