import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SharedCodeTableService {
  private codeTables = new BehaviorSubject<any>({});
  codeTables$ = this.codeTables.asObservable();

  updateCodeTables(updated: any) {
    this.codeTables.next(updated);
  }
}
