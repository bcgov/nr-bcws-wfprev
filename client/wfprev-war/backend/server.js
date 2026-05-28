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

// Serve static files from the Angular app at the root
app.use(express.static(path.join(__dirname, 'dist/wfprev/browser')));

// Fallback for absolute asset paths used in code (e.g., /assets/logo.png)
app.use('/assets', express.static(path.join(__dirname, 'dist/wfprev/browser/assets')));

// Send all requests to Angular app
app.all(['/assets/data/checktoken-user.json', '/pub/wfprev/assets/data/checktoken-user.json'], (req, res) => {
    res.sendFile(path.join(__dirname, 'dist/wfprev/browser/assets/data/checktoken-user.json'));
});

app.get('/*', (req, res) => {
    res.sendFile(path.join(__dirname, 'dist/wfprev/browser/index.html'));
});

const server = http.createServer(app);

// Listen on {port} and {hostname} to be accessible from public IP address
server.listen(port, hostname, () => {
    console.log(`angular app running on http://${hostname}:${port}`);
});
