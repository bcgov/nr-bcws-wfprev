# Use AWS Lambda Node.js base image
# NOSONAR: Use of base image is required for Lambda compatibility
# Security Hotspot docker:S6471 reviewed – safe in AWS Lambda context due to short-lived sandboxed execution
FROM public.ecr.aws/lambda/nodejs:20

WORKDIR /var/task

# Copy necessary files for installation
COPY package*.json ./

# NOSONAR: Installing native dependencies like gdal-async requires scripts
# Security Hotspot docker:S6505 reviewed – safe: gdal-async install requires lifecycle scripts and runs in isolated Lambda context
# The npm registry config comes in as a BuildKit secret (npmrc) so it never lands in an image layer
RUN --mount=type=secret,id=npmrc \
    NPM_CONFIG_USERCONFIG=/run/secrets/npmrc npm install --omit=dev

# Copy only runtime code
COPY server.js .
COPY lambda.js .

# Set the Lambda function entry point
CMD ["lambda.handler"]
