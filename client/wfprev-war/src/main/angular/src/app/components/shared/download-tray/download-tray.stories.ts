import { computed, importProvidersFrom, signal } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Meta, StoryObj, applicationConfig, moduleMetadata } from '@storybook/angular';
import { ReportJob } from 'src/app/components/models';
import { DownloadTrayService, TrayState, groupExports } from 'src/app/services/download-tray.service';
import { DownloadTrayComponent } from './download-tray.component';

const now = Date.now();
const minutesAgo = (minutes: number) => new Date(now - minutes * 60000).toISOString();

function job(overrides: Partial<ReportJob>): ReportJob {
  return {
    jobGuid: Math.random().toString(36).slice(2),
    exportGroupGuid: 'group',
    reportType: 'RESULTS_XLSX',
    fileName: 'ReMi_RESULTS.xlsx',
    description: 'Fire centres: Coastal Fire Centre\nFiscal years: FY 2026/27',
    status: 'READY',
    expired: false,
    downloaded: false,
    reference: '7F3A91C2',
    requestTimestamp: minutesAgo(2),
    ...overrides
  };
}

/** A stand-in for the tray's service, holding a fixed list of jobs. */
function trayWith(jobs: ReportJob[], state: TrayState, expanded = true) {
  const all = signal(jobs);
  const log = (name: string) => (...args: unknown[]) => console.log(`tray.${name}`, ...args);
  return {
    jobs: all,
    visible: signal(true),
    expanded: signal(expanded),
    announcement: signal(''),
    exports: computed(() => groupExports(all())),
    state: signal<TrayState>(state),
    hasFinished: computed(() => all().some(j => j.status !== 'PREPARING')),
    init: () => undefined,
    save: log('save'),
    retry: log('retry'),
    runAgain: log('runAgain'),
    clearFinished: log('clearFinished'),
    toggleExpanded: log('toggleExpanded'),
    hide: log('hide')
  };
}

const inContext: ReportJob[] = [
  job({ exportGroupGuid: 'a', status: 'READY', requestTimestamp: minutesAgo(2) }),
  job({ exportGroupGuid: 'a', reportType: 'RESULTS_SPATIAL', fileName: 'ReMi_RESULTS_Spatial.zip', status: 'PREPARING', requestTimestamp: minutesAgo(2) }),
  job({
    exportGroupGuid: 'b', reportType: 'PROJECT_XLSX', fileName: 'ReMi_Fiscal.xlsx', status: 'READY', downloaded: true,
    description: 'Fire centres: Kamloops Fire Centre\nFiscal years: FY 2025/26', requestTimestamp: minutesAgo(29)
  }),
  job({
    exportGroupGuid: 'c', status: 'READY', downloaded: true, requestTimestamp: minutesAgo(190),
    description: 'Search: fuel break\nFire centres: Northwest Fire Centre, Prince George Fire Centre, Cariboo Fire Centre\nFiscal years: FY 2025/26, FY 2026/27\nFiscal statuses: Draft, Proposed\nProject types: Fuel Management'
  }),
  job({
    exportGroupGuid: 'c', reportType: 'RESULTS_SPATIAL', fileName: 'ReMi_RESULTS_Spatial.zip', status: 'FAILED', retryable: true,
    errorCode: 'TEMPORARY', errorMessage: 'Something went wrong. Try again.', requestTimestamp: minutesAgo(190),
    description: 'Search: fuel break\nFire centres: Northwest Fire Centre, Prince George Fire Centre, Cariboo Fire Centre\nFiscal years: FY 2025/26, FY 2026/27\nFiscal statuses: Draft, Proposed\nProject types: Fuel Management'
  })
];

const everyState: ReportJob[] = [
  job({ exportGroupGuid: 'p', reportType: 'PROJECT_CSV', fileName: 'ReMi_Fiscal.zip', status: 'PREPARING', description: 'Fire centres: All\nFiscal years: FY 2026/27', requestTimestamp: minutesAgo(1) }),
  job({ exportGroupGuid: 'r', reportType: 'PROJECT_XLSX', fileName: 'ReMi_Fiscal.xlsx', status: 'READY', description: 'Fire centres: Cariboo Fire Centre\nFiscal years: FY 2026/27', requestTimestamp: minutesAgo(20) }),
  job({ exportGroupGuid: 'n', status: 'READY', downloaded: true, description: 'Fire centres: Prince George Fire Centre\nFiscal years: FY 2026/27', requestTimestamp: minutesAgo(40) }),
  job({ exportGroupGuid: 'n', reportType: 'RESULTS_SPATIAL', fileName: 'ReMi_RESULTS_Spatial.zip', status: 'NO_FILES', description: 'Fire centres: Prince George Fire Centre\nFiscal years: FY 2026/27', requestTimestamp: minutesAgo(40) }),
  job({
    exportGroupGuid: 'f', reportType: 'PROJECT_CSV', fileName: 'ReMi_Fiscal.zip', status: 'FAILED', retryable: false, errorCode: 'DATA',
    errorMessage: 'No fiscal data matches these filters.', description: 'Fire centres: Southeast Fire Centre\nFiscal years: FY 2025/26', requestTimestamp: minutesAgo(120)
  }),
  job({ exportGroupGuid: 'x', status: 'READY', expired: true, description: 'Fire centres: Coastal Fire Centre\nFiscal years: FY 2025/26', requestTimestamp: minutesAgo(26 * 60) })
];

export default {
  title: 'Components/DownloadTray',
  component: DownloadTrayComponent,
  decorators: [
    applicationConfig({ providers: [importProvidersFrom(NoopAnimationsModule)] }),
    moduleMetadata({ imports: [DownloadTrayComponent] })
  ],
  parameters: { layout: 'fullscreen' }
} as Meta<DownloadTrayComponent>;

type Story = StoryObj<DownloadTrayComponent>;

export const InContext: Story = {
  decorators: [moduleMetadata({ providers: [{ provide: DownloadTrayService, useValue: trayWith(inContext, 'preparing') }] })]
};

export const EveryState: Story = {
  decorators: [moduleMetadata({ providers: [{ provide: DownloadTrayService, useValue: trayWith(everyState, 'preparing') }] })]
};

export const Collapsed: Story = {
  decorators: [moduleMetadata({ providers: [{ provide: DownloadTrayService, useValue: trayWith(inContext, 'ready', false) }] })]
};
