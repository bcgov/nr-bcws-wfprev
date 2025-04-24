# Use AWS Lambda Node.js base image
# NOSONAR:Dockerfile:NonRootUser
FROM public.ecr.aws/lambda/nodejs:20

WORKDIR /var/task

# Copy only the necessary files
COPY package*.json ./
COPY .npmrc .npmrc

# NOSONAR:javascript:S4721
RUN npm install 

# Copy only runtime-relevant source files
COPY server.js .
COPY lambda.js .

# Set the Lambda function entry point
CMD ["lambda.handler"]