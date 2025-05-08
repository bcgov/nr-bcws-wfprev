import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SharedService {
  private filtersSource = new BehaviorSubject<any>(null);
  filters$ = this.filtersSource.asObservable();

  updateFilters(filters: any) {
    this.filtersSource.next(filters);
  }
}