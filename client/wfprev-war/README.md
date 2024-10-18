# WFPrev UI - Docker Setup Guide

This document provides instructions for building and running the WFPrev UI using Docker.

## Prerequisites

Make sure you have the following installed before proceeding:

- **Docker**: Download and install [Docker](https://www.docker.com/products/docker-desktop) on your machine.
- **NPM Authentication Tokens**: If your project requires private npm packages, you will need access to the necessary tokens.

## Steps to Run the UI with Docker

### Run ng build
navigate to Angular folder and run "ng build --configuration production"
### Script change in dockerfile
comment out "COPY ./dist/wfprev ./dist/wfprev" part in Dockerfile, use "# COPY src/main/angular/dist/wfprev ./dist/wfprev" instead
### Build the Docker Image, run following command under wfprev-war folder where dockerfile locates. 
docker build -t wfprev-ui

Then you should see a wfprev-ui appears in your images list.

### Run the image using following command. 
docker run -d -p 8080:8080 --name wfprev-ui-container wfprev-ui

### Verify the UI is running by opening the browser and navigate to localhost:8080
