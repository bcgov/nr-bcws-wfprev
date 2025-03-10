import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'add-attachment',
  templateUrl: './add-attachment.component.html',
  styleUrls: ['./add-attachment.component.scss'],
})
export class AddAttachmentComponent {
  selectedFileName: string = '';
  attachmentType: string = '';
  description: string = '';
  attachmentTypes = ['Report', 'Image', 'Document', 'Other'];

  constructor(
    private readonly dialogRef: MatDialogRef<AddAttachmentComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { indicator: string; name: string }
  ) {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFileName = file.name;
    }
  }

  onGoBack(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    if (this.isFormValid()) {
      this.dialogRef.close({ file: this.selectedFileName, type: this.attachmentType, description: this.description });
    }
  }

  isFormValid(): boolean {
    return !!this.selectedFileName && !!this.attachmentType && this.description.length > 0;
  }

  chooseFile() {
  }

  getFileName(): string{
    return 'No File Chosen'; 
  }
}
