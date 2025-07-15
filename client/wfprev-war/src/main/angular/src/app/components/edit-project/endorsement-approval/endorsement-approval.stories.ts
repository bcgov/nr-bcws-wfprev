import { Meta, moduleMetadata } from '@storybook/angular';
import { action } from '@storybook/addon-actions';
import { EndorsementApprovalComponent } from 'src/app/components/edit-project/endorsement-approval/endorsement-approval.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Mock ProjectFiscal
const mockFiscal = {
  projectPlanFiscalGuid: 'abc-123',
  endorserName: 'Jane Doe',
  endorsementTimestamp: '2024-05-10T12:00:00Z',
  endorsementComment: 'Looks good to me.',
  isApprovedInd: true,
  approvedTimestamp: '2024-05-11T15:00:00Z',
  approverName: 'John Smith',
  businessAreaComment: 'Approved with minor comments.',
  planFiscalStatusCode: { planFiscalStatusCode: 'APPROVED' }
} as any;

export default {
  title: 'Components/EndorsementApproval',
  component: EndorsementApprovalComponent,
  decorators: [
    moduleMetadata({
      imports: [
        FormsModule,
        ReactiveFormsModule,
      ],
    }),
  ],
} as Meta<EndorsementApprovalComponent>;

export const Default = {
  render: (args: EndorsementApprovalComponent) => ({
    props: {
      ...args,
      saveEndorsement: action('saveEndorsement'),
    },
  }),
  args: {
    fiscal: mockFiscal,
    currentUser: 'Alice Tester',
  },
};
