import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ProjectFiscalsSignalService {

  reloadFiscals = signal(false);

  constructor() { }

  trigger() {
    this.reloadFiscals.update(v => !v);
  }
}
