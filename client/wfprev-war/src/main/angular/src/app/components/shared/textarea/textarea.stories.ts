import { Meta, moduleMetadata } from '@storybook/angular';
import { TextareaComponent } from 'src/app/components/shared/textarea/textarea.component';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TextFieldModule } from '@angular/cdk/text-field';
import { CommonModule } from '@angular/common';

export default {
  title: 'Components/Textarea',
  component: TextareaComponent,
  decorators: [
    moduleMetadata({
      imports: [
        CommonModule,
        ReactiveFormsModule,
        TextFieldModule,
        TextareaComponent,
      ],
    }),
  ],
} as Meta<TextareaComponent>;

export const Default = {
  render: (args: TextareaComponent) => ({
    props: {
      ...args,
      control: new FormControl(''),
    },
  }),
  args: {
    label: 'Description',
    placeholder: 'Enter text here...',
    required: false,
    rows: 4,
  },
};

export const Prefilled = {
  render: (args: TextareaComponent) => ({
    props: {
      ...args,
      control: new FormControl('This is some prefilled content.'),
    },
  }),
  args: {
    label: 'Notes',
    placeholder: 'Type your notes...',
    required: false,
    rows: 6,
  },
};

export const WithMaxLengthError = {
  render: (args: TextareaComponent) => {
    const control = new FormControl('This text is too long and will show an error.');
    control.setErrors({ maxlength: true });
    control.markAsTouched();
    return {
      props: {
        ...args,
        control,
      },
    };
  },
  args: {
    label: 'Comments',
    placeholder: 'Write something...',
    required: false,
    rows: 4,
  },
};
