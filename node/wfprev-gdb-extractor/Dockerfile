# Use AWS Lambda Node.js base image
FROM public.ecr.aws/lambda/nodejs:20

WORKDIR /var/task

# Copy only the necessary files
COPY package*.json ./
COPY .npmrc .npmrc

RUN npm install --omit=dev --ignore-scripts

# Copy only runtime-relevant source files
COPY lambda.js .
COPY server.js .

# Set the Lambda function entry point
CMD ["lambda.handler"]