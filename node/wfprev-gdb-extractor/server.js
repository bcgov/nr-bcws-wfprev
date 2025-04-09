const express = require("express");
const fileUpload = require("express-fileupload");
const gdal = require("gdal-async");
const fs = require("fs");
const path = require("path");
const extract = require("extract-zip");
const cors = require('cors');
const rateLimit = require('express-rate-limit');

const app = express();

// Hide X-Powered-By header to prevent Express version disclosure
app.disable('x-powered-by');

const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100 // limit each IP to 100 requests per windowMs
});

app.use(limiter);
app.use(fileUpload());

// Ensure 'uploads' directory exists
if (!fs.existsSync("uploads")) fs.mkdirSync("uploads");

// Function to validate file path (prevent zip slip vulnerability)
function isValidPath(filePath, destinationPath) {
    const normalizedPath = path.normalize(filePath);
    // Check if the normalized path attempts to navigate outside the destination directory
    return normalizedPath.startsWith(destinationPath);
}

// Extract the handler function so it can be tested independently
async function handleUpload(req, res) {
    if (!req.files || !req.files.file) {
        return res.status(400).send("No file uploaded.");
    }

    // Validate file extension
    const fileName = req.files.file.name;
    if (!fileName.toLowerCase().endsWith('.zip')) {
        return res.status(400).send("Only ZIP files are allowed.");
    }

    let zipPath = path.join(__dirname, "uploads", fileName);
    const unzipPath = path.resolve(__dirname, "uploads", path.basename(fileName, '.zip'));

    // Validate zipPath to ensure it is within the uploads directory
    zipPath = path.resolve(zipPath);
    if (!zipPath.startsWith(path.resolve(__dirname, "uploads"))) {
        return res.status(400).send("Invalid file path.");
    }
    
    await req.files.file.mv(zipPath);

    try {
        // Use onEntry callback to validate each file path before extraction
        await extract(zipPath, { 
            dir: unzipPath,
            onEntry: (entry) => {
                const destPath = path.join(unzipPath, entry.fileName);
                // Validate path to prevent zip slip attack
                if (!isValidPath(destPath, unzipPath)) {
                    throw new Error(`Attempted zip slip attack with file: ${entry.fileName}`);
                }
            }
        });
    } catch (err) {
        console.error("Extraction failed:", err);
        // Clean up the zip file
        try {
            fs.unlinkSync(zipPath);
        } catch (cleanupErr) {
            console.error("Error during cleanup:", cleanupErr);
        }
        return res.status(500).send("Extraction failed.");
    }

    // Locate .gdb folder
    let extractedFiles;
    try {
        extractedFiles = fs.readdirSync(unzipPath);
    } catch (err) {
        console.error("Error reading extracted files:", err);
        return res.status(500).send("Error reading extracted files.");
    }

    const gdbFolder = extractedFiles.find(f => f.endsWith(".gdb"));

    if (!gdbFolder) {
        return res.status(400).send("No .gdb found.");
    }

    const gdbPath = path.join(unzipPath, gdbFolder);

    let dataset;
    let results = [];

    try {
        // Read GDB and extract coordinates
        dataset = gdal.open(gdbPath);

        dataset.layers.forEach((layer) => {
            layer.features.forEach((feature) => {
                const geom = feature.getGeometry();
                if (geom) results.push(JSON.parse(geom.toJSON()));
            });
        });

        // Important: Close the dataset before cleaning up
        dataset.close();
        dataset = null;

        // Send response before cleanup
        res.json(results);

        // Clean up after response has been sent
        setTimeout(() => {
            try {
                // Use the fs module to delete files one by one instead of rimraf
                const deleteFolderRecursive = function(directoryPath) {
                    if (fs.existsSync(directoryPath)) {
                        fs.readdirSync(directoryPath).forEach((file) => {
                            const curPath = path.join(directoryPath, file);
                            if (fs.lstatSync(curPath).isDirectory()) {
                                // Recursive
                                deleteFolderRecursive(curPath);
                            } else {
                                // Delete file
                                fs.unlinkSync(curPath);
                            }
                        });
                        fs.rmdirSync(directoryPath);
                    }
                };

                try {
                    // Delete the zip file
                    if (fs.existsSync(zipPath)) {
                        fs.unlinkSync(zipPath);
                    }
                    
                    // Delete the extracted directory
                    deleteFolderRecursive(unzipPath);
                    
                    // Ensure uploads directory exists
                    if (!fs.existsSync("uploads")) {
                        fs.mkdirSync("uploads");
                    }
                } catch (cleanupErr) {
                    console.error("Manual cleanup error:", cleanupErr);
                }
            } catch (error) {
                console.error("Cleanup operation failed:", error);
            }
        }, 2000); // 2 second delay for safer cleanup

    } catch (err) {
        console.error("Error reading GDB:", err);
        // Make sure to close the dataset if it was opened
        if (dataset) {
            try {
                dataset.close();
                dataset = null;
            } catch (closeErr) {
                console.error("Error closing dataset:", closeErr);
            }
        }

        return res.status(500).send("Failed to read GDB.");
    }
}

// TO-DO - update this to use Github secrets in WFPREV-402 terraform tasks
app.use(cors({
    origin: ['http://localhost:4200', 'https://wfprev-dev.nrs.gov.bc.ca', 'https://wfprev-tst.nrs.gov.bc.ca/', 'https://wfprev.nrs.gov.bc.ca']
}));

// Set up route
app.post("/upload", handleUpload);

if (require.main === module) {
    // Start the server only if the file is run directly, not during tests
    const server = app.listen(3000, () => console.log("Server running on port 3000"));
}
  
module.exports = {
    app,
    handleUpload
}