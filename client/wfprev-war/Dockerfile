#Step 1: Build the Angular app
FROM node:18 AS build

WORKDIR /src/main/angular

# Add NPM Token from build arguments for private registry authentication
# ARG NPM_TOKEN
# ARG PRIVATE_REGISTRY_AUTH
# RUN echo "//registry.npmjs.org/:_authToken=${NPM_TOKEN}" > .npmrc
# RUN echo "//bwa.nrs.gov.bc.ca/int/artifactory/api/npm/nrs-virtual-npm/:_auth=${PRIVATE_REGISTRY_AUTH}" >> .npmrc

# Copy the package.json and package-lock.json files
COPY src/main/angular/package*.json ./

# Clean the npm cache before installing dependencies
RUN npm cache clean --force

# Remove node_modules and package-lock.json if they exist (optional cleanup)
RUN rm -rf node_modules package-lock.json

# Install npm dependencies
RUN npm install

# Copy the rest of the Angular project files
COPY src/main/angular ./

# Build the Angular project (use --configuration=production instead of --prod)
RUN npm run build --configuration=production

# Step 2: Serve the app with nginx
FROM nginx:alpine

# Copy the built Angular app to Nginx
COPY --from=build /src/main/angular/dist/wfprev/* /usr/share/nginx/html
# Expose port 80
EXPOSE 80

# Start Nginx server
CMD ["nginx", "-g", "daemon off;"]