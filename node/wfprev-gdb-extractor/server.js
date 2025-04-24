const express = require("express");
const sanitizeFilename = require("sanitize-filename");
const fileUpload = require("express-fileupload");
const gdal = require("gdal-async");
const fs = require("fs");
const path = require("path");
const extract = require("extract-zip");
const rateLimit = require("express-rate-limit");
const tmp = require("tmp");

const app = express();
app.set("trust proxy", 1);
app.disable("x-powered-by");

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100,
});
app.use(limiter);
app.use(fileUpload());

function isValidPath(filePath, destinationPath) {
  const normalizedPath = path.normalize(filePath);
  return normalizedPath.startsWith(destinationPath);
}

async function extractAndParseGDB(zipBuffer, zipFileName = "upload.zip") {
  const tmpDir = tmp.dirSync({ unsafeCleanup: true });
  const zipPath = path.join(tmpDir.name, zipFileName);
  const unzipPath = path.join(tmpDir.name, path.basename(zipFileName, ".zip"));

  fs.writeFileSync(zipPath, zipBuffer);

  await extract(zipPath, {
    dir: unzipPath,
    onEntry: (entry) => {
      const destPath = path.join(unzipPath, entry.fileName);
      if (!isValidPath(destPath, unzipPath)) {
        throw new Error(`Zip slip detected: ${entry.fileName}`);
      }
    },
  });

  const files = fs.readdirSync(unzipPath);
  const gdbFolder = files.find((f) => f.endsWith(".gdb"));
  if (!gdbFolder) throw new Error("No .gdb found.");

  const gdbPath = path.join(unzipPath, gdbFolder);
  const dataset = gdal.open(gdbPath);
  const results = [];

  dataset.layers.forEach((layer) => {
    layer.features.forEach((feature) => {
      const geom = feature.getGeometry();
      if (geom) results.push(JSON.parse(geom.toJSON()));
    });
  });

  dataset.close();

  // Clean up tmp files
  tmpDir.removeCallback();

  return results;
}

async function handleUpload(req, res) {
  if (!req.files || !req.files.file) {
    return res.status(400).send("No file uploaded.");
  }

  const fileName = sanitizeFilename(path.basename(req.files.file.name));
  if (!fileName.toLowerCase().endsWith(".zip")) {
    return res.status(400).send("Only ZIP files are allowed.");
  }

  try {
    const results = await extractAndParseGDB(req.files.file.data, fileName);
    res.json(results);
  } catch (err) {
    console.error("Upload error:", err);
    res.status(500).send(err.message || "Processing failed.");
  }
}

app.post("/upload", handleUpload);

if (require.main === module) {
  app.listen(3000, () => console.log("Server running on port 3000"));
}

module.exports = {
  app,
  handleUpload,
  extractAndParseGDB,
};
