import { Component, ComponentRef, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, Observable } from 'rxjs';
import { ProjectDetailsComponent } from 'src/app/components/edit-project/project-details/project-details.component';
import { ProjectFiscalsComponent } from 'src/app/components/edit-project/project-fiscals/project-fiscals.component';
import { CanComponentDeactivate } from 'src/app/services/util/can-deactive.guard';
import { EditProjectTabIndexes, ResourcesRoutes } from 'src/app/utils';

@Component({
  selector: 'wfprev-edit-project',
  standalone: true,
  imports: [MatTabsModule, ProjectDetailsComponent],
  templateUrl: './edit-project.component.html',
  styleUrl: './edit-project.component.scss'
})
export class EditProjectComponent implements CanComponentDeactivate, OnInit {
  @ViewChild('fiscalsContainer', { read: ViewContainerRef }) fiscalsContainer!: ViewContainerRef;
  @ViewChild(ProjectDetailsComponent) projectDetailsComponent!: ProjectDetailsComponent;

  projectName: string | null = null;
  projectFiscalsComponentRef: ComponentRef<any> | null = null;
  activeRoute = '';
  // default to details tab
  selectedTabIndex = EditProjectTabIndexes.Details;
  focusedFiscalId: string | null = null;

  constructor(
    protected router: Router,
    private readonly route: ActivatedRoute
  ) { }


  ngOnInit(): void {
    this.route.queryParamMap
      .pipe(
        filter(params => !!params.get('projectGuid')) // wait until projectGuid is present
      )
      .subscribe(params => {
        const tab = params.get('tab');
        const fiscalGuid = params.get('fiscalGuid');

        this.focusedFiscalId = fiscalGuid;

        if (!tab) {
          // if no tab provided, default to Details tab
          this.selectedTabIndex = EditProjectTabIndexes.Details;
          this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { tab: 'details' },
            queryParamsHandling: 'merge' 
          });
          return;
        }

        if (tab === 'fiscal') {
          this.selectedTabIndex = EditProjectTabIndexes.Fiscal;

          if (!fiscalGuid) {
            // if no fiscalGuid and tab=fiscal, load the component and route to the first fiscal
            this.loadFiscalComponent().then(() => {
              const firstFiscalGuid = this.projectFiscalsComponentRef?.instance?.getFirstFiscalGuid?.();
              if (firstFiscalGuid) {
                this.router.navigate([], {
                  relativeTo: this.route,
                  queryParams: {
                    tab: 'fiscal',
                    fiscalGuid: firstFiscalGuid
                  },
                  queryParamsHandling: 'merge' 
                });
              }
            });
          } else {
            this.loadFiscalComponent();
          }
        } else {
          // Details tab (or invalid tab, fallback to details)
          this.selectedTabIndex = EditProjectTabIndexes.Details;

          if (fiscalGuid) {
            // Clean up fiscalGuid if it's present in Details view
            this.router.navigate([], {
              relativeTo: this.route,
              queryParams: { fiscalGuid: null },
              queryParamsHandling: 'merge'
            });
          }
        }
      });

    // Fallback to map page when projectGuid is missing
    this.route.queryParamMap
      .pipe(filter(params => !params.get('projectGuid')))
      .subscribe(() => {
        this.router.navigate([ResourcesRoutes.MAP]);
      });
  }

  onTabChange(event: any): void {
    this.selectedTabIndex = event.index;

    const isFiscalTab = event.index === EditProjectTabIndexes.Fiscal;
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
      queryParamsHandling: 'merge'
    });

    // Check if "Fiscal Activities" tab is selected
    if (event.index === EditProjectTabIndexes.Fiscal && !this.projectFiscalsComponentRef) {
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

  loadFiscalComponent(): Promise<void> {
    if (!this.projectFiscalsComponentRef) {
      return this.getProjectFiscalsComponent().then(({ ProjectFiscalsComponent }) => {
        this.fiscalsContainer.clear();
        this.projectFiscalsComponentRef = this.fiscalsContainer.createComponent(ProjectFiscalsComponent);
        this.projectFiscalsComponentRef.instance.focusedFiscalId = this.focusedFiscalId;
        this.projectFiscalsComponentRef.instance.loadProjectFiscals();
      });
    }
    return Promise.resolve();
  }

  getProjectFiscalsComponent(): Promise<any> {
    return import('./project-fiscals/project-fiscals.component');
  }

}