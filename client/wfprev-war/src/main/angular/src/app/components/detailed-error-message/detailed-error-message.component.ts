import { Component, inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA, MatSnackBarRef } from '@angular/material/snack-bar';
import { DetailedErrorMessage } from '../models';

@Component({
  selector: 'app-detailed-error-message',
  standalone: true,
  templateUrl: './detailed-error-message.component.html',
  styleUrl: './detailed-error-message.component.scss'
})
export class DetailedErrorMessageComponent {
  data = inject<DetailedErrorMessage>(MAT_SNACK_BAR_DATA);
  private snackBarRef = inject<MatSnackBarRef<DetailedErrorMessageComponent>>(MatSnackBarRef);


  showReasons = false;

  showDetails(): void {
    this.showReasons = true;
  }

  close(): void {
    this.snackBarRef.dismiss();
  }

}
