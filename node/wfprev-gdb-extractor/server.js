const express = require("express");
const fileUpload = require("express-fileupload");
const gdal = require("gdal-async");
const fs = require("fs");
const path = require("path");
const extract = require("extract-zip");
const cors = require("cors");
const rateLimit = require("express-rate-limit");
const awsServerlessExpressMiddleware = require("aws-serverless-express/middleware");

const app = express();
app.set("trust proxy", 1);
app.disable("x-powered-by");

app.use(awsServerlessExpressMiddleware.eventContext());

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100,
});
app.use(limiter);

app.use(cors({
  origin: [
    "http://localhost:4200",
    "https://wfprev-dev.nrs.gov.bc.ca",
    "https://wfprev-tst.nrs.gov.bc.ca",
    "https://wfprev.nrs.gov.bc.ca"
  ]
}));

// Decode base64 body if necessary (Lambda -> API Gateway)
app.use((req, res, next) => {
  const event = req.apiGateway?.event;
  if (event?.isBase64Encoded && event.body) {
    const buff = Buffer.from(event.body, "base64");
    req.body = buff;
    req.headers["content-length"] = buff.length;
  }
  next();
});

app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

app.use(fileUpload());

const uploadDir = "/tmp/uploads";
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

function isValidPath(filePath, destinationPath) {
  const normalizedPath = path.normalize(filePath);
  return normalizedPath.startsWith(destinationPath);
}

async function handleUpload(req, res) {
  if (!req.files || !req.files.file) {
    return res.status(400).send("No file uploaded.");
  }

  const fileName = req.files.file.name;
  if (!fileName.toLowerCase().endsWith(".zip")) {
    return res.status(400).send("Only ZIP files are allowed.");
  }

  const zipPath = path.join(uploadDir, fileName);
  const unzipPath = path.join(uploadDir, path.basename(fileName, ".zip"));

  await req.files.file.mv(zipPath);

  try {
    await extract(zipPath, {
      dir: unzipPath,
      onEntry: (entry) => {
        const destPath = path.join(unzipPath, entry.fileName);
        if (!isValidPath(destPath, unzipPath)) {
          throw new Error(`Zip slip detected: ${entry.fileName}`);
        }
      }
    });
  } catch (err) {
    console.error("Extraction failed:", err);
    try { fs.unlinkSync(zipPath); } catch (cleanupErr) {}
    return res.status(500).send("Extraction failed.");
  }

  let extractedFiles;
  try {
    extractedFiles = fs.readdirSync(unzipPath);
  } catch (err) {
    console.error("Failed to read extracted dir:", err);
    return res.status(500).send("Could not read extracted files.");
  }

  const gdbFolder = extractedFiles.find(f => f.endsWith(".gdb"));
  if (!gdbFolder) {
    return res.status(400).send("No .gdb found.");
  }

  const gdbPath = path.join(unzipPath, gdbFolder);

  let dataset;
  const results = [];

  try {
    dataset = gdal.open(gdbPath);
    dataset.layers.forEach((layer) => {
      layer.features.forEach((feature) => {
        const geom = feature.getGeometry();
        if (geom) results.push(JSON.parse(geom.toJSON()));
      });
    });

    dataset.close();
    dataset = null;

    res.json(results);

    // Post-response cleanup
    setTimeout(() => {
      try {
        if (fs.existsSync(zipPath)) fs.unlinkSync(zipPath);

        const deleteFolderRecursive = (dirPath) => {
          if (fs.existsSync(dirPath)) {
            fs.readdirSync(dirPath).forEach((file) => {
              const curPath = path.join(dirPath, file);
              if (fs.lstatSync(curPath).isDirectory()) {
                deleteFolderRecursive(curPath);
              } else {
                fs.unlinkSync(curPath);
              }
            });
            fs.rmdirSync(dirPath);
          }
        };
        deleteFolderRecursive(unzipPath);
      } catch (cleanupErr) {
        console.error("Cleanup failed:", cleanupErr);
      }
    }, 2000);
  } catch (err) {
    console.error("Error reading GDB:", err);
    if (dataset) {
      try {
        dataset.close();
      } catch (_) {}
    }
    return res.status(500).send("Failed to read GDB.");
  }
}

app.post("/upload", handleUpload);

if (require.main === module) {
  app.listen(3000, () => console.log("Server running on port 3000"));
}

module.exports = {
  app,
  handleUpload
};
