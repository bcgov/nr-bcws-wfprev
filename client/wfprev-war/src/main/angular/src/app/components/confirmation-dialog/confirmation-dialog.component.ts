import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ModalTitles } from 'src/app/utils/constants';

@Component({
    selector: 'wfprev-confirmation-dialog',
    templateUrl: './confirmation-dialog.component.html',
    styleUrls: ['./confirmation-dialog.component.scss'],
    imports: [
        CommonModule,
        MatIconModule
    ]
})
export class ConfirmationDialogComponent {
  private readonly dialogRef = inject<MatDialogRef<ConfirmationDialogComponent>>(MatDialogRef);
  data = inject<{
    indicator: string;
    title: string;
    name: string;
    message: string;
}>(MAT_DIALOG_DATA);

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

  get isDeleteOrChangeDialog (): boolean {
    return this.dialogUsage.startsWith('delete-') || this.dialogUsage.startsWith('change-');
  }

  constructor() {
    this.dialogUsage = this.data?.indicator
  }

  onGoBack(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}

