# Use official Node.js v20 Alpine image
FROM node:20-alpine

# Create app directory
WORKDIR /usr/src/app

# Copy only what's needed (app code and pre-installed node_modules)
COPY . .

# Expose your app's port (change if needed)
EXPOSE 3000

# Run the app
CMD ["node", "server.js"]