import { HttpBackend, HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { AsyncSubject, Observable, firstValueFrom } from "rxjs";
import { LibraryConfig } from "../config/library-config";
import { ApplicationConfig } from "../interfaces/application-config";

@Injectable({
  providedIn: 'root',
})
export class AppConfigService {
  private appConfig!: ApplicationConfig;
  private configSubject = new AsyncSubject<ApplicationConfig>();
  public configEmitter: Observable<ApplicationConfig> = this.configSubject.asObservable();

  constructor(private httpHandler: HttpBackend, private libConfig: LibraryConfig) {}

  async loadAppConfig(): Promise<void> {
    const http = new HttpClient(this.httpHandler);

    try {
      const data = await firstValueFrom(
        http.get<ApplicationConfig>(this.libConfig.configurationPath)
      );

      if (!data) {
        throw new Error('No data returned from application config');
      }

      // Cache configuration and notify subscribers
      this.appConfig = data;
      this.configSubject.next(this.appConfig);
      this.configSubject.complete();
    } catch (error) {
      this.configSubject.error(error); // Notify subscribers of the error
      throw new Error(`Failed to load application configuration: ${(error as Error).message}`);
    }
  }

  getConfig(): ApplicationConfig {
    if (!this.appConfig) {
      throw new Error('Configuration not loaded. Please call loadAppConfig() first.');
    }
    return this.appConfig;
  }
}