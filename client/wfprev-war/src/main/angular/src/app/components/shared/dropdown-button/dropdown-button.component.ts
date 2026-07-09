
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectFiscal } from 'src/app/components/models';
import { FiscalActionLabels, FiscalStatuses, EndorsementCode, FiscalActions } from 'src/app/utils/constants';

interface DropdownAction {
  label: string;
  action: string;
  requireSaved?: boolean;
  requireGuid?: boolean;
}

@Component({
    selector: 'wfprev-dropdown-button',
    templateUrl: './dropdown-button.component.html',
    styleUrls: ['./dropdown-button.component.scss'],
    standalone: true,
    imports: [
    MatMenuModule,
    MatButtonModule,
    MatTooltipModule
]
})
export class DropdownButtonComponent {
  @Input() status!: string;
  @Input() isApproved = false;
  @Input() index!: number;
  @Input() fiscal!: ProjectFiscal;
  @Input() isDirty: boolean | undefined;
  @Input() isDisabled = false;

  @Output() actionSelected = new EventEmitter<{ action: string; index: number }>();

  readonly FiscalStatuses = FiscalStatuses;
  readonly FiscalActionLabels = FiscalActionLabels;

  private actionMap: Record<string, DropdownAction[]> = {
    [FiscalStatuses.DRAFT]: [
      { label: FiscalActionLabels.SUBMIT, action: FiscalStatuses.PROPOSED, requireSaved: true, requireGuid: true },
      { label: FiscalActionLabels.DELETE, action: FiscalActions.DELETE }
    ],
    [FiscalStatuses.PROPOSED]: [
      { label: FiscalActionLabels.REVERT_TO_DRAFT, action: FiscalStatuses.DRAFT }
    ],
    [FiscalStatuses.PREPARED]: [
      { label: FiscalActionLabels.SET_IN_PROG, action: FiscalStatuses.IN_PROGRESS, requireSaved: true },
      { label: FiscalActionLabels.YEAR_END_UPDATE, action: FiscalActions.YEAR_END_UPDATE, requireSaved: true },
      { label: FiscalActionLabels.CANCEL_FISCAL, action: FiscalActions.YEAR_END_CANCEL, requireSaved: true }
    ],
    [FiscalStatuses.IN_PROGRESS]: [
      { label: FiscalActionLabels.YEAR_END_UPDATE, action: FiscalActions.YEAR_END_UPDATE, requireSaved: true },
      { label: FiscalActionLabels.CANCEL_FISCAL, action: FiscalActions.YEAR_END_CANCEL, requireSaved: true }
    ],
    [FiscalStatuses.COMPLETE]: [
      { label: FiscalActionLabels.YEAR_END_UPDATE, action: FiscalActions.YEAR_END_UPDATE, requireSaved: true },
      { label: FiscalActionLabels.CANCEL_FISCAL, action: FiscalActions.YEAR_END_CANCEL, requireSaved: true }
    ],
    [FiscalStatuses.CANCELLED]: [
      { label: FiscalActionLabels.YEAR_END_UPDATE, action: FiscalActions.YEAR_END_UPDATE, requireSaved: true }
    ]
  };

  get allowedActions(): DropdownAction[] {
    return this.actionMap[this.status] || [];
  }

  isActionDisabled(action: DropdownAction): boolean {
    if (action.requireGuid && !this.fiscal?.projectPlanFiscalGuid) {
      return true;
    }
    if (action.requireSaved && this.isDirty) {
      return true;
    }
    return false;
  }

  getActionTooltip(action: DropdownAction): string {
    if (action.requireSaved && this.isDirty) {
      return 'Please save your changes to enable this action.';
    }
    return '';
  }

  emitAction(action: string) {
    this.actionSelected.emit({ action, index: this.index });
  }

  isButtonDisabled(): boolean {
    return this.isDisabled;
  }
}

