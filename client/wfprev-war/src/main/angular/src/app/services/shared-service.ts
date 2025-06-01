import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { Project } from 'src/app/components/models';

@Injectable({ providedIn: 'root' })
export class SharedService {
  private readonly filtersSource = new BehaviorSubject<any>(null);
  filters$ = this.filtersSource.asObservable();

  private readonly displayedProjectsSource = new BehaviorSubject<any[]>([]);
  displayedProjects$ = this.displayedProjectsSource.asObservable();

  private selectedProjectSubject = new Subject<any>();
  selectedProject$ = this.selectedProjectSubject.asObservable();

  updateFilters(filters: any) {
    this.filtersSource.next(filters);
  }

  updateDisplayedProjects(projects: Project[]) {
    this.displayedProjectsSource.next(projects);
  }

  selectProject(project?: Project) {
    this.selectedProjectSubject.next(project);
  }
}
