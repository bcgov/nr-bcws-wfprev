import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, Validators } from '@angular/forms';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { NgxCurrency } from '@dintecom/ngx-currency';
import { InputFieldComponent } from './input-field.component';

const meta: Meta<InputFieldComponent> = {
  title: 'Components/Forms/InputField',
  component: InputFieldComponent,
  tags: ['autodocs'],
  decorators: [
    moduleMetadata({
      imports: [
        CommonModule,
        ReactiveFormsModule,
        MatTooltipModule,
        NoopAnimationsModule,
        NgxCurrency,
      ],
    }),
  ],
};

export default meta;
type Story = StoryObj<InputFieldComponent>;

export const Default: Story = {
  args: {
    control: new FormControl(''),
    label: 'Username',
    placeholder: 'Enter your username',
    id: 'username-input',
    required: false,
  },
};

export const RequiredWithError: Story = {
  args: {
    control: new FormControl('', [Validators.required]),
    label: 'Email Address',
    placeholder: 'Enter your email',
    id: 'email-input',
    required: true,
    type: 'email',
    errorMessages: { required: 'Email is required' },
  },
  parameters: {
    // Force the error to display by marking as touched
    docs: {
      story: {
        inline: true,
      },
    },
  },
};

export const CurrencyAmount: Story = {
  args: {
    control: new FormControl(15000),
    label: 'Cost Estimate',
    placeholder: 'Enter cost',
    id: 'cost-input',
    enableAmountFormat: true,
    inputPrefix: '$',
  },
};
