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
  dialogUsage: string;

  get dialogTitle(): string {
    switch (this.dialogUsage) {
      case 'confirm-cancel':
        return 'Confirm Cancel';
      case 'confirm-delete':
        return 'Confirm Delete';
      default:
        return 'Duplicate Found';
    }
  }

  constructor(
    private readonly dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { indicator: string }
  ) {
    this.dialogUsage = this.data?.indicator
  }

  onGoBack(): void {
    this.dialogRef.close(false); // Close without confirmation
  }

  onConfirm(): void {
    this.dialogRef.close(true); // Close with confirmation
  }
}

