import { Injectable, Signal, computed, inject, signal } from '@angular/core';
import { TokenService } from './token.service';

export const WFPREV_ACTIONS = {
  GET_TOPLEVEL:                 'WFPREV.GET_TOPLEVEL',
  GET_PREVENTION_PROJECT:       'WFPREV.GET_PREVENTION_PROJECT',
  CREATE_PREVENTION_PROJECT:    'WFPREV.CREATE_PREVENTION_PROJECT',
  UPDATE_PREVENTION_PROJECT:    'WFPREV.UPDATE_PREVENTION_PROJECT',
  DELETE_PREVENTION_PROJECT:    'WFPREV.DELETE_PREVENTION_PROJECT',
  CREATE_PREVENTION_FISCAL:     'WFPREV.CREATE_PREVENTION_FISCAL',
  UPDATE_PREVENTION_FISCAL:     'WFPREV.UPDATE_PREVENTION_FISCAL',
  DELETE_PREVENTION_FISCAL:     'WFPREV.DELETE_PREVENTION_FISCAL',
  CREATE_PREVENTION_ACTIVITY:   'WFPREV.CREATE_PREVENTION_ACTIVITY',
  UPDATE_PREVENTION_ACTIVITY:   'WFPREV.UPDATE_PREVENTION_ACTIVITY',
  DELETE_PREVENTION_ACTIVITY:   'WFPREV.DELETE_PREVENTION_ACTIVITY',
  CREATE_PERFORMANCE_UPDATE:    'WFPREV.CREATE_PERFORMANCE_UPDATE',
  CREATE_SPATIAL_UPLOAD:        'WFPREV.CREATE_SPATIAL_UPLOAD',
  DELETE_SPATIAL_UPLOAD:        'WFPREV.DELETE_SPATIAL_UPLOAD',
  UPDATE_SPATIAL_METADATA:      'WFPREV.UPDATE_SPATIAL_METADATA',
  GET_SPATIAL:                  'WFPREV.GET_SPATIAL',
  EXPORT_TO_RESULTS:            'WFPREV.EXPORT_TO_RESULTS',
  CREATE_EVALUATION_CRITERIA:   'WFPREV.CREATE_EVALUATION_CRITERIA',
  UPDATE_EVALUATION_CRITERIA:   'WFPREV.UPDATE_EVALUATION_CRITERIA',
  DELETE_EVALUATION_CRITERIA:   'WFPREV.DELETE_EVALUATION_CRITERIA',
  CREATE_YEAR_END_REPORT:       'WFPREV.CREATE_YEAR_END_REPORT',
  UPDATE_YEAR_END_REPORT:       'WFPREV.UPDATE_YEAR_END_REPORT',
  DELETE_YEAR_END_REPORT:       'WFPREV.DELETE_YEAR_END_REPORT',
  CREATE_ACTIVITY_ATTACHMENT:   'WFPREV.CREATE_ACTIVITY_ATTACHMENT',
  UPDATE_ACTIVITY_ATTACHMENT:   'WFPREV.UPDATE_ACTIVITY_ATTACHMENT',
  DELETE_ACTIVITY_ATTACHMENT:   'WFPREV.DELETE_ACTIVITY_ATTACHMENT',
} as const;

export type WfprevAction = (typeof WFPREV_ACTIONS)[keyof typeof WFPREV_ACTIONS];

@Injectable({ providedIn: 'root' })
export class PermissionsService {
  private readonly tokenService = inject(TokenService);
  private readonly scopes = signal<ReadonlySet<string>>(new Set());

  loadFromCredentials(tokenDetails: any): void {
    const raw: unknown = tokenDetails?.scope;
    let scopes: string[];
    if (Array.isArray(raw)) {
      scopes = raw;
    } else if (typeof raw === 'string') {
      scopes = raw.split(/\s+/).filter(Boolean);
    } else {
      scopes = [];
    }
    this.scopes.set(new Set(scopes));
  }

  clearScopes(): void {
    this.scopes.set(new Set());
  }

  readonly canCreateProject: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.CREATE_PREVENTION_PROJECT)
  );

  readonly canEditProject: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.UPDATE_PREVENTION_PROJECT)
  );

  readonly canDeleteProject: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.DELETE_PREVENTION_PROJECT)
  );

  readonly canExportList: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.GET_PREVENTION_PROJECT)
  );

  readonly canCreateFiscal: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.CREATE_PREVENTION_FISCAL)
  );

  readonly canUpdateFiscal: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.UPDATE_PREVENTION_FISCAL)
  );

  readonly canDeleteFiscal: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.DELETE_PREVENTION_FISCAL)
  );

  readonly canCreateActivity: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.CREATE_PREVENTION_ACTIVITY)
  );

  readonly canUpdateActivity: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.UPDATE_PREVENTION_ACTIVITY)
  );

  readonly canDeleteActivity: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.DELETE_PREVENTION_ACTIVITY)
  );

  readonly canCreatePerformanceUpdate: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.CREATE_PERFORMANCE_UPDATE)
  );

  readonly canViewSpatial: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.GET_SPATIAL)
  );

  readonly canUploadSpatial: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.CREATE_SPATIAL_UPLOAD)
  );

  readonly canDeleteSpatial: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.DELETE_SPATIAL_UPLOAD)
  );

  readonly canUpdateSpatialMetadata: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.UPDATE_SPATIAL_METADATA)
  );

  readonly canExportToResults: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.EXPORT_TO_RESULTS)
  );

  readonly canCreateEvaluationCriteria: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.CREATE_EVALUATION_CRITERIA)
  );

  readonly canUpdateEvaluationCriteria: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.UPDATE_EVALUATION_CRITERIA)
  );

  readonly canDeleteEvaluationCriteria: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.DELETE_EVALUATION_CRITERIA)
  );

  readonly canCreateYearEndReport: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.CREATE_YEAR_END_REPORT)
  );

  readonly canUpdateYearEndReport: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.UPDATE_YEAR_END_REPORT)
  );

  readonly canDeleteYearEndReport: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.DELETE_YEAR_END_REPORT)
  );

  readonly canCreateActivityAttachment: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.CREATE_ACTIVITY_ATTACHMENT)
  );

  readonly canUpdateActivityAttachment: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.UPDATE_ACTIVITY_ATTACHMENT)
  );

  readonly canDeleteActivityAttachment: Signal<boolean> = computed(() =>
    this.scopes().has(WFPREV_ACTIONS.DELETE_ACTIVITY_ATTACHMENT)
  );
}