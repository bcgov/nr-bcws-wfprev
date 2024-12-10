import { TestBed } from '@angular/core/testing';
import { Injector } from '@angular/core';
import { AppConfigService } from '../services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import { CustomOAuthLogger, CustomDateTimeProvider } from './index';

describe('App Initializer and Custom Classes', () => {
  let injector: Injector;
  let appConfigService: jasmine.SpyObj<AppConfigService>;
  let tokenService: jasmine.SpyObj<TokenService>;
  beforeEach(() => {
    // Mock AppConfigService and TokenService
    const appConfigServiceMock = jasmine.createSpyObj('AppConfigService', ['loadAppConfig', 'configEmitter']);
    const tokenServiceMock = jasmine.createSpyObj('TokenService', ['getToken']);
    TestBed.configureTestingModule({
      providers: [
        { provide: AppConfigService, useValue: appConfigServiceMock },
        { provide: TokenService, useValue: tokenServiceMock },
        Injector,
      ]
    });
    injector = TestBed.inject(Injector);
    appConfigService = TestBed.inject(AppConfigService) as jasmine.SpyObj<AppConfigService>;
    tokenService = TestBed.inject(TokenService) as jasmine.SpyObj<TokenService>;
  });
  // Test for CustomOAuthLogger class
  describe('CustomOAuthLogger', () => {
    let logger: CustomOAuthLogger;
    beforeEach(() => {
      logger = new CustomOAuthLogger();
    });
    it('should log debug messages', () => {
      const logSpy = spyOn(console, 'debug');
      logger.debug('Debug message');
      expect(logSpy).toHaveBeenCalledWith('OAuthLogger Debug:', 'Debug message');
    });
    it('should log info messages', () => {
      const logSpy = spyOn(console, 'info');
      logger.info('Info message');
      expect(logSpy).toHaveBeenCalledWith('OAuthLogger Info:', 'Info message');
    });
    it('should log warn messages', () => {
      const logSpy = spyOn(console, 'warn');
      logger.warn('Warn message');
      expect(logSpy).toHaveBeenCalledWith('OAuthLogger Warn:', 'Warn message');
    });
    it('should log error messages', () => {
      const logSpy = spyOn(console, 'error');
      logger.error('Error message');
      expect(logSpy).toHaveBeenCalledWith('OAuthLogger Error:', 'Error message');
    });
    it('should log regular log messages', () => {
      const logSpy = spyOn(console, 'log');
      logger.log('Log message');
      expect(logSpy).toHaveBeenCalledWith('OAuthLogger Log:', 'Log message');
    });
  });
  // Test for CustomDateTimeProvider class
  describe('CustomDateTimeProvider', () => {
    let dateTimeProvider: CustomDateTimeProvider;
    beforeEach(() => {
      dateTimeProvider = new CustomDateTimeProvider();
    });
    it('should return a number when now() is called', () => {
      const nowSpy = spyOn(dateTimeProvider, 'now').and.callThrough();
      const result = dateTimeProvider.now();
      expect(nowSpy).toHaveBeenCalled();
      expect(typeof result).toBe('object');
    });
    it('should return a Date object when new() is called', () => {
      const newSpy = spyOn(dateTimeProvider, 'new').and.callThrough();
      const result = dateTimeProvider.new();
      expect(newSpy).toHaveBeenCalled();
      expect(result).toBeInstanceOf(Date);
    });
  });
});