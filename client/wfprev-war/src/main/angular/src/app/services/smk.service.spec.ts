import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { SmkService } from './smk.service';

describe('SmkService', () => {
  let service: SmkService;
  let include: jasmine.Spy & { option: jasmine.Spy };
  const fakeSmk = { TYPE: {} };
  const originalSmk = (window as any).SMK;

  beforeEach(() => {
    include = Object.assign(jasmine.createSpy('include').and.returnValue(Promise.resolve()), {
      option: jasmine.createSpy('option')
    });
    (window as any).include = include;
    (window as any).SMK = fakeSmk;
    TestBed.configureTestingModule({});
    service = TestBed.inject(SmkService);
  });

  afterEach(() => {
    delete (window as any).include;
    (window as any).SMK = originalSmk;
  });

  it('should load SMK\'s Leaflet viewer modules through its own loader, with no map', async () => {
    const SMK = await service.load();

    expect(include.option).toHaveBeenCalledWith({ baseUrl: `${service.baseUrl}assets/src/` });
    expect(include.calls.allArgs()).toEqual([['smk-map'], ['viewer-leaflet']]);
    expect(SMK).toBe(fakeSmk);
  });

  it('should load once, however many callers ask', async () => {
    await Promise.all([service.load(), service.load()]);
    await service.load();

    expect(include).toHaveBeenCalledTimes(2);
  });

  it('should try again after a failed load', async () => {
    include.and.returnValues(Promise.reject(new Error('offline')), Promise.resolve(), Promise.resolve());

    await expectAsync(service.load()).toBeRejected();
    await expectAsync(service.load()).toBeResolvedTo(fakeSmk);
  });

  it('should preload when the browser is idle', () => {
    const idle = spyOn(window, 'requestIdleCallback').and.callFake((callback: IdleRequestCallback) => {
      callback({} as IdleDeadline);
      return 1;
    });
    const load = spyOn(service, 'load').and.returnValue(Promise.resolve(fakeSmk));

    service.preload();

    expect(idle).toHaveBeenCalled();
    expect(load).toHaveBeenCalled();
  });

  it('should only warn if the preload fails', fakeAsync(() => {
    spyOn(window, 'requestIdleCallback').and.callFake((callback: IdleRequestCallback) => {
      callback({} as IdleDeadline);
      return 1;
    });
    spyOn(service, 'load').and.returnValue(Promise.reject(new Error('offline')));
    const warn = spyOn(console, 'warn');

    service.preload();
    tick();

    expect(warn).toHaveBeenCalledWith('SMK preload failed', jasmine.any(Error));
  }));
});
