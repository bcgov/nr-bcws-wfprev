import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'wfprev-icon-display-field',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './icon-display-field.component.html',
  styleUrl: './icon-display-field.component.scss'
})
export class IconDisplayFieldComponent {
  @Input() label: string = '';
  @Input() value: string | number | null = '';
  @Input() matIcon: string = '';
  @Input() imageIcon: string = '';
}
