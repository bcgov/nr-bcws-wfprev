import { APP_INITIALIZER, Injector, ModuleWithProviders, NgModule } from "@angular/core";
import { LibraryConfig } from "../config/library-config";
import { AppConfigService } from "../services/app-config.service";
import { HttpHandler } from "@angular/common/http";
import { OWL_DATE_TIME_FORMATS } from "@busacca/ng-pick-datetime";
import { DATE_FORMATS } from "@wf1/core-ui";
import { appInitializerFactory } from "../utils";

@NgModule({
  declarations: [], // Add any components, directives, or pipes that belong to this module
  imports: [],      // Add any modules that this module depends on
  exports: []       // Add any components, directives, or pipes that should be available to modules importing this one
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
          {
            provide: OWL_DATE_TIME_FORMATS,
            useValue: DATE_FORMATS
          },
        ]
      };
    }
}