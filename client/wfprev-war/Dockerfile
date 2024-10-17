# Use Node.js as the base image for running the app
FROM node:20-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the pre-built Angular app from the local machine into the container
COPY src/main/angular/dist/wfprev ./dist/wfprev

# Copy backend files (for serving the Angular app using Express)
COPY backend /app/backend
RUN cd /app/backend && npm install --only=production

# Expose port 80 for the application
EXPOSE 80

# Start the Node.js server to serve the Angular app
ENTRYPOINT ["node", "/app/backend/server.js"]
