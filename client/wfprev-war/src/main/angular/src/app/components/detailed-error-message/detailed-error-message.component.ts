import { Component, Inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA, MatSnackBarRef } from '@angular/material/snack-bar';
import { DetailedErrorMessage } from '../models';

@Component({
  selector: 'app-detailed-error-message',
  standalone: true,
  templateUrl: './detailed-error-message.component.html',
  styleUrl: './detailed-error-message.component.scss'
})
export class DetailedErrorMessageComponent {

  showReasons = false;

  constructor(
    @Inject(MAT_SNACK_BAR_DATA)
    public data: DetailedErrorMessage,
    private snackBarRef: MatSnackBarRef<DetailedErrorMessageComponent>
  ) {}

  showDetails(): void {
    this.showReasons = true;
  }

  close(): void {
    this.snackBarRef.dismiss();
  }

}
