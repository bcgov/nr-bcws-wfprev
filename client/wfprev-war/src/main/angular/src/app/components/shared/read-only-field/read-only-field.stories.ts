import { Meta, moduleMetadata } from '@storybook/angular';
import { ReadOnlyFieldComponent } from 'src/app/components/shared/read-only-field/read-only-field.component';

export default {
  title: 'Components/ReadOnlyField',
  component: ReadOnlyFieldComponent,
  decorators: [
    moduleMetadata({
      imports: [ReadOnlyFieldComponent],
    }),
  ],
} as Meta<ReadOnlyFieldComponent>;

export const Default = {
  render: (args: ReadOnlyFieldComponent) => ({
    props: args,
  }),
  args: {
    label: 'Label',
    value: 'This is the value',
  },
};

export const EmptyValue = {
  render: (args: ReadOnlyFieldComponent) => ({
    props: args,
  }),
  args: {
    label: 'Empty Field',
    value: '',
  },
};

export const LongText = {
  render: (args: ReadOnlyFieldComponent) => ({
    props: args,
  }),
  args: {
    label: 'Description',
    value: 'This is a much longer piece of text meant to demonstrate wrapping or overflow handling.',
  },
};