import { Component, ComponentRef, ViewChild, ViewContainerRef } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { Observable } from 'rxjs';
import { ProjectDetailsComponent } from 'src/app/components/edit-project/project-details/project-details.component';
import { ProjectFiscalsComponent } from 'src/app/components/edit-project/project-fiscals/project-fiscals.component';
import { CanComponentDeactivate } from 'src/app/services/util/can-deactive.guard';

@Component({
  selector: 'app-edit-project',
  standalone: true,
  imports: [MatTabsModule, ProjectDetailsComponent, ProjectFiscalsComponent],
  templateUrl: './edit-project.component.html',
  styleUrl: './edit-project.component.scss'
})
export class EditProjectComponent implements CanComponentDeactivate {
  projectName: string | null = null;
  @ViewChild('fiscalsContainer', { read: ViewContainerRef }) fiscalsContainer!: ViewContainerRef;
  projectFiscalsComponentRef: ComponentRef<any> | null = null;

  constructor() {}

  onTabChange(event: any): void {
    // Check if "Fiscal Years" tab is selected
    if (event.index === 1 && !this.projectFiscalsComponentRef) {
      import('./project-fiscals/project-fiscals.component').then(
        ({ ProjectFiscalsComponent }) => {
          this.fiscalsContainer.clear();
          this.projectFiscalsComponentRef = this.fiscalsContainer.createComponent(ProjectFiscalsComponent);
          this.projectFiscalsComponentRef.instance.loadProjectFiscals();
        }
      );
    }
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.projectFiscalsComponentRef?.instance?.isFormDirty()) {
      return this.projectFiscalsComponentRef.instance.canDeactivate();
    }
    return true;
  }
}