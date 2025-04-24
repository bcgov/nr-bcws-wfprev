const request = require("supertest");
const fs = require("fs");
const extract = require("extract-zip");

jest.mock("gdal-async", () => ({
  open: jest.fn().mockReturnValue({
    layers: [{
      features: [{
        getGeometry: () => ({
          toJSON: () => '{"type":"Point","coordinates":[1,1]}'
        })
      }]
    }],
    close: jest.fn()
  })
}));

jest.mock("fs");
jest.mock("extract-zip", () => jest.fn(() => Promise.resolve()));

const gdal = require("gdal-async");
const { app, extractAndParseGDB, handleUpload } = require("../server");

describe("Server Module", () => {
  beforeEach(() => {
    jest.clearAllMocks();

    fs.existsSync.mockReturnValue(true);
    fs.mkdirSync.mockImplementation(() => {});
    fs.writeFileSync.mockImplementation(() => {});
    fs.readdirSync.mockReturnValue(["test.gdb"]);
    fs.unlinkSync.mockImplementation(() => {});
    fs.lstatSync.mockReturnValue({ isDirectory: () => false });
    fs.rmdirSync.mockImplementation(() => {});
  });

  describe("POST /upload", () => {
    test("should return 400 for missing file", async () => {
      const res = await request(app).post("/upload");
      expect(res.status).toBe(400);
      expect(res.text).toBe("No file uploaded.");
    });

    test("should return 400 for non-ZIP file", async () => {
      const res = await request(app)
        .post("/upload")
        .attach("file", Buffer.from("not a zip"), "file.txt");
      expect(res.status).toBe(400);
      expect(res.text).toBe("Only ZIP files are allowed.");
    });

    test("should extract GDB and return parsed GeoJSON", async () => {
      const res = await request(app)
        .post("/upload")
        .attach("file", Buffer.from("mock zip"), "test.zip");
      expect(res.status).toBe(200);
      expect(res.body).toEqual([{ type: "Point", coordinates: [1, 1] }]);
    });

    test("should handle ZIP slip attack", async () => {
      extract.mockImplementationOnce((_, opts) => {
        opts.onEntry({ fileName: "../malicious.txt" });
        return Promise.resolve();
      });

      const res = await request(app)
        .post("/upload")
        .attach("file", Buffer.from("mock zip"), "test.zip");
      expect(res.status).toBe(500);
      expect(res.text).toContain("Zip slip detected");
    });

    test("should handle extract error", async () => {
      extract.mockImplementationOnce(() => { throw new Error("Extract failed"); });

      const res = await request(app)
        .post("/upload")
        .attach("file", Buffer.from("zip content"), "test.zip");
      expect(res.status).toBe(500);
      expect(res.text).toBe("Extract failed");
    });
  });

  describe("extractAndParseGDB", () => {
    test("should parse GDB and return GeoJSON", async () => {
      const buf = Buffer.from("zip content");
      const result = await extractAndParseGDB(buf, "sample.zip");
      expect(result).toEqual([{ type: "Point", coordinates: [1, 1] }]);
      expect(gdal.open).toHaveBeenCalled();
    });

    test("should throw error if .gdb folder is missing", async () => {
      fs.readdirSync.mockReturnValueOnce([]);
      await expect(extractAndParseGDB(Buffer.from("zip"), "sample.zip"))
        .rejects.toThrow("No .gdb found.");
    });
  });
});
