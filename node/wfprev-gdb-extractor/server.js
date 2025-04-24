const express = require("express");
const fileUpload = require("express-fileupload");
const gdal = require("gdal-async");
const fs = require("fs");
const path = require("path");
const extract = require("extract-zip");
const rateLimit = require("express-rate-limit");

const app = express();
app.set("trust proxy", 1);
app.disable("x-powered-by");

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100
});
app.use(limiter);

app.use(fileUpload());

const uploadDir = "/tmp/uploads";
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

function isValidPath(filePath, destinationPath) {
  const normalizedPath = path.normalize(filePath);
  return normalizedPath.startsWith(destinationPath);
}

async function extractAndParseGDB(zipBuffer, zipFileName = "upload.zip") {
  const zipPath = path.join(uploadDir, zipFileName);
  const unzipPath = path.join(uploadDir, path.basename(zipFileName, ".zip"));

  // Ensure upload directory exists
  if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir, { recursive: true });
  }

  // Write the buffer to a file
  fs.writeFileSync(zipPath, zipBuffer);

  // Extract the zip file
  await extract(zipPath, {
    dir: unzipPath,
    onEntry: (entry) => {
      const destPath = path.join(unzipPath, entry.fileName);
      if (!isValidPath(destPath, unzipPath)) {
        throw new Error(`Zip slip detected: ${entry.fileName}`);
      }
    }
  });

  // Find the .gdb folder
  const files = fs.readdirSync(unzipPath);
  const gdbFolder = files.find(f => f.endsWith(".gdb"));
  if (!gdbFolder) throw new Error("No .gdb found.");

  // Parse the GDB
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

  // Clean up
  try { fs.unlinkSync(zipPath); } catch (_) {}
  try {
    const deleteRecursive = (dir) => {
      if (fs.existsSync(dir)) {
        fs.readdirSync(dir).forEach((file) => {
          const curPath = path.join(dir, file);
          if (fs.lstatSync(curPath).isDirectory()) {
            deleteRecursive(curPath);
          } else {
            fs.unlinkSync(curPath);
          }
        });
        fs.rmdirSync(dir);
      }
    };
    deleteRecursive(unzipPath);
  } catch (err) {
    console.error("Cleanup error:", err);
  }

  return results;
}

async function handleUpload(req, res) {
  if (!req.files || !req.files.file) {
    return res.status(400).send("No file uploaded.");
  }

  const fileName = req.files.file.name;
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
  extractAndParseGDB 
};