import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { getFiscalYearDisplay, PlanFiscalStatusIcons } from 'src/app/utils/tools';
import { CodeTableKeys, PlanFiscalStatus } from 'src/app/utils/constants';

@Component({
  selector: 'wfprev-fiscal-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fiscal-card.component.html',
  styleUrls: ['./fiscal-card.component.scss']
})
export class FiscalCardComponent {
  @Input() fiscal: any;
  @Input() getDescription!: (key: string, value: any) => string | null;
  readonly PlanFiscalStatus = PlanFiscalStatus;

  readonly CodeTableKeys = CodeTableKeys;
  getFiscalYearDisplay = getFiscalYearDisplay;

  formatValue(val: number | undefined, suffix = ''): string {
    if (val == null) return '';
    const formatted = (val % 1 === 0)
      ? val.toLocaleString(undefined, { minimumFractionDigits: 0 })
      : val.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    return `${formatted}${suffix}`;
  }

  formatCurrency(val: number | undefined): string {
    return val != null ? `$${this.formatValue(val)}` : '';
  }

  getStatusIcon(statusCode: string) {
    return PlanFiscalStatusIcons[statusCode];
  }
}
