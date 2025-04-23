const awsServerlessExpress = require('aws-serverless-express');
const { app } = require('./server');

const server = awsServerlessExpress.createServer(app);

exports.handler = (event, context) => {
  console.log("=== Incoming Lambda Event ===");
  console.log("HTTP Method:", event.requestContext?.http?.method);
  console.log("Raw Path:", event.rawPath || event.path);
  console.log("Headers:", JSON.stringify(event.headers, null, 2));
  console.log("Is Base64 Encoded:", event.isBase64Encoded);
  console.log("Body (truncated):", event.body?.slice(0, 300)); // avoid spamming logs
  console.log("============================");

  return awsServerlessExpress.proxy(server, event, context);
};