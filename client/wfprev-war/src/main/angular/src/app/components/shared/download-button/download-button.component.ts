import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { DownloadOption } from 'src/app/components/models';

@Component({
    selector: 'wfprev-download-button',
    templateUrl: './download-button.component.html',
    styleUrls: ['./download-button.component.scss'],
    imports: [MatMenuModule, MatButtonModule, MatIconModule],
    standalone: true,
})
export class DownloadButtonComponent {
  @Input() disabled = false;
  @Input() formats: DownloadOption[] = [];
  @Output() download = new EventEmitter<string>();

  isDisabled(): boolean {
    return this.disabled;
  }

  onDownload(type: string): void {
    this.download.emit(type);
  }
}
