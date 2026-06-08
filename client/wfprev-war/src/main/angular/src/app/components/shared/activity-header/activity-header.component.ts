import { CommonModule } from '@angular/common';
import { Component, Input, ElementRef, AfterViewInit, OnDestroy, NgZone, ChangeDetectorRef, OnChanges, SimpleChanges } from '@angular/core';
import { ExpansionIndicatorComponent } from '../expansion-indicator/expansion-indicator.component';
import { StatusBadgeComponent } from '../status-badge/status-badge.component';

@Component({
  selector: 'wfprev-activity-header',
  standalone: true,
  imports: [CommonModule, ExpansionIndicatorComponent, StatusBadgeComponent],
  templateUrl: './activity-header.component.html',
  styleUrl: './activity-header.component.scss'
})
export class ActivityHeaderComponent implements AfterViewInit, OnDestroy, OnChanges {
  @Input() isExpanded: boolean = false;
  @Input() title: string = '';
  @Input() isSpatialAdded?: boolean;
  @Input() hasOutstandingObligations?: boolean;
  @Input() isCarryForward?: boolean;
  @Input() isResultsReportable?: boolean;
  @Input() statusCode?: string;
  @Input() backgroundColor?: string;
  @Input() isMissingInfo?: boolean;

  shouldStack = false;
  private resizeObserver?: ResizeObserver;
  private lastHorizontalWidth = 0;
  private lastUnstackedTitleWidth = 0;

  constructor(
    private readonly el: ElementRef,
    private readonly zone: NgZone,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['backgroundColor']) {
      this.applyBackgroundColor();
    }
  }

  ngAfterViewInit() {
    this.zone.runOutsideAngular(() => {
      this.resizeObserver = new ResizeObserver(() => {
        this.checkOverlap();
      });
      this.resizeObserver.observe(this.el.nativeElement);
    });
    this.applyBackgroundColor();
  }

  ngOnDestroy() {
    this.resizeObserver?.disconnect();
  }

  private applyBackgroundColor() {
    if (this.backgroundColor && this.el?.nativeElement) {
      const parent = this.el.nativeElement.closest('.mat-expansion-panel-header');
      if (parent) {
        parent.style.backgroundColor = this.backgroundColor;
      }
    }
  }

  checkOverlap() {
    const hostEl = this.el.nativeElement;
    const titleEl = hostEl.querySelector('.activity-title');
    const indicatorsEl = hostEl.querySelector('.activity-indicators');

    if (titleEl && indicatorsEl) {
      const hostWidth = hostEl.getBoundingClientRect().width;

      if (!this.shouldStack) {
        this.lastHorizontalWidth = indicatorsEl.getBoundingClientRect().width;
        this.lastUnstackedTitleWidth = titleEl.getBoundingClientRect().width;
      }

      const titleWidth = this.lastUnstackedTitleWidth || titleEl.getBoundingClientRect().width;
      const horizontalWidth = this.lastHorizontalWidth || indicatorsEl.getBoundingClientRect().width;

      const newShouldStack = (titleWidth + horizontalWidth + 80 > hostWidth);

      if (newShouldStack !== this.shouldStack) {
        this.shouldStack = newShouldStack;
        this.cdr.detectChanges();
      }
    }
  }
}
