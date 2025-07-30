import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'wfprev-download-button',
  templateUrl: './download-button.component.html',
  styleUrls: ['./download-button.component.scss'],
  imports: [MatMenuModule, MatButtonModule, MatIconModule, CommonModule],
  standalone: true,
})
export class DownloadButtonComponent {
  @Input() disabled = false;
  @Input() formats: string[] = [];
  @Output() download = new EventEmitter<string>();

  isDisabled(): boolean {
    return this.disabled;
  }

  onDownload(type: string): void {
    this.download.emit(type);
  }
}
