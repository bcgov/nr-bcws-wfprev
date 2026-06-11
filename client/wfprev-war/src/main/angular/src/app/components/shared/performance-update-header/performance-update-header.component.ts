import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, Input, NgZone, AfterViewInit, OnDestroy } from '@angular/core';
import { ForecastStatus, Option, PerformanceUpdateExtended, ProgressStatus, ReportingPeriod, UpdateGeneralStatus } from '../../models';
import { ExpansionIndicatorComponent } from '../expansion-indicator/expansion-indicator.component';
import { StatusBadgeComponent } from '../status-badge/status-badge.component';

@Component({
  selector: 'wfprev-performance-update-header',
  standalone: true,
  imports: [CommonModule, ExpansionIndicatorComponent, StatusBadgeComponent],
  templateUrl: './performance-update-header.component.html',
  styleUrl: './performance-update-header.component.scss',
})
export class PerformanceUpdateHeaderComponent implements AfterViewInit, OnDestroy {
  @Input() update!: PerformanceUpdateExtended;
  @Input() isExpanded = false;

  shouldStack = false;
  private resizeObserver?: ResizeObserver;
  private stackedAtWidth?: number;
  private static readonly STACK_BUFFER = 40;
  private static readonly UNSTACK_MARGIN = 60;

  protected readonly ForecastStatus = ForecastStatus;
  protected readonly ProgressStatus = ProgressStatus;
  protected readonly UpdateGeneralStatus = UpdateGeneralStatus;

  protected readonly delayedProgressStatus: Option<ProgressStatus> = { value: ProgressStatus.Delayed, description: 'Delayed' }
  protected readonly onTrackProgressStatus: Option<ProgressStatus> = { value: ProgressStatus.OnTrack, description: 'On track' }
  protected readonly deferredProgressStatus: Option<ProgressStatus> = { value: ProgressStatus.Deferred, description: 'Deferred' }
  protected readonly cancelledProgressStatus: Option<ProgressStatus> = { value: ProgressStatus.Cancelled, description: 'Cancelled' }

  protected readonly other: Option<ReportingPeriod> = { value: ReportingPeriod.Custom, description: 'Other' }
  protected readonly march7: Option<ReportingPeriod> = { value: ReportingPeriod.March7, description: 'March 7' }
  protected readonly q1: Option<ReportingPeriod> = { value: ReportingPeriod.Q1, description: 'End of Q1' }
  protected readonly q2: Option<ReportingPeriod> = { value: ReportingPeriod.Q2, description: 'End of Q2' }
  protected readonly q3: Option<ReportingPeriod> = { value: ReportingPeriod.Q3, description: 'End of Q3' }

  protected readonly cancelledUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Cancelled, description: 'Cancelled' }
  protected readonly completeUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Complete, description: 'Complete' }
  protected readonly draftUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Draft, description: 'Draft' }
  protected readonly inProgressUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.InProgress, description: 'In Progress' }
  protected readonly preparedUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Prepared, description: 'Prepared' }
  protected readonly proposedUpdateGeneralStatus: Option<UpdateGeneralStatus> = { value: UpdateGeneralStatus.Proposed, description: 'Proposed' }

  constructor(
    private readonly el: ElementRef,
    private readonly zone: NgZone,
    private readonly cdr: ChangeDetectorRef
  ) { }

  ngAfterViewInit() {
    this.zone.runOutsideAngular(() => {
      this.resizeObserver = new ResizeObserver(() => {
        this.checkOverlap();
      });
      this.resizeObserver.observe(this.el.nativeElement);
      // Watch the header sections so badge changes re-trigger the check without needing to resize the window
      const indicatorsEl = this.el.nativeElement.querySelector('.header-indicators-group');
      const headerLeftEl = this.el.nativeElement.querySelector('.header-left');
      if (indicatorsEl) this.resizeObserver.observe(indicatorsEl);
      if (headerLeftEl) this.resizeObserver.observe(headerLeftEl);
    });
  }

  ngOnDestroy() {
    this.resizeObserver?.disconnect();
  }

  checkOverlap() {
    const host = this.el.nativeElement as HTMLElement;
    const headerLeft = host.querySelector('.header-left') as HTMLElement;
    const indicators = host.querySelector('.header-indicators-group') as HTMLElement;
    if (!headerLeft || !indicators) return;

    const hostWidth = host.getBoundingClientRect().width;
    // 20 is the flex gap value from .header-left and .header-indicators-group
    // scrollWidth doesn't include gaps, so naturalRowWidth adds them back
    const leftWidth = this.naturalRowWidth(headerLeft, 20);
    const rowWidth = this.naturalRowWidth(indicators, 20);
    const estimated = leftWidth + rowWidth + PerformanceUpdateHeaderComponent.STACK_BUFFER;

    let shouldStack: boolean;
    if (this.shouldStack) {
      // Already stacked: only unstack once there's clearly enough room,
      // so the layout doesn't flip back and forth at the boundary
      shouldStack = hostWidth < (this.stackedAtWidth ?? hostWidth) + PerformanceUpdateHeaderComponent.UNSTACK_MARGIN;
    } else {
      // Not stacked: stack if the estimated natural width exceeds available space
      shouldStack = estimated > hostWidth;
      if (shouldStack) this.stackedAtWidth = hostWidth;
    }

    if (shouldStack === this.shouldStack) return;

    this.shouldStack = shouldStack;
    this.zone.run(() => this.cdr.detectChanges());
  }

  // Sum of children's natural widths plus the flex gaps between them,
  // i.e. the width the row would need if nothing was squeezed
  private naturalRowWidth(el: HTMLElement, gap: number): number {
    const children = Array.from(el.children) as HTMLElement[];
    if (!children.length) return 0;
    return children.reduce((sum, c) => sum + c.scrollWidth, 0) + gap * (children.length - 1);
  }
}