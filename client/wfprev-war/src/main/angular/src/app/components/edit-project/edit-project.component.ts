import { AfterViewInit, Component, ComponentRef, ViewChild, ViewContainerRef } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { ProjectDetailsComponent } from 'src/app/components/edit-project/project-details/project-details.component';
import { ProjectFiscalsComponent } from 'src/app/components/edit-project/project-fiscals/project-fiscals.component';
import { CanComponentDeactivate } from 'src/app/services/util/can-deactive.guard';
import { ResourcesRoutes } from 'src/app/utils';

@Component({
  selector: 'wfprev-edit-project',
  standalone: true,
  imports: [MatTabsModule, ProjectDetailsComponent],
  templateUrl: './edit-project.component.html',
  styleUrl: './edit-project.component.scss'
})
export class EditProjectComponent implements CanComponentDeactivate, AfterViewInit {
  projectName: string | null = null;
  @ViewChild('fiscalsContainer', { read: ViewContainerRef }) fiscalsContainer!: ViewContainerRef;
  @ViewChild(ProjectDetailsComponent) projectDetailsComponent!: ProjectDetailsComponent;

  projectFiscalsComponentRef: ComponentRef<any> | null = null;
  activeRoute = '';
  selectedTabIndex = 0;
  focusedFiscalId: string | null = null;

  constructor(
    protected router: Router,
    private readonly route: ActivatedRoute
  ) { }

  ngAfterViewInit(): void {
    // Check if the URL includes a tab parameter (e.g. ?tab=fiscal)
    // If 'fiscal' is specified, activate the Fiscal Activity tab
    // Extract fiscalGuid query param to direct towards a specific fiscal
    const tab = this.route.snapshot.queryParamMap.get('tab');
    this.focusedFiscalId = this.route.snapshot.queryParamMap.get('fiscalGuid');

    if (tab === 'fiscal') {
      this.selectedTabIndex = 1;
      this.loadFiscalComponent();
    }
  }

  onTabChange(event: any): void {
    this.selectedTabIndex = event.index;

    const isFiscalTab = event.index === 1;
    const tab = isFiscalTab ? 'fiscal' : 'details';

    // Fetch current query params
    const queryParams: { [key: string]: any } = { ...this.route.snapshot.queryParams, tab };

    if (isFiscalTab) {
      // Add fiscalGuid if we have a selected fiscal
      const fiscalGuid = this.projectFiscalsComponentRef?.instance?.currentFiscalGuid;
      if (fiscalGuid) {
        queryParams['fiscalGuid'] = fiscalGuid;
      }
    } else {
      // Remove fiscalGuid from URL when switching to Details tab
      queryParams['fiscalGuid'] = null;
    }

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
      queryParamsHandling: 'merge',
      replaceUrl: true
    });

    // Check if "Fiscal Years" tab is selected
    if (event.index === 1 && !this.projectFiscalsComponentRef) {
      import('./project-fiscals/project-fiscals.component').then(
        ({ ProjectFiscalsComponent }) => {
          this.fiscalsContainer.clear();
          this.projectFiscalsComponentRef = this.fiscalsContainer.createComponent(ProjectFiscalsComponent);
          this.projectFiscalsComponentRef.instance.focusedFiscalId = this.focusedFiscalId;
          this.projectFiscalsComponentRef.instance.loadProjectFiscals();
        }
      );
    } else if (event.index === 0) {
      this.projectDetailsComponent.refreshFiscalData();
    }
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.projectDetailsComponent?.isFormDirty?.()) {
      return this.projectDetailsComponent.canDeactivate?.();
    }

    if (this.projectFiscalsComponentRef?.instance?.isFormDirty?.()) {
      return this.projectFiscalsComponentRef.instance.canDeactivate();
    }

    return true;
  }

  setActive(menuItem: string): void {
    this.activeRoute = menuItem;
    switch (menuItem) {
      case 'list':
        this.router.navigate([ResourcesRoutes.LIST]);
        break;
      case 'map':
        this.router.navigate([ResourcesRoutes.MAP]);
        break;
    }
  }

  loadFiscalsTab(): void {
    this.fiscalsContainer.clear();
    const componentRef = this.fiscalsContainer.createComponent(ProjectFiscalsComponent);
    componentRef.instance.focusedFiscalId = this.focusedFiscalId;
  }

  loadFiscalComponent(): void {
    if (!this.projectFiscalsComponentRef) {
      this.getProjectFiscalsComponent().then(({ ProjectFiscalsComponent }) => {
        this.fiscalsContainer.clear();
        this.projectFiscalsComponentRef = this.fiscalsContainer.createComponent(ProjectFiscalsComponent);
        this.projectFiscalsComponentRef.instance.focusedFiscalId = this.focusedFiscalId;
        this.projectFiscalsComponentRef.instance.loadProjectFiscals();
      });
    }
  }

  getProjectFiscalsComponent(): Promise<any> {
    return import('./project-fiscals/project-fiscals.component');
  }

}