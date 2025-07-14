import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ModalTitles } from 'src/app/utils/constants';

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

  get dialogMessage(): string {
    return this.data?.message || '';
  }

  get dialogTitle(): string {
    if(this.data?.title) return this.data?.title
    else return ModalTitles.DUPLICATE_FOUND_TITLE
  }

  get confirmButtonText(): string {
    if (this.dialogUsage.startsWith('delete-')) {
      return 'Delete';
    }
    return 'Continue';
  }

  get isDeleteDialog(): boolean {
    return this.dialogUsage.startsWith('delete-');
  }

  constructor(
    private readonly dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { indicator: string, title: string, name: string, message: string}
  ) {
    this.dialogUsage = this.data?.indicator
  }

  onGoBack(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}

