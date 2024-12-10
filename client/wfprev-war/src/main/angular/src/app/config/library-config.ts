import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export class LibraryConfig {
  public configurationPath: string;

  constructor() {
    this.configurationPath = '';
  }
}

