# Use Node.js as the base image for running the app
FROM node:20-alpine

# Set the working directory inside the container
WORKDIR /app

# Log the contents of the root directory before doing any copies
RUN echo "Root directory contents before copy:" && ls -la /

# Log the contents of the build context (staging folder) to verify its existence
RUN echo "Staging folder contents before copy:" && ls -la ./staging || echo "Staging folder does not exist"

# Copy the pre-built Angular app from the local machine into the container
COPY ./dist/wfprev ./dist/wfprev

# Log the contents of the /app directory after copying
RUN echo "App directory contents after copy:" && ls -la /app

# Log the contents of the /app/dist/wfprev directory after copying
RUN echo "wfprev directory contents after copy:" && ls -la /app/dist/wfprev

# Copy backend files (for serving the Angular app using Express)
COPY backend /app

# Log the backend directory contents
RUN echo "Backend directory contents after backend copy:" && ls -la /app

RUN npm install --only=production

# Expose port 80 for the application
EXPOSE 8080

# Start the Node.js server to serve the Angular app
ENTRYPOINT ["node", "/app/server.js"]


