import { Meta, StoryObj, moduleMetadata } from '@storybook/angular';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { EndorsementApprovalSectionComponent, EndorsementApprovalSectionConfig } from './endorsement-approval-section.component';

const form = new FormGroup({
  checkbox: new FormControl<boolean | null>(false),
  date: new FormControl<Date | null>(null),
  comment: new FormControl<string | null>(''),
});

const endorsementConfig: EndorsementApprovalSectionConfig = {
  checkboxLabel: 'Endorse Fiscal Activity',
  datepickerLabel: 'Endorsement Date',
  readOnlyLabel: 'Endorsed By',
  readOnlyValue: 'Smith, John',
  textareaLabel: "Endorser's Comments",
  textareaPlaceholder: 'Add supporting details for BCWS endorsement decision...',
};

const approvalConfig: EndorsementApprovalSectionConfig = {
  checkboxLabel: 'Approve Fiscal Activity',
  datepickerLabel: 'Approval Date',
  readOnlyLabel: 'Approval Entered By',
  readOnlyValue: 'Jones, Mary',
  textareaLabel: "Approver's Comments",
  textareaPlaceholder: 'Include position/group(s) responsible for Business Area approval, such as RMT, District Manager(s), etc',
};

const meta: Meta<EndorsementApprovalSectionComponent> = {
  title: 'Components/EditProject/EndorsementApproval/EndorsementApprovalSection',
  component: EndorsementApprovalSectionComponent,
  decorators: [
    moduleMetadata({
      imports: [ReactiveFormsModule],
    }),
  ],
  args: {
    form,
    checkboxKey: 'checkbox',
    dateKey: 'date',
    commentKey: 'comment',
  },
};

export default meta;
type Story = StoryObj<EndorsementApprovalSectionComponent>;

export const Endorsement: Story = {
  args: {
    config: endorsementConfig,
  },
};

export const EndorsementChecked: Story = {
  args: {
    config: endorsementConfig,
    form: new FormGroup({
      checkbox: new FormControl<boolean | null>(true),
      date: new FormControl<Date | null>(new Date()),
      comment: new FormControl<string | null>('Maintenance treatment along the north side of Sun Peaks road.'),
    }),
  },
};

export const Approval: Story = {
  args: {
    config: approvalConfig,
  },
};

export const ApprovalChecked: Story = {
  args: {
    config: approvalConfig,
    form: new FormGroup({
      checkbox: new FormControl<boolean | null>(true),
      date: new FormControl<Date | null>(new Date()),
      comment: new FormControl<string | null>('Maintenance treatment along the north side of Sun Peaks road.'),
    }),
  },
};

export const Disabled: Story = {
  args: {
    config: endorsementConfig,
    form: (() => {
      const f = new FormGroup({
        checkbox: new FormControl<boolean | null>({ value: false, disabled: true }),
        date: new FormControl<Date | null>({ value: null, disabled: true }),
        comment: new FormControl<string | null>({ value: '', disabled: true }),
      });
      return f;
    })(),
  },
};