const fs = require('fs');
const path = require('path');

module.exports = function (config) {
  const coverageDir = path.join(__dirname, './coverage/wfprev');

  // Ensure the directory exists or create it safely
  try {
    if (!fs.existsSync(coverageDir)) {
      fs.mkdirSync(coverageDir, { recursive: true });
    }
  } catch (err) {
    console.error(`Failed to create directory ${coverageDir}:`, err);
  }

  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma'),
    ],
    client: {
      clearContext: false, // Leave Jasmine Spec Runner output visible in the browser
    },
    coverageReporter: {
      dir: coverageDir,
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' },
        { type: 'json-summary' },
        { type: 'lcov' },
      ],
      clean: true, // Ensure the coverage directory is cleaned automatically
    },
    preprocessors: {
      'src/**/*.ts': ['coverage'], // Instrument your TypeScript files for coverage
    },
    reporters: ['progress', 'kjhtml', 'coverage'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['ChromeHeadless'],
    singleRun: false,
    restartOnFileChange: true,
  });
};
