const awsServerlessExpress = require('aws-serverless-express');
const { app } = require('./server');

const binaryMimeTypes = [
  'multipart/form-data',
  'application/zip',
  'application/octet-stream'
];

const server = awsServerlessExpress.createServer(app, null, binaryMimeTypes);

exports.handler = (event, context) => {
  console.log("=== Incoming Lambda Event ===");
  console.log("HTTP Method:", event.requestContext?.http?.method);
  console.log("Raw Path:", event.rawPath);
  console.log("Headers:", JSON.stringify(event.headers, null, 2));
  console.log("Is Base64 Encoded:", event.isBase64Encoded);
  console.log("Body (truncated):", event.body?.slice(0, 300));
  console.log("============================");

  // Map the HTTP API v2 event to something Express can understand
  if (event.requestContext && event.requestContext.http) {
    // For HTTP API v2
    event.httpMethod = event.requestContext.http.method;
    event.path = event.rawPath;
    
    // Extract client IP address for rate limiter
    if (event.headers && (event.headers['x-forwarded-for'] || event.headers['X-Forwarded-For'])) {
      // Normalize header name (API Gateway might use different casing)
      const forwardedHeader = event.headers['x-forwarded-for'] || event.headers['X-Forwarded-For'];
      // Set the IP for express-rate-limit
      event.ip = forwardedHeader.split(',')[0].trim();
    } else if (event.requestContext.http.sourceIp) {
      // Fallback to source IP
      event.ip = event.requestContext.http.sourceIp;
    }
  }

  return awsServerlessExpress.proxy(server, event, context);
};