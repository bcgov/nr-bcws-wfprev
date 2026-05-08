import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, Validators } from '@angular/forms';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { SelectFieldComponent } from './select-field.component';

const meta: Meta<SelectFieldComponent> = {
  title: 'Components/Forms/SelectField',
  component: SelectFieldComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [CommonModule, ReactiveFormsModule, MatTooltipModule, NoopAnimationsModule],
    }),
  ],
};

export default meta;
type Story = StoryObj<SelectFieldComponent>;

const mockOptions = [
  { id: '1', label: 'Option One' },
  { id: '2', label: 'Option Two' },
  { id: '3', label: 'Option Three' },
];

export const Default: Story = {
  args: {
    control: new FormControl(''),
    label: 'Choose Option',
    options: mockOptions,
    optionValueField: 'id',
    optionLabelField: 'label',
    placeholder: 'Select an option',
    required: false,
    id: 'select-one',
  },
};

export const SelectedValue: Story = {
  args: {
    control: new FormControl('2'),
    label: 'Choose Option',
    options: mockOptions,
    optionValueField: 'id',
    optionLabelField: 'label',
    placeholder: 'Select an option',
    required: false,
    id: 'select-two',
  },
};

export const RequiredWithError: Story = {
  args: {
    control: new FormControl('', [Validators.required]),
    label: 'Choose Option (Required)',
    options: mockOptions,
    optionValueField: 'id',
    optionLabelField: 'label',
    placeholder: 'Select an option',
    required: true,
    id: 'select-three',
  },
};
