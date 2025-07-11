import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'wfprev-confirmation-dialog',
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule
  ],
})
export class ConfirmationDialogComponent {
  dialogUsage: string = 'confirm-cancel';

  private readonly dialogMessages: Record<string, string> = {
    'confirm-cancel': 'Are you sure you want to cancel? This information will not be saved.',
    'duplicate-project': 'This Project already exists: ',
    'confirm-unsave': 'Are you sure you want to leave this page? The changes you made will not be saved.',
    'delete-attachment': 'Are you sure you want to delete this file? ',
  };

  get dialogMessage(): string {
    if (this.dialogUsage === 'delete-fiscal-year') {
      return this.getDeleteFiscalYearMessage();
    }
    if (this.dialogUsage === 'delete-activity') {
      return this.getDeleteActivityMessage();
    }
    if (this.dialogUsage === 'confirm-fiscal-status-update') {
      return this.getConfirmFiscalStatusUpdateMessage();
    }
    return this.dialogMessages[this.dialogUsage] || '';
  }

  get dialogTitle(): string {
    const newStatus = this.capitalizeFirstLetter(this.data?.newStatus);
    switch (this.dialogUsage) {
      case 'confirm-cancel':
        return 'Confirm Cancel';
      case 'confirm-unsave':
        return 'Confirm Unsave'
      case 'confirm-fiscal-status-update':
        return `Confirm Change to ${newStatus}`
      case 'delete-attachment':
        return 'Delete Attachment';
      case 'delete-fiscal-year':
        return 'Delete Fiscal Year'
      case 'delete-activity':
        return 'Delete Activity'
      default:
        return 'Duplicate Found';
    }
  }

  get confirmButtonText(): string {
    if (this.dialogUsage.startsWith('delete-')) {
      return 'Delete';
    }
    return 'Continue';
  }

  get confirmationIcon(): string {
    if (this.dialogUsage.startsWith('delete-')) {
      return '/assets/warning-red.svg';
    }
    return '/assets/warning-yellow.svg';
  }

  get isDeleteDialog(): boolean {
    return this.dialogUsage.startsWith('delete-');
  }

  constructor(
    private readonly dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { indicator: string, name: string, currentStatus: string, newStatus: string }
  ) {
    this.dialogUsage = this.data?.indicator
  }

  getDeleteActivityMessage(): string {
    return `Are you sure you want to delete ${this.data?.name || 'this activity'}? This action cannot be reversed and will immediately remove the activity from the Fiscal scope.`;
  }

  getDeleteFiscalYearMessage(): string {
    return `Are you sure you want to delete ${this.data?.name || 'this fiscal year'}? This action cannot be reversed and will immediately remove the Fiscal Year from the Project scope.`;
  }

  getConfirmFiscalStatusUpdateMessage(): string {
    const currentStatus = this.capitalizeFirstLetter(this.data?.currentStatus);
    const newStatus = this.capitalizeFirstLetter(this.data?.newStatus);
    return `You are about the change the status of this Project from ${currentStatus} to ${newStatus}. Do you wish to continue?`
  }

  capitalizeFirstLetter(status: string): string {
    if (!status) return '';
    return status.toLowerCase().split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
  }

  onGoBack(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}

