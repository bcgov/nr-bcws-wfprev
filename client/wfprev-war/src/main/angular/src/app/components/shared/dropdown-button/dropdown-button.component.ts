import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { ProjectFiscal } from 'src/app/components/models';
import { FiscalActionLabels, FiscalStatuses } from 'src/app/utils/constants';

@Component({
  selector: 'wfprev-dropdown-button',
  templateUrl: './dropdown-button.component.html',
  styleUrls: ['./dropdown-button.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatMenuModule,
    MatButtonModule
  ]
})
export class DropdownButtonComponent {
  @Input() status!: string;
  @Input() isApproved = false;
  @Input() index!: number;
  @Input() fiscal!: ProjectFiscal

  @Output() actionSelected = new EventEmitter<{ action: string; index: number }>();

  readonly FiscalStatuses = FiscalStatuses;
  readonly FiscalActionLabels = FiscalActionLabels;

  emitAction(action: string) {
    this.actionSelected.emit({ action, index: this.index });
  }

  isDisabled(): boolean {
    return [this.FiscalStatuses.COMPLETE, this.FiscalStatuses.CANCELLED].includes(this.status);
  }
}
