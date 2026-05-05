import { Component, inject } from '@angular/core';
import { AppConfigService } from 'src/app/services/app-config.service';

@Component({
    selector: 'wfprev-error-page',
    imports: [],
    templateUrl: './error-page.component.html',
    styleUrl: './error-page.component.scss'
})
export class ErrorPageComponent {
  private readonly appConfigService = inject(AppConfigService);

  remiPlannerEmailAddress: string;

  constructor() {
    this.remiPlannerEmailAddress = this.appConfigService.getConfig()?.application?.remiPlannerEmailAddress || '';
  }

}
