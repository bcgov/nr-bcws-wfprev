const sinon = require('sinon');
const fs = require('fs');
const path = require('path');
const gdal = require('gdal-async');

// Mock extract-zip before importing it
jest.mock('extract-zip', () => jest.fn());
const extract = require('extract-zip');

// Import your server code
const server = require('../server');

describe('GDB Extractor Function Tests', () => {
  let req, res;

  beforeEach(() => {
    // Mock Express request and response objects
    req = {
      files: null
    };

    res = {
      status: jest.fn().mockReturnThis(),
      send: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis()
    };

    // Reset mocks before each test
    jest.clearAllMocks();
  });

  it('should return 400 when no file is uploaded', async () => {
    // Set up the request with no files
    req.files = null;

    // Call the upload handler directly
    await server.handleUpload(req, res);

    // Verify the response
    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.send).toHaveBeenCalledWith('No file uploaded.');
  });

  it('should return 400 when uploaded file has no GDB folder', async () => {
    // Set up the request with a mock file
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined)
      }
    };

    // Stub file system operations
    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);

    // Set up extract-zip mock to resolve
    extract.mockResolvedValue(undefined);

    // Stub fs.readdirSync to return a list without a .gdb file
    jest.spyOn(fs, 'readdirSync').mockReturnValue(['folder1', 'folder2']);

    // Call the upload handler directly
    await server.handleUpload(req, res);

    // Verify the response
    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.send).toHaveBeenCalledWith('No .gdb found.');
  });

  it('should extract and process GDB data successfully', async () => {
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined),
      },
    };
  
    // Fix the geometry mock to return a JSON string instead of an object
    const mockGeometry = {
      toJSON: () => JSON.stringify({ type: 'Point', coordinates: [1, 2] }),
    };
  
    const mockFeature = {
      getGeometry: () => mockGeometry,
    };
  
    const mockLayer = {
      features: [mockFeature, mockFeature],
    };
  
    const mockDataset = {
      layers: [mockLayer],
      close: jest.fn(),
    };
  
    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);
    jest.spyOn(fs, 'readdirSync').mockReturnValue(['test.gdb']);
  
    extract.mockResolvedValue(undefined);
    jest.spyOn(gdal, 'open').mockReturnValue(mockDataset);
    
    // Skip actual timeout execution
    jest.spyOn(global, 'setTimeout').mockImplementation((callback) => {
      // Don't actually run the callback to avoid cleanup code
      return 1;
    });
  
    await server.handleUpload(req, res);
  
    expect(res.json).toHaveBeenCalled();
    const responseData = res.json.mock.calls[0][0];
    expect(responseData).toBeInstanceOf(Array);
    expect(responseData).toHaveLength(2);
    expect(responseData[0]).toEqual({ type: 'Point', coordinates: [1, 2] });
  
    expect(mockDataset.close).toHaveBeenCalled();
  });
  

  it('should handle extraction failure gracefully', async () => {
    // Set up the request with a mock file
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined)
      }
    };

    // Stub file system operations
    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);

    // Stub the extract function to reject with an error
    const extractError = new Error('Extraction failed');
    extract.mockRejectedValue(extractError);

    // Call the upload handler directly
    await server.handleUpload(req, res);

    // Verify the response
    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.send).toHaveBeenCalledWith('Extraction failed.');
  });

  it('should handle GDAL errors gracefully', async () => {
    // Set up the request with a mock file
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined)
      }
    };

    // Stub file system operations
    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);
    jest.spyOn(fs, 'readdirSync').mockReturnValue(['test.gdb']);

    // Set up extract-zip mock to resolve
    extract.mockResolvedValue(undefined);

    // Mock GDAL open function to throw an error
    const gdalError = new Error('GDAL error');
    jest.spyOn(gdal, 'open').mockImplementation(() => {
      throw gdalError;
    });

    // Call the upload handler directly
    await server.handleUpload(req, res);

    // Verify the response
    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.send).toHaveBeenCalledWith('Failed to read GDB.');
  });

  it('should reject non-ZIP files', async () => {
    // Set up request with non-ZIP file
    req.files = {
      file: {
        name: 'test.txt',
        mv: jest.fn().mockResolvedValue(undefined)
      }
    };

    await server.handleUpload(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.send).toHaveBeenCalledWith('Only ZIP files are allowed.');
  });

  it('should reject paths attempting zip slip', async () => {
    req.files = {
      file: {
        name: '../malicious.zip', // Path traversal attempt
        mv: jest.fn().mockResolvedValue(undefined)
      }
    };

    await server.handleUpload(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.send).toHaveBeenCalledWith('Failed to read GDB.');
  });

  it('should handle zip slip attack attempts during extraction', async () => {
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined)
      }
    };

    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);

    // Mock extract to simulate a zip slip attack
    extract.mockImplementation((zipPath, options) => {
      // Trigger the onEntry callback with a malicious path
      options.onEntry({ fileName: '../../../etc/passwd' });
      return Promise.reject(new Error('Attempted zip slip attack with file: ../../../etc/passwd'));
    });

    await server.handleUpload(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.send).toHaveBeenCalledWith('Extraction failed.');
  });

  it('should handle errors when reading extracted files', async () => {
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined)
      }
    };

    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);

    // Extract succeeds
    extract.mockResolvedValue(undefined);

    // But reading the directory fails
    const readError = new Error('Failed to read directory');
    jest.spyOn(fs, 'readdirSync').mockImplementation(() => {
      throw readError;
    });

    await server.handleUpload(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.send).toHaveBeenCalledWith('Could not read extracted files.');
  });

  it('should trigger cleanup operations after successful processing', async () => {
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined),
      }
    };

    const mockGeometry = {
      toJSON: () => JSON.stringify({ type: 'Point', coordinates: [1, 2] }),
    };

    const mockFeature = {
      getGeometry: () => mockGeometry,
    };

    const mockLayer = {
      features: [mockFeature],
    };

    const mockDataset = {
      layers: [mockLayer],
      close: jest.fn(),
    };

    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);
    jest.spyOn(fs, 'readdirSync').mockReturnValue(['test.gdb']);
    jest.spyOn(fs, 'lstatSync').mockReturnValue({ isDirectory: () => false });
    jest.spyOn(fs, 'unlinkSync').mockReturnValue(undefined);
    jest.spyOn(fs, 'rmdirSync').mockReturnValue(undefined);

    extract.mockResolvedValue(undefined);
    jest.spyOn(gdal, 'open').mockReturnValue(mockDataset);

    // Capture the setTimeout call to test cleanup logic
    let cleanupCallback;
    jest.spyOn(global, 'setTimeout').mockImplementation((callback) => {
      cleanupCallback = callback;
      return 1;
    });

    await server.handleUpload(req, res);

    // Verify response
    expect(res.json).toHaveBeenCalled();

    // Now execute the cleanup callback manually to test the cleanup code
    cleanupCallback();

    // Verify cleanup operations were performed
    expect(fs.unlinkSync).toHaveBeenCalled(); // Verify zip deletion
    expect(fs.existsSync).toHaveBeenCalled(); // Verify directory existence check
  });

  it('should handle errors during dataset cleanup', async () => {
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined),
      }
    };

    // Create a mock dataset that throws an error when closed
    const mockDataset = {
      layers: [],
      close: jest.fn().mockImplementation(() => {
        throw new Error('Error closing dataset');
      })
    };

    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);
    jest.spyOn(fs, 'readdirSync').mockReturnValue(['test.gdb']);
    jest.spyOn(gdal, 'open').mockReturnValue(mockDataset);

    // Mock console.error to prevent actual logging during test
    jest.spyOn(console, 'error').mockImplementation(() => {});

    // Make GDAL throw an error after opening
    jest.spyOn(mockDataset.layers, 'forEach').mockImplementation(() => {
      throw new Error('GDAL processing error');
    });

    await server.handleUpload(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.send).toHaveBeenCalledWith('Failed to read GDB.');
    expect(mockDataset.close).toHaveBeenCalled();
    expect(console.error).toHaveBeenCalled();
  });

  it('should return nothing if feature has no geometry', async () => {
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined),
      }
    };

    // Feature without geometry
    const mockFeature = {
      getGeometry: () => null, // No geometry
    };

    const mockLayer = {
      features: [mockFeature],
    };

    const mockDataset = {
      layers: [mockLayer],
      close: jest.fn(),
    };

    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);
    jest.spyOn(fs, 'readdirSync').mockReturnValue(['test.gdb']);

    extract.mockResolvedValue(undefined);
    jest.spyOn(gdal, 'open').mockReturnValue(mockDataset);

    // Skip timeout
    jest.spyOn(global, 'setTimeout').mockImplementation(() => 1);

    await server.handleUpload(req, res);

    expect(res.json).toHaveBeenCalledWith([]);
    expect(mockDataset.close).toHaveBeenCalled();
  });
});