import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'wfprev-download-button',
  templateUrl: './download-button.component.html',
  styleUrls: ['./download-button.component.scss'],
  imports: [MatMenuModule, MatButtonModule, MatIconModule],
  standalone: true,
})
export class DownloadButtonComponent {
  @Input() disabled = false;
  @Output() download = new EventEmitter<'csv' | 'excel'>();

  isDisabled(): boolean {
    return this.disabled;
  }

  onDownload(type: 'csv' | 'excel'): void {
    this.download.emit(type);
  }
}
