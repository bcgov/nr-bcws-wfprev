import { CommonModule } from '@angular/common'; // Import CommonModule
import { Component, Input } from '@angular/core';

@Component({
  standalone: true, // Make it standalone
  imports: [CommonModule], // Import necessary modules for the template (if any)
  selector: 'wfprev-expansion-indicator',
  templateUrl: './expansion-indicator.component.html',
  styleUrls: ['./expansion-indicator.component.scss']
})
export class ExpansionIndicatorComponent {
  @Input() isExpanded: boolean = false;
  @Input() marginRight: string = '8px'; // Default margin-right
  @Input() iconSize: string | null = null; // Default icon size to null (no specific scaling by default)
}
