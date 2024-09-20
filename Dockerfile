#Step 1: Build the Angular app
FROM node:18 AS build

WORKDIR /client/wfprev-war/src/main/angular

# Copy the package.json and package-lock.json files
COPY client/wfprev-war/main/angular/package*.json ./

# Clean the npm cache before installing dependencies
RUN npm cache clean --force

# Remove node_modules and package-lock.json if they exist (optional cleanup)
RUN rm -rf node_modules package-lock.json

# Install npm dependencies
RUN npm install

# Copy the rest of the Angular project files
COPY client/wfprev-war/src/main/angular ./

# Build the Angular project (use --configuration=production instead of --prod)
RUN npm run build --configuration=production

# Step 2: Serve the app with nginx
FROM nginx:alpine

# Copy the built Angular app to Nginx
COPY --from=build /client/wfprev-war/src/main/angular/dist/wfprev/* /usr/share/nginx/html
# Expose port 8080
EXPOSE 8080

# Start Nginx server
CMD ["nginx", "-g", "daemon off;"]