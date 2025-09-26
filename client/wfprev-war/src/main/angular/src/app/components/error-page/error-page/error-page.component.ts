import { Component } from '@angular/core';
import { AppConfigService } from 'src/app/services/app-config.service';

@Component({
  selector: 'wfprev-error-page',
  standalone: true,
  imports: [],
  templateUrl: './error-page.component.html',
  styleUrl: './error-page.component.scss'
})
export class ErrorPageComponent {
  remiPlannerEmailAddress: string;

  constructor(private readonly appConfigService: AppConfigService) {
    this.remiPlannerEmailAddress = this.appConfigService.getConfig()?.application?.remiPlannerEmailAddress || '';
  }

}
