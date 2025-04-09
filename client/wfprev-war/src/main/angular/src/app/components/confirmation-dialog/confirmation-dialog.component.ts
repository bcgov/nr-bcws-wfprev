import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-confirmation-dialog',
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
    'confirm-cancel': 'Are you sure you want to cancel?<br />This information will not be saved.',
    'duplicate-project': 'This Project already exists:<br />',
    'confirm-delete': 'Are you sure you want to delete this fiscal year?<br />This action cannot be undone.',
    'confirm-unsave': 'Are you sure you want to leave this page?<br />The changes you made will not be saved.',
    'confirm-delete-attachment': 'Are you sure you want to delete this file?<br />',
  };

  get dialogMessage(): string {
    if (this.dialogUsage === 'delete-activity') {
      return this.getDeleteActivityMessage();
    }
    return this.dialogMessages[this.dialogUsage] || '';
  }

  get dialogTitle(): string {
    switch (this.dialogUsage) {
      case 'confirm-cancel':
        return 'Confirm Cancel';
      case 'confirm-delete':
        return 'Confirm Delete';
      case 'confirm-unsave':
        return 'Confirm Unsave'
      case 'delete-activity':
        return 'Delete Activity'
      default:
        return 'Duplicate Found';
    }
  }

  constructor(
    private readonly dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { indicator: string, name: string }
  ) {
    this.dialogUsage = this.data?.indicator
  }

  getDeleteActivityMessage(): string {
    return `Are you sure you want to delete ${this.data?.name || 'this activity'}? This action cannot be reversed and will immediately remove the activity from the Fiscal scope.`;
  }
  onGoBack(): void {
    this.dialogRef.close(false); // Close without confirmation
  }

  onConfirm(): void {
    this.dialogRef.close(true); // Close with confirmation
  }
}

