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
  shouldWrapBelow = false;
  private resizeObserver?: ResizeObserver;
  private static readonly STACK_BUFFER = 40;
  private static readonly UNSTACK_MARGIN = 60;

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
      // Watch the header sections so badge changes (e.g. toggling Carry Forward) re-trigger the check without needing to resize the window
      const indicatorsEl = this.el.nativeElement.querySelector('.activity-indicators');
      const headerLeftEl = this.el.nativeElement.querySelector('.header-left');
      if (indicatorsEl) this.resizeObserver.observe(indicatorsEl);
      if (headerLeftEl) this.resizeObserver.observe(headerLeftEl);
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
    const host = this.el.nativeElement;
    const headerLeft = host.querySelector('.header-left');
    const title = host.querySelector('.activity-title');
    const indicators = host.querySelector('.activity-indicators');
    if (!headerLeft || !title || !indicators) return;

    const hostWidth = host.getBoundingClientRect().width;
    // 8 and 12 are the flex gap values from .header-left and .activity-indicators  
    // scrollWidth doesn't include gaps, so naturalRowWidth adds them back
    const leftWidth = this.naturalRowWidth(headerLeft, 8);
    const rowWidth = this.naturalRowWidth(indicators, 12);
    const widestBadge = this.widestChild(indicators);
    const estimated = leftWidth + rowWidth + ActivityHeaderComponent.STACK_BUFFER;

    let shouldStack: boolean;
    if (this.shouldStack) {
      // Already stacked: only unstack once there's clearly enough room,
      // so the layout doesn't flip back and forth at the boundary
      shouldStack = estimated + ActivityHeaderComponent.UNSTACK_MARGIN > hostWidth;
    } else {
      // Not stacked: the live layout tells us directly if the title is clipped
      const titleClipped = title.scrollWidth > title.clientWidth;
      shouldStack = estimated > hostWidth || titleClipped;
    }

    const shouldWrapBelow = shouldStack
      && leftWidth + widestBadge + ActivityHeaderComponent.STACK_BUFFER > hostWidth;

    if (shouldStack !== this.shouldStack || shouldWrapBelow !== this.shouldWrapBelow) {
      this.shouldStack = shouldStack;
      this.shouldWrapBelow = shouldWrapBelow;
      this.zone.run(() => this.cdr.detectChanges());
    }
}

  // Sum of children's natural widths plus the flex gaps between them,
  // i.e. the width the row would need if nothing was squeezed
  private naturalRowWidth(el: HTMLElement, gap: number): number {
    const children = Array.from(el.children) as HTMLElement[];
    if (!children.length) return 0;
    return children.reduce((sum, c) => sum + c.scrollWidth, 0) + gap * (children.length - 1);
  }

  // Widest single child - the column width when badges are stacked vertically
  private widestChild(el: HTMLElement): number {
    return Math.max(0, ...Array.from(el.children).map(c => (c as HTMLElement).scrollWidth));
  }
}
