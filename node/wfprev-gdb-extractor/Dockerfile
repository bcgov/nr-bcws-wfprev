# Use AWS Lambda Node.js base image
# NOSONAR: Use of base image is required for Lambda compatibility
FROM public.ecr.aws/lambda/nodejs:20

# Explicitly create a non-root user (Sonar-friendly workaround)
RUN addgroup app && adduser -S -G app app

WORKDIR /var/task

# Copy necessary files for installation
COPY package*.json ./
COPY .npmrc .npmrc

# NOSONAR: Installing native dependencies like gdal-async requires scripts
RUN --mount=type=secret,id=npm_token \
    npm install

# Copy only runtime code
COPY server.js .
COPY lambda.js .

# Change ownership to non-root user and switch
RUN chown -R app:app /var/task
USER app

# Set the Lambda function entry point
CMD ["lambda.handler"]