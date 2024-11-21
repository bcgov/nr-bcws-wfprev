import { AppConfigService } from 'src/app/services/app-config.service';

export function initializeAppConfig(appConfigService: AppConfigService): () => Promise<void> {
  return () =>
    new Promise<void>((resolve, reject) => {
      appConfigService.loadAppConfig()
        .then(() => {
          resolve(); // Resolve the promise if the config is successfully loaded
        })
        .catch((error: any) => {
          console.error('Failed to load app config', error);
          reject(error); // Reject the promise if there is an error
        });
    });
}