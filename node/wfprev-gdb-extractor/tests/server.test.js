const request = require("supertest");
const path = require("path");
const fs = require("fs");
const mockFs = require("mock-fs");
const extract = require("extract-zip");
const gdal = require("gdal-async");

jest.mock("extract-zip");
jest.mock("gdal-async");

const { app } = require("../server"); // adjust to your file location

describe("POST /upload", () => {
  afterEach(() => {
    mockFs.restore();
    jest.resetAllMocks();
  });

  it("should return 400 if no file is uploaded", async () => {
    const res = await request(app).post("/upload");
    expect(res.statusCode).toBe(400);
    expect(res.text).toBe("No file uploaded.");
  });

  it("should return 400 if file is not a zip", async () => {
    const res = await request(app)
      .post("/upload")
      .attach("file", Buffer.from("dummy content"), "data.txt");

    expect(res.statusCode).toBe(400);
    expect(res.text).toBe("Only ZIP files are allowed.");
  });

  it("should return 500 if no .gdb folder is found", async () => {
    extract.mockImplementation(async () => {});
    mockFs({
      "/tmp/uploads": {
        "test.zip": "fake zip content",
        "test": { "not_a_gdb.txt": "data" }
      }
    });

    const res = await request(app)
      .post("/upload")
      .attach("file", Buffer.from("fake zip"), "test.zip");

    expect(res.statusCode).toBe(500);
    expect(res.text).toBe("No .gdb found.");
  });

  it("should return geometry data if GDB is valid", async () => {
    const mockFeature = {
      getGeometry: () => ({
        toJSON: () => JSON.stringify({ type: "Point", coordinates: [1, 2] })
      }),
    };

    const mockLayer = {
      features: [mockFeature, mockFeature],
    };

    const mockDataset = {
      layers: [mockLayer],
      close: jest.fn(),
    };

    gdal.open.mockReturnValue(mockDataset);

    extract.mockImplementation(async () => {});
    mockFs({
      "/tmp/uploads": {
        "test.zip": "fake zip content",
        "test": {
          "sample.gdb": {}
        }
      }
    });

    const res = await request(app)
      .post("/upload")
      .attach("file", Buffer.from("fake zip"), "test.zip");

    expect(res.statusCode).toBe(200);
    expect(res.body).toEqual([
      { type: "Point", coordinates: [1, 2] },
      { type: "Point", coordinates: [1, 2] }
    ]);
  });

  it("should return 500 if GDB reading fails", async () => {
    gdal.open.mockImplementation(() => {
      throw new Error("GDB read error");
    });

    extract.mockImplementation(async () => {});
    mockFs({
      "/tmp/uploads": {
        "test.zip": "fake zip content",
        "test": {
          "sample.gdb": {}
        }
      }
    });

    const res = await request(app)
      .post("/upload")
      .attach("file", Buffer.from("fake zip"), "test.zip");

    expect(res.statusCode).toBe(500);
    expect(res.text).toBe("GDB read error");
  });
});
