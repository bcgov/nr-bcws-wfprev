const express = require('express');
const http = require('http');
const path = require('path');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();

// Host setup
const port = process.env.PORT || 8080;
const hostname = "0.0.0.0";

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Serve static files from the Angular app
app.use('/pub/wfprev', express.static(path.join(__dirname, 'dist/wfprev')));

// Send all requests to Angular app
app.get('/pub/wfprev/*', (req, res) => {
    res.sendFile(path.join(__dirname, 'dist/wfprev/index.html'));
});

const server = http.createServer(app);

// Listen on {port} and {hostname} to be accessible from public IP address
server.listen(port, hostname, () => {
    console.log(`angular app running on http://${hostname}:${port}`);
});
