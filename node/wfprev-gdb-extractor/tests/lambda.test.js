const { handler } = require("../lambda");
const { extractAndParseGDB } = require("../server");

jest.mock("../server", () => ({
  extractAndParseGDB: jest.fn()
}));

describe("Lambda Handler", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("should return 400 if 'file' is missing", async () => {
    const event = {};
    const res = await handler(event);
    expect(res.statusCode).toBe(400);
    expect(JSON.parse(res.body)).toEqual({ error: "Missing 'file' in payload" });
  });

  test("should return 200 with extracted geometries", async () => {
    const mockData = [{ type: "Point", coordinates: [1, 1] }];
    extractAndParseGDB.mockResolvedValueOnce(mockData);

    const base64 = Buffer.from("zip content").toString("base64");
    const event = { file: base64 };
    const res = await handler(event);

    expect(res.statusCode).toBe(200);
    expect(JSON.parse(res.body)).toEqual(mockData);
    expect(extractAndParseGDB).toHaveBeenCalled();
  });

  test("should return 500 on processing error", async () => {
    extractAndParseGDB.mockRejectedValueOnce(new Error("Parse failed"));
    const base64 = Buffer.from("zip content").toString("base64");

    const res = await handler({ file: base64 });

    expect(res.statusCode).toBe(500);
    const body = JSON.parse(res.body);
    expect(body.error).toBe("Failed to process GDB");
    expect(body.message).toBe("Parse failed");
  });
});
