import { TestBed } from '@angular/core/testing';
import { CanDeactivateGuard, CanComponentDeactivate } from './can-deactive.guard';
import { Observable, of } from 'rxjs';

describe('CanDeactivateGuard', () => {
  let guard: CanDeactivateGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CanDeactivateGuard]
    });
    guard = TestBed.inject(CanDeactivateGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow deactivation when component is null', () => {
    expect(guard.canDeactivate(null as any)).toBe(true);
  });

  it('should allow deactivation when canDeactivate is not defined', () => {
    const mockComponent = {} as CanComponentDeactivate;
    expect(guard.canDeactivate(mockComponent)).toBe(true);
  });

  it('should allow deactivation when canDeactivate returns true', () => {
    const mockComponent: CanComponentDeactivate = {
      canDeactivate: () => true
    };
    expect(guard.canDeactivate(mockComponent)).toBe(true);
  });

  it('should block deactivation when canDeactivate returns false', () => {
    const mockComponent: CanComponentDeactivate = {
      canDeactivate: () => false
    };
    expect(guard.canDeactivate(mockComponent)).toBe(false);
  });

  it('should handle canDeactivate returning an Observable<boolean>', (done) => {
    const mockComponent: CanComponentDeactivate = {
      canDeactivate: () => of(false)
    };

    const result = guard.canDeactivate(mockComponent);
    if (result instanceof Observable) {
      result.subscribe((value: boolean) => {
        expect(value).toBe(false);
        done();
      });
    } else {
      fail('Expected an Observable but received something else');
    }
  });

  it('should handle canDeactivate returning a Promise<boolean>', async () => {
    const mockComponent: CanComponentDeactivate = {
      canDeactivate: () => Promise.resolve(true)
    };

    const result = guard.canDeactivate(mockComponent);
    if (result instanceof Promise) {
      await expectAsync(result).toBeResolvedTo(true);
    } else {
      fail('Expected a Promise but received something else');
    }
  });

  it('should handle canDeactivate returning a boolean', () => {
    const mockComponent: CanComponentDeactivate = {
      canDeactivate: () => true
    };

    const result = guard.canDeactivate(mockComponent);
    expect(result).toBe(true);
  });
});
