import { TestBed } from '@angular/core/testing';
import { PermissionsService, WFPREV_ACTIONS } from './permissions.service';
import { TokenService } from './token.service';

class MockTokenService {}

const VIEWER_SCOPES = [
  WFPREV_ACTIONS.GET_TOPLEVEL,
  WFPREV_ACTIONS.GET_PREVENTION_PROJECT,
  WFPREV_ACTIONS.GET_SPATIAL,
  WFPREV_ACTIONS.EXPORT_TO_RESULTS,
];

const EDITOR_SCOPES = [
  WFPREV_ACTIONS.GET_TOPLEVEL,
  WFPREV_ACTIONS.GET_PREVENTION_PROJECT,
  WFPREV_ACTIONS.CREATE_PREVENTION_PROJECT,
  WFPREV_ACTIONS.UPDATE_PREVENTION_PROJECT,
  WFPREV_ACTIONS.CREATE_PREVENTION_FISCAL,
  WFPREV_ACTIONS.UPDATE_PREVENTION_FISCAL,
  WFPREV_ACTIONS.CREATE_PREVENTION_ACTIVITY,
  WFPREV_ACTIONS.UPDATE_PREVENTION_ACTIVITY,
  WFPREV_ACTIONS.CREATE_PERFORMANCE_UPDATE,
  WFPREV_ACTIONS.CREATE_SPATIAL_UPLOAD,
  WFPREV_ACTIONS.UPDATE_SPATIAL_METADATA,
  WFPREV_ACTIONS.GET_SPATIAL,
  WFPREV_ACTIONS.EXPORT_TO_RESULTS,
  WFPREV_ACTIONS.CREATE_EVALUATION_CRITERIA,
  WFPREV_ACTIONS.UPDATE_EVALUATION_CRITERIA,
  WFPREV_ACTIONS.CREATE_YEAR_END_REPORT,
  WFPREV_ACTIONS.UPDATE_YEAR_END_REPORT,
  WFPREV_ACTIONS.CREATE_ACTIVITY_ATTACHMENT,
  WFPREV_ACTIONS.UPDATE_ACTIVITY_ATTACHMENT,
];

const ADMIN_SCOPES = [
  WFPREV_ACTIONS.GET_TOPLEVEL,
  WFPREV_ACTIONS.GET_PREVENTION_PROJECT,
  WFPREV_ACTIONS.CREATE_PREVENTION_PROJECT,
  WFPREV_ACTIONS.UPDATE_PREVENTION_PROJECT,
  WFPREV_ACTIONS.DELETE_PREVENTION_PROJECT,
  WFPREV_ACTIONS.CREATE_PREVENTION_FISCAL,
  WFPREV_ACTIONS.UPDATE_PREVENTION_FISCAL,
  WFPREV_ACTIONS.DELETE_PREVENTION_FISCAL,
  WFPREV_ACTIONS.CREATE_PREVENTION_ACTIVITY,
  WFPREV_ACTIONS.UPDATE_PREVENTION_ACTIVITY,
  WFPREV_ACTIONS.DELETE_PREVENTION_ACTIVITY,
  WFPREV_ACTIONS.CREATE_PERFORMANCE_UPDATE,
  WFPREV_ACTIONS.CREATE_SPATIAL_UPLOAD,
  WFPREV_ACTIONS.DELETE_SPATIAL_UPLOAD,
  WFPREV_ACTIONS.UPDATE_SPATIAL_METADATA,
  WFPREV_ACTIONS.GET_SPATIAL,
  WFPREV_ACTIONS.EXPORT_TO_RESULTS,
  WFPREV_ACTIONS.CREATE_EVALUATION_CRITERIA,
  WFPREV_ACTIONS.UPDATE_EVALUATION_CRITERIA,
  WFPREV_ACTIONS.DELETE_EVALUATION_CRITERIA,
  WFPREV_ACTIONS.CREATE_YEAR_END_REPORT,
  WFPREV_ACTIONS.UPDATE_YEAR_END_REPORT,
  WFPREV_ACTIONS.DELETE_YEAR_END_REPORT,
  WFPREV_ACTIONS.CREATE_ACTIVITY_ATTACHMENT,
  WFPREV_ACTIONS.UPDATE_ACTIVITY_ATTACHMENT,
  WFPREV_ACTIONS.DELETE_ACTIVITY_ATTACHMENT,
];

fdescribe('PermissionsService', () => {
  let service: PermissionsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PermissionsService,
        { provide: TokenService, useClass: MockTokenService },
      ],
    });
    service = TestBed.inject(PermissionsService);
  });

  describe('initial state', () => {
    it('should have no scopes', () => {
      expect(service.canCreateProject()).toBeFalse();
      expect(service.canEditProject()).toBeFalse();
      expect(service.canDeleteProject()).toBeFalse();
      expect(service.canExportList()).toBeFalse();
      expect(service.canCreateFiscal()).toBeFalse();
      expect(service.canUpdateFiscal()).toBeFalse();
      expect(service.canDeleteFiscal()).toBeFalse();
      expect(service.canCreateActivity()).toBeFalse();
      expect(service.canUpdateActivity()).toBeFalse();
      expect(service.canDeleteActivity()).toBeFalse();
      expect(service.canCreatePerformanceUpdate()).toBeFalse();
      expect(service.canViewSpatial()).toBeFalse();
      expect(service.canUploadSpatial()).toBeFalse();
      expect(service.canDeleteSpatial()).toBeFalse();
      expect(service.canUpdateSpatialMetadata()).toBeFalse();
      expect(service.canExportToResults()).toBeFalse();
      expect(service.canCreateEvaluationCriteria()).toBeFalse();
      expect(service.canUpdateEvaluationCriteria()).toBeFalse();
      expect(service.canDeleteEvaluationCriteria()).toBeFalse();
      expect(service.canCreateYearEndReport()).toBeFalse();
      expect(service.canUpdateYearEndReport()).toBeFalse();
      expect(service.canDeleteYearEndReport()).toBeFalse();
      expect(service.canCreateActivityAttachment()).toBeFalse();
      expect(service.canUpdateActivityAttachment()).toBeFalse();
      expect(service.canDeleteActivityAttachment()).toBeFalse();
    });
  });

  describe('loadFromCredentials', () => {
    it('should handle an array scope claim', () => {
      service.loadFromCredentials({ scope: VIEWER_SCOPES });
      expect(service.canExportList()).toBeTrue();
      expect(service.canViewSpatial()).toBeTrue();
      expect(service.canExportToResults()).toBeTrue();
      expect(service.canCreateProject()).toBeFalse();
    });

    it('should handle a space-delimited string scope claim', () => {
      service.loadFromCredentials({ scope: VIEWER_SCOPES.join(' ') });
      expect(service.canExportList()).toBeTrue();
      expect(service.canCreateProject()).toBeFalse();
    });

    it('should handle missing scope gracefully', () => {
      service.loadFromCredentials({});
      expect(service.canCreateProject()).toBeFalse();
    });

    it('should handle null tokenDetails gracefully', () => {
      service.loadFromCredentials(null);
      expect(service.canCreateProject()).toBeFalse();
    });
  });

  describe('clearScopes', () => {
    it('should clear all permissions', () => {
      service.loadFromCredentials({ scope: ADMIN_SCOPES });
      expect(service.canCreateProject()).toBeTrue();
      service.clearScopes();
      expect(service.canCreateProject()).toBeFalse();
    });
  });

  describe('WFPREV-VIEWER profile', () => {
    beforeEach(() => service.loadFromCredentials({ scope: VIEWER_SCOPES }));

    it('can view project list', () => expect(service.canExportList()).toBeTrue());
    it('can view spatial', () => expect(service.canViewSpatial()).toBeTrue());
    it('can export to results', () => expect(service.canExportToResults()).toBeTrue());
    it('cannot create project', () => expect(service.canCreateProject()).toBeFalse());
    it('cannot edit project', () => expect(service.canEditProject()).toBeFalse());
    it('cannot delete project', () => expect(service.canDeleteProject()).toBeFalse());
    it('cannot create fiscal', () => expect(service.canCreateFiscal()).toBeFalse());
    it('cannot create activity', () => expect(service.canCreateActivity()).toBeFalse());
    it('cannot upload spatial', () => expect(service.canUploadSpatial()).toBeFalse());
    it('cannot create evaluation criteria', () => expect(service.canCreateEvaluationCriteria()).toBeFalse());
    it('cannot create year end report', () => expect(service.canCreateYearEndReport()).toBeFalse());
    it('cannot create activity attachment', () => expect(service.canCreateActivityAttachment()).toBeFalse());
  });

  describe('WFPREV-EDITOR profile', () => {
    beforeEach(() => service.loadFromCredentials({ scope: EDITOR_SCOPES }));

    it('can create project', () => expect(service.canCreateProject()).toBeTrue());
    it('can edit project', () => expect(service.canEditProject()).toBeTrue());
    it('can create fiscal', () => expect(service.canCreateFiscal()).toBeTrue());
    it('can update fiscal', () => expect(service.canUpdateFiscal()).toBeTrue());
    it('can create activity', () => expect(service.canCreateActivity()).toBeTrue());
    it('can update activity', () => expect(service.canUpdateActivity()).toBeTrue());
    it('can create performance update', () => expect(service.canCreatePerformanceUpdate()).toBeTrue());
    it('can upload spatial', () => expect(service.canUploadSpatial()).toBeTrue());
    it('can update spatial metadata', () => expect(service.canUpdateSpatialMetadata()).toBeTrue());
    it('can create evaluation criteria', () => expect(service.canCreateEvaluationCriteria()).toBeTrue());
    it('can update evaluation criteria', () => expect(service.canUpdateEvaluationCriteria()).toBeTrue());
    it('can create year end report', () => expect(service.canCreateYearEndReport()).toBeTrue());
    it('can create activity attachment', () => expect(service.canCreateActivityAttachment()).toBeTrue());
    it('cannot delete project', () => expect(service.canDeleteProject()).toBeFalse());
    it('cannot delete fiscal', () => expect(service.canDeleteFiscal()).toBeFalse());
    it('cannot delete activity', () => expect(service.canDeleteActivity()).toBeFalse());
    it('cannot delete spatial', () => expect(service.canDeleteSpatial()).toBeFalse());
    it('cannot delete evaluation criteria', () => expect(service.canDeleteEvaluationCriteria()).toBeFalse());
    it('cannot delete year end report', () => expect(service.canDeleteYearEndReport()).toBeFalse());
    it('cannot delete activity attachment', () => expect(service.canDeleteActivityAttachment()).toBeFalse());
  });

  describe('WFPREV-ADMIN profile', () => {
    beforeEach(() => service.loadFromCredentials({ scope: ADMIN_SCOPES }));

    it('can create project', () => expect(service.canCreateProject()).toBeTrue());
    it('can edit project', () => expect(service.canEditProject()).toBeTrue());
    it('can delete project', () => expect(service.canDeleteProject()).toBeTrue());
    it('can delete fiscal', () => expect(service.canDeleteFiscal()).toBeTrue());
    it('can delete activity', () => expect(service.canDeleteActivity()).toBeTrue());
    it('can delete spatial', () => expect(service.canDeleteSpatial()).toBeTrue());
    it('can delete evaluation criteria', () => expect(service.canDeleteEvaluationCriteria()).toBeTrue());
    it('can delete year end report', () => expect(service.canDeleteYearEndReport()).toBeTrue());
    it('can delete activity attachment', () => expect(service.canDeleteActivityAttachment()).toBeTrue());
    it('can update year end report', () => expect(service.canUpdateYearEndReport()).toBeTrue());
    it('can update activity attachment', () => expect(service.canUpdateActivityAttachment()).toBeTrue());
  });
});