const { extractAndParseGDB } = require('./server');

exports.handler = async (event) => {
  console.log("=== Incoming Lambda Event ===");
  console.log("Event payload:", JSON.stringify(event, null, 2).slice(0, 500) + "...");
  
  try {
    // Check if we have the file data in the expected field
    const base64Zip = event.file;
    if (!base64Zip) {
      console.error("No file data found in the event");
      return {
        statusCode: 400,
        body: JSON.stringify({ error: "Missing 'file' in payload" }),
        headers: { "Content-Type": "application/json" }
      };
    }

    // Convert base64 to buffer and process it
    const buffer = Buffer.from(base64Zip, "base64");
    console.log(`Received file size: ${buffer.length} bytes`);
    
    // Process the file
    const results = await extractAndParseGDB(buffer);
    console.log(`Extracted ${results.length} geometries from GDB`);

    // Return the results
    return {
      statusCode: 200,
      body: JSON.stringify(results),
      headers: { "Content-Type": "application/json" }
    };
  } catch (err) {
    console.error("Lambda handler error:", err);
    return {
      statusCode: 500,
      body: JSON.stringify({ 
        error: "Failed to process GDB", 
        message: err.message 
      }),
      headers: { "Content-Type": "application/json" }
    };
  }
};