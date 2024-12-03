import { TestBed } from '@angular/core/testing';
import { LibraryConfig } from './library-config';

describe('LibraryConfig', () => {
  let libraryConfig: LibraryConfig;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LibraryConfig],
    });
    libraryConfig = TestBed.inject(LibraryConfig);
  });

  it('should be created', () => {
    expect(libraryConfig).toBeTruthy();
  });

  it('should initialize configurationPath to an empty string', () => {
    expect(libraryConfig.configurationPath).toBe('');
  });

  it('should allow configurationPath to be updated', () => {
    const newPath = '/new/config/path';
    libraryConfig.configurationPath = newPath;
    expect(libraryConfig.configurationPath).toBe(newPath);
  });
});