import { Meta, moduleMetadata } from '@storybook/angular';
import { DetailsContainerComponent } from 'src/app/components/shared/details-container/details-container.component';
import { action } from '@storybook/addon-actions';

export default {
  title: 'Components/DetailsContainer',
  component: DetailsContainerComponent,
  decorators: [
    moduleMetadata({
      imports: [
        DetailsContainerComponent,
      ],
    }),
  ],
} as Meta<DetailsContainerComponent>;

export const Default = {
  render: (args: DetailsContainerComponent) => ({
    props: {
      ...args,
      save: action('save'),
      cancel: action('cancel'),
    },
    template: `
      <wfprev-details-container
        [title]="title"
        [saveText]="saveText"
        [cancelText]="cancelText"
        [saveDisabled]="saveDisabled"
        [cancelDisabled]="cancelDisabled"
        (save)="save($event)"
        (cancel)="cancel($event)">
        <p>This is some projected content inside the container.</p>
      </wfprev-details-container>
    `,
  }),
  args: {
    title: 'My Details Container',
    saveText: 'Save Changes',
    cancelText: 'Cancel',
    saveDisabled: false,
    cancelDisabled: false,
  },
};
