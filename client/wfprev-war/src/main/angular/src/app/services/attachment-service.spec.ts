import { TestBed } from '@angular/core/testing';
import { AttachmentService } from './attachment-service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfigService } from './app-config.service';
import { TokenService } from './token.service';
import { FileAttachment } from '../components/models';

const mockApplicationConfig = {
    application: {
        baseUrl: 'http://test.com',
        lazyAuthenticate: false,
        enableLocalStorageToken: true,
        acronym: 'TEST',
        environment: 'DEV',
        version: '1.0.0',
    },
    webade: {
        oauth2Url: 'http://oauth.test',
        clientId: 'test-client',
        authScopes: 'TEST.*',
    },
    rest: {
        wfprev: 'http://test.com/api',
    },
    mapServices: {
      geoserverBaseUrl: 'http://geoserver.test',
      wfnewsBaseUrl: 'http://wfnews.test'
    }
};

const attachment: FileAttachment = {
    fileAttachmentGuid: '12345',
    sourceObjectNameCode: { sourceObjectNameCode: 'PROJECT' },
    sourceObjectUniqueId: 'unique-id-123',
    documentPath: '/attachments/file.txt',
    fileIdentifier: 'file-123',
    wildfireYear: 2025,
    attachmentContentTypeCode: { attachmentContentTypeCode: 'CODE' },
    attachmentDescription: 'Test file attachment',
    attachmentReadOnlyInd: false,
    createDate: new Date().toISOString(),
};


describe('AttachmentService', () => {
    let service: AttachmentService;
    let httpMock: HttpTestingController;
    let mockAppConfigService: jasmine.SpyObj<AppConfigService>;
    let mockTokenService: jasmine.SpyObj<TokenService>;

    beforeEach(() => {
        const appConfigSpy = jasmine.createSpyObj('AppConfigService', ['getConfig']);
        const tokenSpy = jasmine.createSpyObj('TokenService', ['getOauthToken']);

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                AttachmentService,
                { provide: AppConfigService, useValue: appConfigSpy },
                { provide: TokenService, useValue: tokenSpy },
            ],
        });

        service = TestBed.inject(AttachmentService);
        httpMock = TestBed.inject(HttpTestingController);
        mockAppConfigService = TestBed.inject(AppConfigService) as jasmine.SpyObj<AppConfigService>;
        mockTokenService = TestBed.inject(TokenService) as jasmine.SpyObj<TokenService>;

        // Use mockApplicationConfig instead of manually setting values
        mockAppConfigService.getConfig.and.returnValue(mockApplicationConfig);
        mockTokenService.getOauthToken.and.returnValue('mock-token');
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should send a POST request to create an attachment', () => {
        const projectGuid = 'test-guid';

        service.createProjectAttachment(projectGuid, attachment).subscribe(response => {
            expect(response).toEqual({ success: true });
        });

        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/attachments`);

        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(attachment);
        expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');

        req.flush({ success: true }); // Mock API response
    });

    it('should handle errors correctly', () => {
        const projectGuid = 'test-guid';
        const expectedUrl = `${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/attachments`;

        service.createProjectAttachment(projectGuid, attachment).subscribe({
            next: () => fail('Expected an error'),
            error: (error) => {
                expect(error.message).toBe('Failed to create project attachment');
            }
        });

        const req = httpMock.expectOne(expectedUrl);

        req.flush('Error', { status: 500, statusText: 'Internal Server Error' });

    });

    it('should send a GET request to fetch project attachments', () => {
        const projectGuid = 'test-guid';
        const mockResponse = [{ fileAttachmentGuid: '12345', attachmentDescription: 'Test file' }];
        
        service.getProjectAttachments(projectGuid).subscribe(response => {
            expect(response).toEqual(mockResponse);
        });
    
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/attachments`);
    
        expect(req.request.method).toBe('GET');
        expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    
        req.flush(mockResponse); 
    });

    it('should handle errors correctly when fetching project attachments', () => {
        const projectGuid = 'test-guid';
    
        service.getProjectAttachments(projectGuid).subscribe({
            next: () => fail('Expected an error'),
            error: (error) => {
                expect(error.message).toBe('Failed to fetch project attachments');
            }
        });
    
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/attachments`);
    
        req.flush('Error', { status: 500, statusText: 'Internal Server Error' });
    });

    it('should send a DELETE request to delete a project attachment', () => {
        const projectGuid = 'test-guid';
        const attachmentGuid = '12345';
    
        service.deleteProjectAttachment(projectGuid, attachmentGuid).subscribe(response => {
            expect(response).toEqual({ success: true });
        });
    
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/attachments/${attachmentGuid}`);
    
        expect(req.request.method).toBe('DELETE');
        expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    
        req.flush({ success: true }); // Mock API response
    });

    it('should handle errors correctly when deleting project attachment', () => {
        const projectGuid = 'test-guid';
        const attachmentGuid = '12345';
    
        service.deleteProjectAttachment(projectGuid, attachmentGuid).subscribe({
            next: () => fail('Expected an error'),
            error: (error) => {
                expect(error.message).toBe('Failed to delete project attachment');
            }
        });
    
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/attachments/${attachmentGuid}`);
    
        req.flush('Error', { status: 500, statusText: 'Internal Server Error' });
    });

    it('should create an activity attachment', () => {
        const projectGuid = 'proj-001';
        const fiscalGuid = 'fiscal-001';
        const activityGuid = 'activity-001';
      
        service.createActivityAttachment(projectGuid, fiscalGuid, activityGuid, attachment).subscribe((response) => {
          expect(response).toEqual({ success: true });
        });
      
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/attachments`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(attachment);
        expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
        req.flush({ success: true });
      });
      
      it('should handle errors when creating an activity attachment', () => {
        const projectGuid = 'proj-001';
        const fiscalGuid = 'fiscal-001';
        const activityGuid = 'activity-001';
      
        service.createActivityAttachment(projectGuid, fiscalGuid, activityGuid, attachment).subscribe({
          next: () => fail('Expected error'),
          error: (err) => {
            expect(err.message).toBe('Failed to create activity attachment');
          }
        });
      
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/attachments`);
        req.flush('Error', { status: 500, statusText: 'Internal Server Error' });
      });
      
      it('should fetch activity attachments', () => {
        const projectGuid = 'proj-001';
        const fiscalGuid = 'fiscal-001';
        const activityGuid = 'activity-001';
        const mockResponse = [{ fileAttachmentGuid: '12345', attachmentDescription: 'Activity file' }];
      
        service.getActivityAttachments(projectGuid, fiscalGuid, activityGuid).subscribe(response => {
          expect(response).toEqual(mockResponse);
        });
      
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/attachments`);
        expect(req.request.method).toBe('GET');
        expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
        req.flush(mockResponse);
      });
      
      it('should handle errors when fetching activity attachments', () => {
        const projectGuid = 'proj-001';
        const fiscalGuid = 'fiscal-001';
        const activityGuid = 'activity-001';
      
        service.getActivityAttachments(projectGuid, fiscalGuid, activityGuid).subscribe({
          next: () => fail('Expected error'),
          error: (err) => {
            expect(err.message).toBe('Failed to fetch activity attachments');
          }
        });
      
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/attachments`);
        req.flush('Error', { status: 500, statusText: 'Internal Server Error' });
      });
      
      it('should delete an activity attachment', () => {
        const projectGuid = 'proj-001';
        const fiscalGuid = 'fiscal-001';
        const activityGuid = 'activity-001';
        const fileGuid = 'file-789';
      
        service.deleteActivityAttachments(projectGuid, fiscalGuid, activityGuid, fileGuid).subscribe(response => {
          expect(response).toEqual({ success: true });
        });
      
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/attachments/${fileGuid}`);
        expect(req.request.method).toBe('DELETE');
        expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
        req.flush({ success: true });
      });
      
      it('should handle errors when deleting activity attachment', () => {
        const projectGuid = 'proj-001';
        const fiscalGuid = 'fiscal-001';
        const activityGuid = 'activity-001';
        const fileGuid = 'file-789';
      
        service.deleteActivityAttachments(projectGuid, fiscalGuid, activityGuid, fileGuid).subscribe({
          next: () => fail('Expected error'),
          error: (err) => {
            expect(err.message).toBe('Failed to delete activity attachments');
          }
        });
      
        const req = httpMock.expectOne(`${mockApplicationConfig.rest.wfprev}/wfprev-api/projects/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/attachments/${fileGuid}`);
        req.flush('Error', { status: 500, statusText: 'Internal Server Error' });
      });
      
});
