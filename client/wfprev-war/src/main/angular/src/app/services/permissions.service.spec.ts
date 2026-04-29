import { TestBed } from '@angular/core/testing';
import { PermissionsService, WFPREV_ACTIONS } from './permissions.service';
import { TokenService } from './token.service';

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

describe('PermissionsService', () => {
  let service: PermissionsService;
  let mockTokenService: { doesUserHaveApplicationPermissions: jasmine.Spy };
  let activeScopes: string[];

  beforeEach(() => {
    activeScopes = [];
    mockTokenService = {
      doesUserHaveApplicationPermissions: jasmine.createSpy('doesUserHaveApplicationPermissions')
        .and.callFake((scopes: string[]) =>
          scopes.every(s => activeScopes.includes(s))
        ),
    };

    TestBed.configureTestingModule({
      providers: [
        PermissionsService,
        { provide: TokenService, useValue: mockTokenService },
      ],
    });
    service = TestBed.inject(PermissionsService);
  });

  describe('initial state', () => {
    it('should return false for all actions when no scopes are loaded', () => {
      expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_PROJECT)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.UPDATE_PREVENTION_PROJECT)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_PROJECT)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.GET_PREVENTION_PROJECT)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_FISCAL)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.UPDATE_PREVENTION_FISCAL)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_FISCAL)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_ACTIVITY)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.UPDATE_PREVENTION_ACTIVITY)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_ACTIVITY)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.CREATE_PERFORMANCE_UPDATE)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.GET_SPATIAL)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.CREATE_SPATIAL_UPLOAD)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.DELETE_SPATIAL_UPLOAD)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.UPDATE_SPATIAL_METADATA)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.EXPORT_TO_RESULTS)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.CREATE_EVALUATION_CRITERIA)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.UPDATE_EVALUATION_CRITERIA)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.DELETE_EVALUATION_CRITERIA)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.CREATE_YEAR_END_REPORT)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.UPDATE_YEAR_END_REPORT)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.DELETE_YEAR_END_REPORT)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.CREATE_ACTIVITY_ATTACHMENT)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.UPDATE_ACTIVITY_ATTACHMENT)).toBeFalse();
      expect(service.hasAction(WFPREV_ACTIONS.DELETE_ACTIVITY_ATTACHMENT)).toBeFalse();
    });
  });

  describe('WFPREV-VIEWER profile', () => {
    beforeEach(() => activeScopes = [...VIEWER_SCOPES]);

    it('can view project list', () => expect(service.hasAction(WFPREV_ACTIONS.GET_PREVENTION_PROJECT)).toBeTrue());
    it('can view spatial', () => expect(service.hasAction(WFPREV_ACTIONS.GET_SPATIAL)).toBeTrue());
    it('can export to results', () => expect(service.hasAction(WFPREV_ACTIONS.EXPORT_TO_RESULTS)).toBeTrue());
    it('cannot create project', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_PROJECT)).toBeFalse());
    it('cannot edit project', () => expect(service.hasAction(WFPREV_ACTIONS.UPDATE_PREVENTION_PROJECT)).toBeFalse());
    it('cannot delete project', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_PROJECT)).toBeFalse());
    it('cannot create fiscal', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_FISCAL)).toBeFalse());
    it('cannot create activity', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_ACTIVITY)).toBeFalse());
    it('cannot upload spatial', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_SPATIAL_UPLOAD)).toBeFalse());
    it('cannot create evaluation criteria', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_EVALUATION_CRITERIA)).toBeFalse());
    it('cannot create year end report', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_YEAR_END_REPORT)).toBeFalse());
    it('cannot create activity attachment', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_ACTIVITY_ATTACHMENT)).toBeFalse());
  });

  describe('WFPREV-EDITOR profile', () => {
    beforeEach(() => activeScopes = [...EDITOR_SCOPES]);

    it('can create project', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_PROJECT)).toBeTrue());
    it('can edit project', () => expect(service.hasAction(WFPREV_ACTIONS.UPDATE_PREVENTION_PROJECT)).toBeTrue());
    it('can create fiscal', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_FISCAL)).toBeTrue());
    it('can update fiscal', () => expect(service.hasAction(WFPREV_ACTIONS.UPDATE_PREVENTION_FISCAL)).toBeTrue());
    it('can create activity', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_ACTIVITY)).toBeTrue());
    it('can update activity', () => expect(service.hasAction(WFPREV_ACTIONS.UPDATE_PREVENTION_ACTIVITY)).toBeTrue());
    it('can create performance update', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_PERFORMANCE_UPDATE)).toBeTrue());
    it('can upload spatial', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_SPATIAL_UPLOAD)).toBeTrue());
    it('can update spatial metadata', () => expect(service.hasAction(WFPREV_ACTIONS.UPDATE_SPATIAL_METADATA)).toBeTrue());
    it('can create evaluation criteria', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_EVALUATION_CRITERIA)).toBeTrue());
    it('can update evaluation criteria', () => expect(service.hasAction(WFPREV_ACTIONS.UPDATE_EVALUATION_CRITERIA)).toBeTrue());
    it('can create year end report', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_YEAR_END_REPORT)).toBeTrue());
    it('can create activity attachment', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_ACTIVITY_ATTACHMENT)).toBeTrue());
    it('cannot delete project', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_PROJECT)).toBeFalse());
    it('cannot delete fiscal', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_FISCAL)).toBeFalse());
    it('cannot delete activity', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_ACTIVITY)).toBeFalse());
    it('cannot delete spatial', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_SPATIAL_UPLOAD)).toBeFalse());
    it('cannot delete evaluation criteria', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_EVALUATION_CRITERIA)).toBeFalse());
    it('cannot delete year end report', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_YEAR_END_REPORT)).toBeFalse());
    it('cannot delete activity attachment', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_ACTIVITY_ATTACHMENT)).toBeFalse());
  });

  describe('WFPREV-ADMIN profile', () => {
    beforeEach(() => activeScopes = [...ADMIN_SCOPES]);

    it('can create project', () => expect(service.hasAction(WFPREV_ACTIONS.CREATE_PREVENTION_PROJECT)).toBeTrue());
    it('can edit project', () => expect(service.hasAction(WFPREV_ACTIONS.UPDATE_PREVENTION_PROJECT)).toBeTrue());
    it('can delete project', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_PROJECT)).toBeTrue());
    it('can delete fiscal', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_FISCAL)).toBeTrue());
    it('can delete activity', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_PREVENTION_ACTIVITY)).toBeTrue());
    it('can delete spatial', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_SPATIAL_UPLOAD)).toBeTrue());
    it('can delete evaluation criteria', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_EVALUATION_CRITERIA)).toBeTrue());
    it('can delete year end report', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_YEAR_END_REPORT)).toBeTrue());
    it('can delete activity attachment', () => expect(service.hasAction(WFPREV_ACTIONS.DELETE_ACTIVITY_ATTACHMENT)).toBeTrue());
    it('can update year end report', () => expect(service.hasAction(WFPREV_ACTIONS.UPDATE_YEAR_END_REPORT)).toBeTrue());
    it('can update activity attachment', () => expect(service.hasAction(WFPREV_ACTIONS.UPDATE_ACTIVITY_ATTACHMENT)).toBeTrue());
  });
});