import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { Project } from 'src/app/components/models';

@Injectable({ providedIn: 'root' })
export class SharedService {
  private readonly filtersSource = new BehaviorSubject<any>(null);
  filters$ = this.filtersSource.asObservable();

  private readonly displayedProjectsSource = new BehaviorSubject<any[]>([]);
  displayedProjects$ = this.displayedProjectsSource.asObservable();

  private readonly selectedProjectSubject = new Subject<any>();
  selectedProject$ = this.selectedProjectSubject.asObservable();

  private readonly _mapCommand$ = new Subject<{ action: 'open' | 'close', project: Project }>();
  mapCommand$ = this._mapCommand$.asObservable();

  updateFilters(filters: any) {
    this.filtersSource.next(filters);
  }

  updateDisplayedProjects(projects: Project[]) {
    this.displayedProjectsSource.next(projects);
  }

  selectProject(project?: Partial<Project>) {
    this.selectedProjectSubject.next(project as Project);
  }

  triggerMapCommand(action: 'open' | 'close', project: Project) {
    this._mapCommand$.next({ action, project });
  }

  get currentFilters(): any {
    return this.filtersSource.getValue();
  }

  get currentDisplayedProjects(): Project[] {
    return this.displayedProjectsSource.getValue();
  }
}
