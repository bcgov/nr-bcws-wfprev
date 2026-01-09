import { Component } from '@angular/core';
import { MatExpansionModule, MatExpansionPanel, MatExpansionPanelHeader } from '@angular/material/expansion';
import { ExpansionIndicatorComponent } from "../../shared/expansion-indicator/expansion-indicator.component";
import { IconButtonComponent } from "../../shared/icon-button/icon-button.component";

@Component({
  selector: 'wfprev-performance-update',
  standalone: true,
  imports: [MatExpansionPanel, ExpansionIndicatorComponent, MatExpansionPanelHeader, IconButtonComponent],
  templateUrl: './performance-update.component.html',
  styleUrl: './performance-update.component.scss'
})
export class PerformanceUpdateComponent {

}
