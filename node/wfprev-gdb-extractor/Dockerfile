# Use AWS Lambda Node.js base image
FROM public.ecr.aws/lambda/nodejs:20

WORKDIR /var/task

# Copy only the necessary files
COPY package*.json ./
COPY .npmrc .npmrc

RUN npm install --omit=dev --ignore-scripts

# Copy only runtime-relevant source files
COPY server.js .
COPY lambda.js .

# SECURITY NOTE: Running as non-root user for Lambda execution
# AWS Lambda provides its own security context, so this is considered safe
USER 1000

# Set the Lambda function entry point
CMD ["lambda.handler"]