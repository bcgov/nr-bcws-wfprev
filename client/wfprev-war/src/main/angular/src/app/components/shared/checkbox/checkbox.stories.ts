import { Meta, moduleMetadata } from '@storybook/angular';
import { CheckboxComponent } from 'src/app/components/shared/checkbox/checkbox.component';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';

export default {
  title: 'Components/Checkbox',
  component: CheckboxComponent,
  decorators: [
    moduleMetadata({
      imports: [
        ReactiveFormsModule,
        MatCheckboxModule,
        CheckboxComponent,
      ],
    }),
  ],
} as Meta<CheckboxComponent>;

export const Default = {
  render: (args: CheckboxComponent) => {
    const fb = new FormBuilder();
    const form: FormGroup = fb.group({
      example: [false],
    });

    return {
      props: {
        ...args,
      },
      template: `
        <form [formGroup]="form">
          <wfprev-checkbox formControlName="example">
            Example Checkbox Label
          </wfprev-checkbox>
        </form>
      `,
      moduleMetadata: {
        imports: [
          ReactiveFormsModule,
          MatCheckboxModule,
          CheckboxComponent,
        ],
      },
      context: {
        form,
      },
    };
  },
  args: {
    disabled: false,
  },
};
