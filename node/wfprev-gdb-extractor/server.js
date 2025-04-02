const express = require("express");
const fileUpload = require("express-fileupload");
const gdal = require("gdal-async");
const fs = require("fs");
const path = require("path");
const extract = require("extract-zip");
const cors = require('cors');

const app = express();
app.use(fileUpload());

// Ensure 'uploads' directory exists
if (!fs.existsSync("uploads")) fs.mkdirSync("uploads");

// Extract the handler function so it can be tested independently
async function handleUpload(req, res) {
    if (!req.files || !req.files.file) {
        return res.status(400).send("No file uploaded.");
    }

    const zipPath = path.join(__dirname, "uploads", req.files.file.name);
    await req.files.file.mv(zipPath);

    // Extract ZIP file
    const unzipPath = zipPath.replace(".zip", "");
    console.log(`Extracting ${zipPath} to ${unzipPath}`);

    try {
        await extract(zipPath, { dir: unzipPath });
        console.log("Extraction complete.");
    } catch (err) {
        console.error("Extraction failed:", err);
        return res.status(500).send("Extraction failed.");
    }

    // Locate .gdb folder
    let extractedFiles;
    try {
        extractedFiles = fs.readdirSync(unzipPath);
        console.log("Extracted files:", extractedFiles);
    } catch (err) {
        console.error("Error reading extracted files:", err);
        return res.status(500).send("Error reading extracted files.");
    }

    const gdbFolder = extractedFiles.find(f => f.endsWith(".gdb"));

    if (!gdbFolder) {
        return res.status(400).send("No .gdb found.");
    }

    const gdbPath = path.join(unzipPath, gdbFolder);
    console.log(`Opening GDB: ${gdbPath}`);

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
                    deleteFolderRecursive(path.join(__dirname, "uploads"));
                    fs.mkdirSync("uploads");
                    console.log("Cleanup completed successfully");
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
    origin: ['http://localhost:4200', 'https://wfprev-dev.nrs.gov.bc.ca', 'https://wfprev-tst.nrs.gov.bc.ca/', 'https://wfprev-dev.nrs.gov.bc.ca']
}));

// Set up route
app.post("/upload", handleUpload);

app.listen(3000, () => console.log("Server running on port 3000"));

// Export for testing
module.exports = {
    handleUpload
};