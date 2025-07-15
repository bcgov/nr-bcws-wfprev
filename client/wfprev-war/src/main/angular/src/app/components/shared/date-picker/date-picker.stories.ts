import { Meta, moduleMetadata } from '@storybook/angular';
import { DatePickerComponent } from 'src/app/components/shared/date-picker/date-picker.component';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

export default {
  title: 'Components/DatePicker',
  component: DatePickerComponent,
  decorators: [
    moduleMetadata({
      imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatDatepickerModule,
        MatNativeDateModule,
        DatePickerComponent,
      ],
    }),
  ],
} as Meta<DatePickerComponent>;

export const Default = {
  render: (args: DatePickerComponent) => ({
    props: {
      ...args,
      control: new FormControl(new Date()),
    },
  }),
  args: {
    label: 'Pick a date',
  },
};
