# Use an official Node.js runtime as a parent image
FROM node:20-alpine

# Set the working directory inside the container
WORKDIR /usr/src/app

# Copy the Angular app build files from the host into the container
COPY src/main/angular/dist ./dist

# Install a lightweight static file server (serve)
RUN npm install -g serve

# Serve the static files from the Angular dist directory on port 8080
CMD ["serve", "-s", "dist/wfprev", "-l", "8080"]

# Expose the port the app will run on
EXPOSE 8080