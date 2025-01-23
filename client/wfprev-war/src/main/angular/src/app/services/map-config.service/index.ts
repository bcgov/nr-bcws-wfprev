import { Injectable } from '@angular/core';
import {
  mapConfig,
} from './map.config';
import { AppConfigService } from '../app-config.service';

@Injectable()
export class MapConfigService {
  constructor(private readonly appConfig: AppConfigService) {}

  getMapConfig(): Promise<any> {

    return this.appConfig
      .loadAppConfig()
      .then((config) =>
        mapConfig(),
      );
  }


}
