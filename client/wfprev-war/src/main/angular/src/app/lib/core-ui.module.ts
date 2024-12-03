import { APP_INITIALIZER, Injector, ModuleWithProviders, NgModule } from "@angular/core";
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
            multi: true,
            deps: [Injector]
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