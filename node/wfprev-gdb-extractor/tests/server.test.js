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
    // Set up the request with a mock file
    req.files = {
      file: {
        name: 'test.zip',
        mv: jest.fn().mockResolvedValue(undefined)
      }
    };

    // Mock the GDAL dataset and features
    const mockGeometry = {
      toJSON: () => ({ type: 'Point', coordinates: [1, 2] })
    };

    const mockFeature = {
      getGeometry: () => mockGeometry
    };

    const mockLayer = {
      features: [mockFeature, mockFeature]
    };

    const mockDataset = {
      layers: [mockLayer],
      close: jest.fn()
    };

    // Stub file system operations
    jest.spyOn(fs, 'existsSync').mockReturnValue(true);
    jest.spyOn(fs, 'mkdirSync').mockReturnValue(undefined);
    jest.spyOn(fs, 'readdirSync').mockReturnValue(['test.gdb']);

    // Set up extract-zip mock to resolve
    extract.mockResolvedValue(undefined);

    // Mock GDAL open function
    jest.spyOn(gdal, 'open').mockReturnValue(mockDataset);

    // Stub setTimeout to execute immediately
    jest.spyOn(global, 'setTimeout').mockImplementation((callback) => {
      callback();
      return 1; // Return a timeout ID
    });

    // Custom fs.rmdirSync and fs.unlinkSync to prevent actual file deletion
    jest.spyOn(fs, 'rmdirSync').mockReturnValue(undefined);
    jest.spyOn(fs, 'unlinkSync').mockReturnValue(undefined);
    jest.spyOn(fs, 'lstatSync').mockReturnValue({
      isDirectory: () => false
    });

    // Call the upload handler directly
    await server.handleUpload(req, res);

    // Verify the response
    expect(res.json).toHaveBeenCalled();
    const responseData = res.json.mock.calls[0][0];
    expect(responseData).toBeInstanceOf(Array);
    expect(responseData).toHaveLength(2);
    expect(responseData[0]).toEqual({ type: 'Point', coordinates: [1, 2] });

    // Verify that the dataset was closed
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
});