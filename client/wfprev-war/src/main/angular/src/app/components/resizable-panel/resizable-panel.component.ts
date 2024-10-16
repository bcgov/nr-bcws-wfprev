import { Component, EventEmitter, HostListener, Output } from '@angular/core';

@Component({
  selector: 'app-resizable-panel',
  standalone: true,
  imports: [],
  templateUrl: './resizable-panel.component.html',
  styleUrl: './resizable-panel.component.scss'
})
export class ResizablePanelComponent {
  panelWidth: string = '50vw';  // Default panel width at 50%
  @Output() panelResized = new EventEmitter<void>();
  // Method to resize the panel based on given percentage
  resizePanel(percentage: number): void {
    this.panelWidth = `${percentage}vw`;  // Resize based on viewport width
    this.panelResized.emit();
  }

  // Listen to window resize to handle responsive behavior
  @HostListener('window:resize', ['$event'])
  onResize(event: Event): void {
    // Implement any additional logic if needed when window resizes
  }

}