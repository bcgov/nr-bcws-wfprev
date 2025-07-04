import { CommonModule } from '@angular/common';
import { Component, Input, ContentChild, TemplateRef, ViewChild } from '@angular/core';
import { MatExpansionModule, MatExpansionPanel } from '@angular/material/expansion';

@Component({
  selector: 'wfprev-expansion-panel',
  standalone: true,
  imports: [CommonModule, MatExpansionModule],
  templateUrl: './expansion-panel.component.html',
  styleUrls: ['./expansion-panel.component.scss']
})
export class ExpansionPanelComponent {
  @Input() title: string = '';

  @ContentChild('header') headerTemplate?: TemplateRef<any>;

  @ViewChild(MatExpansionPanel, { static: true }) panel!: MatExpansionPanel;
}
