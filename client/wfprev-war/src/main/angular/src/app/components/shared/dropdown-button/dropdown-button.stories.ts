import { Meta, moduleMetadata, StoryObj } from '@storybook/angular';
import { DropdownButtonComponent } from './dropdown-button.component';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FiscalStatuses } from 'src/app/utils/constants';

const meta: Meta<DropdownButtonComponent> = {
  title: 'Components/DropdownButton',
  component: DropdownButtonComponent,
  decorators: [
    moduleMetadata({
      imports: [
        MatMenuModule,
        MatButtonModule,
        BrowserAnimationsModule,
      ],
    }),
  ],
  argTypes: {
    status: {
      control: {
        type: 'select',
        options: Object.values(FiscalStatuses),
      },
    },
    isApproved: { control: 'boolean' },
    index: { control: 'number' },
    actionSelected: { action: 'actionSelected' },
  },
};
export default meta;

export const Draft: StoryObj<DropdownButtonComponent> = {
  args: {
    status: FiscalStatuses.DRAFT,
    isApproved: false,
    index: 0,
  },
};

export const Proposed: StoryObj<DropdownButtonComponent> = {
  args: {
    status: FiscalStatuses.PROPOSED,
    isApproved: false,
    index: 1,
  },
};

export const Prepared: StoryObj<DropdownButtonComponent> = {
  args: {
    status: FiscalStatuses.PREPARED,
    isApproved: false,
    index: 2,
  },
};

export const InProgress: StoryObj<DropdownButtonComponent> = {
  args: {
    status: FiscalStatuses.IN_PROGRESS,
    isApproved: false,
    index: 3,
  },
};

export const CompleteDisabled: StoryObj<DropdownButtonComponent> = {
  args: {
    status: FiscalStatuses.COMPLETE,
    isApproved: true,
    index: 4,
  },
};
