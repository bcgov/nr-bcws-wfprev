import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { Observable } from 'rxjs';

export interface CanComponentDeactivate {
  canDeactivate: () => Observable<boolean> | Promise<boolean> | boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CanDeactivateGuard implements CanDeactivate<CanComponentDeactivate> {
  canDeactivate(component: CanComponentDeactivate): Observable<boolean> | Promise<boolean> | boolean {
    // If the component doesn't have canDeactivate, allow navigation
    if (!component || !Object.hasOwn(component, 'canDeactivate')) {
      return true;
    }

    // Call canDeactivate() and return its result, ensuring undefined/null are treated as true
    return component.canDeactivate() ?? true;
  }
  
}
