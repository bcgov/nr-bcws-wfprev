import { Injector, ModuleWithProviders, NgModule, inject, APP_INITIALIZER } from "@angular/core";
import { LibraryConfig } from "../config/library-config";
import { AppConfigService } from "../services/app-config.service";
import { HttpHandler } from "@angular/common/http";
import { appInitializerFactory } from "../utils";

@NgModule({
  declarations: [], 
  imports: [],    
  exports: []     
})
export class CoreUIModule {
    static forRoot(config: LibraryConfig): ModuleWithProviders<CoreUIModule> {
      return {
        ngModule: CoreUIModule,
        providers: [
          {
            provide: APP_INITIALIZER,
            useFactory: appInitializerFactory,
            deps: [Injector],
            multi: true
          },
          {
            provide: LibraryConfig,
            useValue: config
          },
          {
            provide: AppConfigService,
            useClass: AppConfigService,
            deps: [HttpHandler, LibraryConfig]
          },
        ]
      };
    }
}