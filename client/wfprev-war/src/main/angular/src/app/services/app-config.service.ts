import { Injectable } from "@angular/core";
import { HttpBackend, HttpClient } from "@angular/common/http";
import { AsyncSubject, firstValueFrom, Observable } from "rxjs";
import { LibraryConfig } from "../config/library-config";
import { ApplicationConfig } from "../interfaces/application-config"

@Injectable({
    providedIn: 'root',
})
export class AppConfigService {
    private appConfig!: ApplicationConfig;
    private config = new AsyncSubject<ApplicationConfig>();
    public configEmitter: Observable<ApplicationConfig> = this.config.asObservable();

    constructor(private httpHandler: HttpBackend, private libConfig: LibraryConfig) {
    }

    async loadAppConfig(): Promise<void> {
        const http = new HttpClient(this.httpHandler);

        try {
            const data = await firstValueFrom(http.get<ApplicationConfig>('/assets/data/appConfig.json'));
            this.appConfig = data;
            this.config.next(this.appConfig);
            this.config.complete();
        } catch (error) {
            console.error('Failed to load application config:', error);
            throw error;
        }
    }

    getConfig(): ApplicationConfig {
        if (!this.appConfig) {
            throw new Error('Configuration not loaded. Please call loadAppConfig() first.');
        }
        return this.appConfig;
    }
}