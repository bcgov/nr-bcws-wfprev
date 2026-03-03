import { Injectable, signal } from '@angular/core';
import { ProjectFilter } from '../components/models';

@Injectable({
  providedIn: 'root'
})
export class ProjectFilterStateService {

  private readonly STORAGE_KEY = 'project-filters';

  filters = signal<ProjectFilter | null>(this.load());

  set(filters: ProjectFilter) {
    this.filters.set(filters);
    sessionStorage.setItem(this.STORAGE_KEY, JSON.stringify(filters));
  }

  update(changes: Partial<ProjectFilter>) {
    this.filters.update(current => {
      const updated = {
        ...current,
        ...changes
      };

      sessionStorage.setItem(this.STORAGE_KEY, JSON.stringify(updated));
      return updated;
    });
  }

  clear() {
    this.filters.set(null);
    sessionStorage.removeItem(this.STORAGE_KEY);
  }

  private load(): ProjectFilter | null {
    const raw = sessionStorage.getItem(this.STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  }

}
