import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { ProjectsListComponent } from 'src/app/components/list-panel/projects-list/projects-list.component';

@Component({
  selector: 'wfprev-resizable-panel',
  standalone: true,
  imports: [CommonModule, MatTabsModule,ProjectsListComponent],
  templateUrl: './resizable-panel.component.html',
  styleUrls: ['./resizable-panel.component.scss'],
})

export class ResizablePanelComponent {
  panelWidth: string = '50vw';
  breakpoints = [5, 50, 90];
  selectedTabIndex = 0; // Default selected tab
  @Output() panelResized = new EventEmitter<void>();
  @ViewChild('projectList') projectList!: ProjectsListComponent;

  tabs = [
    { name: 'Projects', component: 'app-tab1' },
    { name: 'Dashboard', component: 'app-tab2' },
    { name: 'Planning', component: 'app-tab3' }
  ];

  resizePanel(percentage: number): void {
    this.panelWidth = `${percentage}vw`;
    this.panelResized.emit();
  }

  selectTab(index: number): void {
    this.selectedTabIndex = index;
  }

  onParentScroll(event: Event) {
    this.projectList.onScroll(event);
  }
}
